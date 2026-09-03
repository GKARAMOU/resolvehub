package dev.karamousalis.resolvehub.ticket;

import java.util.EnumSet;
import java.util.Set;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    WAITING_ON_CUSTOMER,
    RESOLVED,
    CLOSED;

    public boolean canTransitionTo(TicketStatus target) {
        return allowedTargets().contains(target);
    }

    private Set<TicketStatus> allowedTargets() {
        return switch (this) {
            case OPEN -> EnumSet.of(IN_PROGRESS, RESOLVED, CLOSED);
            case IN_PROGRESS -> EnumSet.of(WAITING_ON_CUSTOMER, RESOLVED, OPEN);
            case WAITING_ON_CUSTOMER -> EnumSet.of(IN_PROGRESS, RESOLVED);
            case RESOLVED -> EnumSet.of(CLOSED, IN_PROGRESS);
            case CLOSED -> EnumSet.of(OPEN);
        };
    }
}
