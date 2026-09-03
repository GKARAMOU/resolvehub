package dev.karamousalis.resolvehub.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingTicketEventPublisher implements TicketEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(LoggingTicketEventPublisher.class);

    @Override
    public void publish(TicketLifecycleEvent event) {
        log.info("Ticket lifecycle event: {} {} {}", event.reference(), event.eventType(), event.status());
    }
}
