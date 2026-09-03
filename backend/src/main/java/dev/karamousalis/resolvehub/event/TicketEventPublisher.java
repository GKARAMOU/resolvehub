package dev.karamousalis.resolvehub.event;

public interface TicketEventPublisher {
    void publish(TicketLifecycleEvent event);
}
