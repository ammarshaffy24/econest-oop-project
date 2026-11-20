package model;

// Tasks 8 & 9: one email notification that the app has sent
public class Email {
    private final String sentAt;
    private final int orderId;
    private final String to;
    private final String subject;
    private final String body;

    public Email(String sentAt, int orderId, String to, String subject, String body) {
        this.sentAt = sentAt;
        this.orderId = orderId;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public String getSentAt() { return sentAt; }
    public int getOrderId() { return orderId; }
    public String getTo() { return to; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
}
