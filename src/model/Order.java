package model;

import java.util.ArrayList;
import java.util.List;

// Task 5 creates an order with its items; Task 6 assigns an agent and tracks delivery status.
public class Order {
    public static final String PENDING = "Pending";
    public static final String ASSIGNED = "Assigned";
    public static final String DISPATCHED = "Dispatched";
    public static final String DELIVERED = "Delivered";
    public static final int NO_AGENT = 0;
    public static final String NO_DATE = "-";

    private int id;
    private final int clientId;
    private final String orderDate;   // date text, e.g. 2026-11-14
    private final double deliveryFee;
    private final List<OrderItem> items = new ArrayList<>();
    private int agentId = NO_AGENT;
    private String deliveryDate = NO_DATE;
    private String status = PENDING;

    public Order(int id, int clientId, String orderDate, double deliveryFee) {
        this.id = id;
        this.clientId = clientId;
        this.orderDate = orderDate;
        this.deliveryFee = deliveryFee;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return "ORD-" + id; }
    public int getClientId() { return clientId; }
    public String getOrderDate() { return orderDate; }
    public double getDeliveryFee() { return deliveryFee; }
    public List<OrderItem> getItems() { return items; }
    public int getAgentId() { return agentId; }
    public String getDeliveryDate() { return deliveryDate; }
    public String getStatus() { return status; }

    public boolean hasAgent() { return agentId != NO_AGENT; }

    // adding the same product twice just increases its quantity
    public void addItem(OrderItem item) {
        for (OrderItem existing : items) {
            if (existing.getProductId().equals(item.getProductId())) {
                existing.addQuantity(item.getQuantity());
                return;
            }
        }
        items.add(item);
    }

    public int quantityOf(String productId) {
        for (OrderItem item : items) if (item.getProductId().equals(productId)) return item.getQuantity();
        return 0;
    }

    public double getSubTotal() {
        double sum = 0;
        for (OrderItem item : items) sum += item.getLineTotal();
        return sum;
    }

    public double getTotal() { return getSubTotal() + deliveryFee; }

    public int getItemCount() {
        int count = 0;
        for (OrderItem item : items) count += item.getQuantity();
        return count;
    }

    public void assign(int agentId, String deliveryDate) {
        this.agentId = agentId;
        this.deliveryDate = deliveryDate;
        this.status = ASSIGNED;
    }

    public void setDelivery(int agentId, String deliveryDate, String status) {
        this.agentId = agentId;
        this.deliveryDate = deliveryDate;
        this.status = status;
    }

    public void setStatus(String status) { this.status = status; }
}
