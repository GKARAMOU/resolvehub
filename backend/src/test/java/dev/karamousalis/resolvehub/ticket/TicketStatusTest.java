package dev.karamousalis.resolvehub.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TicketStatusTest {
    @Test
    void supportsTheExpectedSupportWorkflow() {
        assertThat(TicketStatus.OPEN.canTransitionTo(TicketStatus.IN_PROGRESS)).isTrue();
        assertThat(TicketStatus.IN_PROGRESS.canTransitionTo(TicketStatus.WAITING_ON_CUSTOMER)).isTrue();
        assertThat(TicketStatus.WAITING_ON_CUSTOMER.canTransitionTo(TicketStatus.RESOLVED)).isTrue();
        assertThat(TicketStatus.RESOLVED.canTransitionTo(TicketStatus.CLOSED)).isTrue();
    }

    @Test
    void blocksInvalidWorkflowJumps() {
        assertThat(TicketStatus.OPEN.canTransitionTo(TicketStatus.WAITING_ON_CUSTOMER)).isFalse();
        assertThat(TicketStatus.WAITING_ON_CUSTOMER.canTransitionTo(TicketStatus.CLOSED)).isFalse();
        assertThat(TicketStatus.CLOSED.canTransitionTo(TicketStatus.RESOLVED)).isFalse();
    }
}
