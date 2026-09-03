package dev.karamousalis.resolvehub.config;

import dev.karamousalis.resolvehub.ticket.TicketPriority;
import dev.karamousalis.resolvehub.ticket.TicketRepository;
import dev.karamousalis.resolvehub.ticket.TicketService;
import dev.karamousalis.resolvehub.ticket.TicketService.CreateTicket;
import dev.karamousalis.resolvehub.user.AppUser;
import dev.karamousalis.resolvehub.user.AppUserRepository;
import dev.karamousalis.resolvehub.user.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoDataConfig {
    @Bean
    @ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
    CommandLineRunner seedDemoData(
            AppUserRepository users,
            TicketRepository tickets,
            PasswordEncoder passwords,
            TicketService ticketService) {
        return args -> {
            if (users.count() > 0 || tickets.count() > 0) return;

            AppUser admin = users.save(new AppUser(
                    "admin@resolvehub.example", passwords.encode("ResolveHubDemo!2026"), "Maya Chen", UserRole.ADMIN));
            AppUser agent = users.save(new AppUser(
                    "agent@resolvehub.example", passwords.encode("ResolveHubDemo!2026"), "Alex Rivera", UserRole.AGENT));
            AppUser requester = users.save(new AppUser(
                    "requester@resolvehub.example", passwords.encode("ResolveHubDemo!2026"), "Jordan Lee", UserRole.REQUESTER));

            var payments = ticketService.create(requester.getEmail(), new CreateTicket(
                    "Payment reconciliation is delayed",
                    "Finance imports remain in processing for more than fifteen minutes.",
                    TicketPriority.CRITICAL));
            ticketService.assign(payments.id(), admin.getEmail(), agent.getId());

            ticketService.create(requester.getEmail(), new CreateTicket(
                    "Invite emails are not delivered",
                    "New workspace members do not receive their invitation email.",
                    TicketPriority.HIGH));
            ticketService.create(admin.getEmail(), new CreateTicket(
                    "Add export filters to the audit log",
                    "Operations needs date, actor and event filters before CSV export.",
                    TicketPriority.MEDIUM));
        };
    }
}
