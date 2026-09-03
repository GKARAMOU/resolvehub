package dev.karamousalis.resolvehub.ticket;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    @EntityGraph(attributePaths = {"reporter", "assignee"})
    List<Ticket> findAllByOrderByUpdatedAtDesc();
}
