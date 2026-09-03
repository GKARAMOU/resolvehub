package dev.karamousalis.resolvehub.ticket;

import java.time.Duration;

public enum TicketPriority {
    LOW(Duration.ofDays(5)),
    MEDIUM(Duration.ofDays(3)),
    HIGH(Duration.ofHours(24)),
    CRITICAL(Duration.ofHours(4));

    private final Duration responseWindow;

    TicketPriority(Duration responseWindow) {
        this.responseWindow = responseWindow;
    }

    public Duration responseWindow() {
        return responseWindow;
    }
}
