package dev.karamousalis.resolvehub.ticket;

import dev.karamousalis.resolvehub.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String reference;

    @Column(nullable = false, length = 180)
    private String subject;

    @Column(nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private AppUser reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private AppUser assignee;

    @Column(name = "due_at", nullable = false)
    private Instant dueAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Ticket() {}

    public Ticket(String subject, String description, TicketPriority priority, AppUser reporter) {
        this.id = UUID.randomUUID();
        this.reference = "RH-" + id.toString().substring(0, 8).toUpperCase(Locale.ROOT);
        this.subject = subject.trim();
        this.description = description.trim();
        this.priority = priority;
        this.status = TicketStatus.OPEN;
        this.reporter = reporter;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
        this.dueAt = createdAt.plus(priority.responseWindow());
    }

    public void transitionTo(TicketStatus target) {
        if (target == status) return;
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException("Cannot transition a ticket from " + status + " to " + target);
        }
        status = target;
        updatedAt = Instant.now();
    }

    public void assignTo(AppUser user) {
        assignee = user;
        if (status == TicketStatus.OPEN) status = TicketStatus.IN_PROGRESS;
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getReference() { return reference; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public TicketPriority getPriority() { return priority; }
    public TicketStatus getStatus() { return status; }
    public AppUser getReporter() { return reporter; }
    public AppUser getAssignee() { return assignee; }
    public Instant getDueAt() { return dueAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public boolean isSlaBreached() {
        return dueAt.isBefore(Instant.now()) && status != TicketStatus.RESOLVED && status != TicketStatus.CLOSED;
    }
}
