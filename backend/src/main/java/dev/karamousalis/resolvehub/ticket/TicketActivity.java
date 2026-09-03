package dev.karamousalis.resolvehub.ticket;

import dev.karamousalis.resolvehub.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_activities")
public class TicketActivity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private AppUser actor;

    @Column(name = "activity_type", nullable = false, length = 40)
    private String activityType;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected TicketActivity() {}

    public TicketActivity(Ticket ticket, AppUser actor, String activityType, String message) {
        this.id = UUID.randomUUID();
        this.ticket = ticket;
        this.actor = actor;
        this.activityType = activityType;
        this.message = message;
        this.occurredAt = Instant.now();
    }

    public UUID getId() { return id; }
    public AppUser getActor() { return actor; }
    public String getActivityType() { return activityType; }
    public String getMessage() { return message; }
    public Instant getOccurredAt() { return occurredAt; }
}
