package service;

import model.Client;
import model.DeliveryAgent;
import model.Email;
import model.Order;

// Task 8: emails a client when their order has been dispatched
public class DispatchNotifier {
    private final EmailService emailService;

    public DispatchNotifier(EmailService emailService) {
        this.emailService = emailService;
    }

    public Email notifyClient(Client client, DeliveryAgent agent, Order order) {
        String subject = "Your EcoNest order " + order.getCode() + " has been dispatched";
        String body = "Hello " + client.getName() + ",\n\n"
                + "Good news! Your order is on its way.\n\n"
                + "Order ID: " + order.getCode() + "\n"
                + "Items: " + DeliveryNotifier.itemSummary(order) + "\n"
                + "Total: LKR " + String.format("%,.2f", order.getTotal()) + "\n"
                + "Expected delivery: " + order.getDeliveryDate() + "\n"
                + "Delivery agent: " + agent.getName() + " (" + agent.getPhone() + ")\n"
                + "Delivery address: " + client.getAddress() + "\n\n"
                + "Thank you for shopping sustainably with EcoNest.\n\n"
                + "Regards,\nEcoNest";

        return emailService.send(order.getId(), client.getEmail(), subject, body);
    }
}
