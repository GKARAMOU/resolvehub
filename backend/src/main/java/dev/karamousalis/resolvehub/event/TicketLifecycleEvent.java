package dev.karamousalis.resolvehub.event;

import dev.karamousalis.resolvehub.ticket.TicketPriority;
import dev.karamousalis.resolvehub.ticket.TicketStatus;
import java.time.Instant;
import java.util.UUID;

public record TicketLifecycleEvent(
        UUID ticketId,
        String reference,
        String eventType,
        TicketStatus status,
        TicketPriority priority,
        Instant occurredAt) {}
