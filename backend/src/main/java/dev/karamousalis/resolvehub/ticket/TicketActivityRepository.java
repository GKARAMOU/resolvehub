package dev.karamousalis.resolvehub.ticket;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketActivityRepository extends JpaRepository<TicketActivity, UUID> {
    @EntityGraph(attributePaths = "actor")
    List<TicketActivity> findByTicketIdOrderByOccurredAtDesc(UUID ticketId);
}
