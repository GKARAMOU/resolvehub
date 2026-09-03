package dev.karamousalis.resolvehub.ticket;

import dev.karamousalis.resolvehub.ticket.TicketService.ActivityView;
import dev.karamousalis.resolvehub.ticket.TicketService.CreateTicket;
import dev.karamousalis.resolvehub.ticket.TicketService.DashboardView;
import dev.karamousalis.resolvehub.ticket.TicketService.TicketDetails;
import dev.karamousalis.resolvehub.ticket.TicketService.TicketView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TicketController {
    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public DashboardView dashboard() {
        return service.dashboard();
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketView create(Principal principal, @Valid @RequestBody CreateTicketRequest request) {
        return service.create(principal.getName(), new CreateTicket(
                request.subject(), request.description(), request.priority()));
    }

    @GetMapping("/tickets")
    public List<TicketView> list(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) String q) {
        return service.list(status, priority, q);
    }

    @GetMapping("/tickets/{id}")
    public TicketDetails get(@PathVariable UUID id) {
        return service.get(id);
    }

    @PatchMapping("/tickets/{id}/status")
    public TicketView changeStatus(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody StatusRequest request) {
        return service.changeStatus(id, principal.getName(), request.status());
    }

    @PostMapping("/tickets/{id}/assign")
    public TicketView assign(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody AssignmentRequest request) {
        return service.assign(id, principal.getName(), request.assigneeId());
    }

    @PostMapping("/tickets/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityView comment(
            Principal principal,
            @PathVariable UUID id,
            @Valid @RequestBody CommentRequest request) {
        return service.addComment(id, principal.getName(), request.message());
    }

    public record CreateTicketRequest(
            @NotBlank @Size(max = 180) String subject,
            @NotBlank @Size(max = 4000) String description,
            @NotNull TicketPriority priority) {}

    public record StatusRequest(@NotNull TicketStatus status) {}
    public record AssignmentRequest(@NotNull UUID assigneeId) {}
    public record CommentRequest(@NotBlank @Size(max = 500) String message) {}
}
