package service;

import model.Client;
import model.Email;
import model.DeliveryAgent;
import model.Order;
import model.OrderItem;

// Task 9: emails a delivery agent when a new delivery is assigned to them
public class DeliveryNotifier {
    private final EmailService emailService;

    public DeliveryNotifier(EmailService emailService) {
        this.emailService = emailService;
    }

    public Email notifyAgent(DeliveryAgent agent, Client client, Order order) {
        String subject = "New Delivery Assigned - Order " + order.getCode();
        String body = "Hello " + agent.getName() + ",\n\n"
                + "A new delivery has been assigned to you.\n\n"
                + "Order ID: " + order.getCode() + "\n"
                + "Client: " + client.getName() + "\n"
                + "Phone: " + client.getPhone() + "\n"
                + "Address: " + client.getAddress() + "\n"
                + "Delivery date: " + order.getDeliveryDate() + "\n"
                + "Items: " + itemSummary(order) + "\n"
                + "Your vehicle: " + agent.getVehicleType() + " " + agent.getVehicleNo() + "\n\n"
                + "Please confirm once the delivery is completed.\n\n"
                + "Regards,\nEcoNest";

        return emailService.send(order.getId(), agent.getEmail(), subject, body);
    }

    static String itemSummary(Order order) {
        StringBuilder summary = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            if (summary.length() > 0) summary.append(", ");
            summary.append(item.getQuantity()).append(" x ").append(item.getProductName());
        }
        return summary.length() == 0 ? "-" : summary.toString();
    }
}
