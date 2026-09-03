package dev.karamousalis.resolvehub.ticket;

import dev.karamousalis.resolvehub.event.TicketEventPublisher;
import dev.karamousalis.resolvehub.event.TicketLifecycleEvent;
import dev.karamousalis.resolvehub.user.AppUser;
import dev.karamousalis.resolvehub.user.AppUserRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TicketService {
    private final TicketRepository tickets;
    private final TicketActivityRepository activities;
    private final AppUserRepository users;
    private final TicketEventPublisher events;

    public TicketService(
            TicketRepository tickets,
            TicketActivityRepository activities,
            AppUserRepository users,
            TicketEventPublisher events) {
        this.tickets = tickets;
        this.activities = activities;
        this.users = users;
        this.events = events;
    }

    public TicketView create(String currentEmail, CreateTicket command) {
        AppUser reporter = userByEmail(currentEmail);
        Ticket ticket = tickets.save(new Ticket(
                command.subject(), command.description(), command.priority(), reporter));
        activities.save(new TicketActivity(ticket, reporter, "CREATED", "Ticket created"));
        publish(ticket, "CREATED");
        return TicketView.from(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketView> list(TicketStatus status, TicketPriority priority, String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return tickets.findAllByOrderByUpdatedAtDesc().stream()
                .filter(ticket -> status == null || ticket.getStatus() == status)
                .filter(ticket -> priority == null || ticket.getPriority() == priority)
                .filter(ticket -> normalizedQuery.isBlank()
                        || ticket.getSubject().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                        || ticket.getReference().toLowerCase(Locale.ROOT).contains(normalizedQuery))
                .map(TicketView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketDetails get(UUID id) {
        Ticket ticket = ticketById(id);
        List<ActivityView> timeline = activities.findByTicketIdOrderByOccurredAtDesc(id).stream()
                .map(ActivityView::from)
                .toList();
        return new TicketDetails(TicketView.from(ticket), ticket.getDescription(), timeline);
    }

    public TicketView changeStatus(UUID id, String currentEmail, TicketStatus target) {
        Ticket ticket = ticketById(id);
        AppUser actor = userByEmail(currentEmail);
        TicketStatus previous = ticket.getStatus();
        try {
            ticket.transitionTo(target);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage());
        }
        activities.save(new TicketActivity(
                ticket, actor, "STATUS_CHANGED", "Status changed from " + previous + " to " + target));
        publish(ticket, "STATUS_CHANGED");
        return TicketView.from(ticket);
    }

    public TicketView assign(UUID id, String currentEmail, UUID assigneeId) {
        Ticket ticket = ticketById(id);
        AppUser actor = userByEmail(currentEmail);
        AppUser assignee = users.findById(assigneeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found"));
        ticket.assignTo(assignee);
        activities.save(new TicketActivity(
                ticket, actor, "ASSIGNED", "Assigned to " + assignee.getDisplayName()));
        publish(ticket, "ASSIGNED");
        return TicketView.from(ticket);
    }

    public ActivityView addComment(UUID id, String currentEmail, String message) {
        Ticket ticket = ticketById(id);
        AppUser actor = userByEmail(currentEmail);
        TicketActivity activity = activities.save(new TicketActivity(ticket, actor, "COMMENT", message.trim()));
        publish(ticket, "COMMENT_ADDED");
        return ActivityView.from(activity);
    }

    @Transactional(readOnly = true)
    public DashboardView dashboard() {
        List<Ticket> all = tickets.findAllByOrderByUpdatedAtDesc();
        long open = all.stream().filter(ticket -> ticket.getStatus() == TicketStatus.OPEN).count();
        long active = all.stream().filter(ticket -> ticket.getStatus() == TicketStatus.IN_PROGRESS).count();
        long breached = all.stream().filter(Ticket::isSlaBreached).count();
        long resolved = all.stream().filter(ticket -> ticket.getStatus() == TicketStatus.RESOLVED).count();
        List<TicketView> urgent = all.stream()
                .filter(ticket -> ticket.getStatus() != TicketStatus.CLOSED)
                .sorted(Comparator.comparing(Ticket::getDueAt))
                .limit(5)
                .map(TicketView::from)
                .toList();
        return new DashboardView(all.size(), open, active, resolved, breached, urgent);
    }

    private AppUser userByEmail(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private Ticket ticketById(UUID id) {
        return tickets.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    }

    private void publish(Ticket ticket, String eventType) {
        events.publish(new TicketLifecycleEvent(
                ticket.getId(), ticket.getReference(), eventType, ticket.getStatus(), ticket.getPriority(), Instant.now()));
    }

    public record CreateTicket(String subject, String description, TicketPriority priority) {}

    public record TicketView(
            UUID id,
            String reference,
            String subject,
            TicketPriority priority,
            TicketStatus status,
            PersonView reporter,
            PersonView assignee,
            Instant dueAt,
            Instant updatedAt,
            boolean slaBreached) {
        static TicketView from(Ticket ticket) {
            return new TicketView(
                    ticket.getId(),
                    ticket.getReference(),
                    ticket.getSubject(),
                    ticket.getPriority(),
                    ticket.getStatus(),
                    PersonView.from(ticket.getReporter()),
                    PersonView.from(ticket.getAssignee()),
                    ticket.getDueAt(),
                    ticket.getUpdatedAt(),
                    ticket.isSlaBreached());
        }
    }

    public record TicketDetails(TicketView ticket, String description, List<ActivityView> timeline) {}

    public record PersonView(UUID id, String displayName) {
        static PersonView from(AppUser user) {
            return user == null ? null : new PersonView(user.getId(), user.getDisplayName());
        }
    }

    public record ActivityView(UUID id, String type, String message, PersonView actor, Instant occurredAt) {
        static ActivityView from(TicketActivity activity) {
            return new ActivityView(
                    activity.getId(),
                    activity.getActivityType(),
                    activity.getMessage(),
                    PersonView.from(activity.getActor()),
                    activity.getOccurredAt());
        }
    }

    public record DashboardView(
            long total,
            long open,
            long inProgress,
            long resolved,
            long slaBreached,
            List<TicketView> urgentTickets) {}
}
