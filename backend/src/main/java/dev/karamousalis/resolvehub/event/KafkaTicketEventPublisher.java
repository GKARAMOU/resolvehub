package dev.karamousalis.resolvehub.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaTicketEventPublisher implements TicketEventPublisher {
    private final KafkaTemplate<String, Object> kafka;
    private final String topic;

    public KafkaTicketEventPublisher(
            KafkaTemplate<String, Object> kafka,
            @Value("${app.kafka.topic}") String topic) {
        this.kafka = kafka;
        this.topic = topic;
    }

    @Override
    public void publish(TicketLifecycleEvent event) {
        kafka.send(topic, event.reference(), event);
    }
}
