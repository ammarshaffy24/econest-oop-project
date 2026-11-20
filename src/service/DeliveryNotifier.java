package service;


import jakarta.mail.MessagingException;
import model.Client;
import model.DeliveryAgent;

public class DeliveryNotifier {
    private final EmailService emailService;

    public DeliveryNotifier(EmailService emailService) {
        this.emailService = emailService;
    }

    public void notifyAgent(DeliveryAgent agent, Client client,
                            int orderId, String deliveryDate, String itemSummary)
            throws MessagingException {

        String email = agent.getEmail();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Delivery agent has no email address.");
        }

        String subject = "New Delivery Assigned - Order #" + orderId;
        String body = "Hello " + agent.getName() + ",\n\n"
                + "A new delivery has been assigned to you.\n\n"
                + "Order ID: " + orderId + "\n"
                + "Client: " + client.getName() + "\n"
                + "Phone: " + client.getPhone() + "\n"
                + "Address: " + client.getAddress() + "\n"
                + "Delivery date: " + deliveryDate + "\n"
                + "Items: " + itemSummary + "\n"
                + "Your vehicle: " + agent.getVehicleType() + " " + agent.getVehicleNo() + "\n\n"
                + "Please confirm once the delivery is completed.\n\n"
                + "Regards,\nEcoNest";

        emailService.send(email, subject, body);
    }
}
