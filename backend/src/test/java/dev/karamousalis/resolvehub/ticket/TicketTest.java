package dev.karamousalis.resolvehub.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.karamousalis.resolvehub.user.AppUser;
import dev.karamousalis.resolvehub.user.UserRole;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class TicketTest {
    private final AppUser reporter = new AppUser(
            "reporter@example.com", "encoded-password", "Alex Reporter", UserRole.REQUESTER);

    @Test
    void calculatesTheCriticalSlaFromTheCreationTime() {
        Ticket ticket = new Ticket("Production outage", "Checkout is unavailable", TicketPriority.CRITICAL, reporter);

        Duration slaWindow = Duration.between(ticket.getCreatedAt(), ticket.getDueAt());

        assertThat(slaWindow).isEqualTo(Duration.ofHours(4));
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(ticket.getReference()).startsWith("RH-");
    }

    @Test
    void assignmentAutomaticallyStartsWork() {
        AppUser agent = new AppUser("agent@example.com", "encoded-password", "Avery Agent", UserRole.AGENT);
        Ticket ticket = new Ticket("Export issue", "CSV export times out", TicketPriority.HIGH, reporter);

        ticket.assignTo(agent);

        assertThat(ticket.getAssignee()).isEqualTo(agent);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void rejectsAnInvalidStatusTransition() {
        Ticket ticket = new Ticket("Invite issue", "Invites are delayed", TicketPriority.MEDIUM, reporter);

        assertThatThrownBy(() -> ticket.transitionTo(TicketStatus.WAITING_ON_CUSTOMER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OPEN")
                .hasMessageContaining("WAITING_ON_CUSTOMER");
    }
}
