import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private String orderCode;
    private Client client;
    private LocalDate orderDate;
    private List<OrderItem> items = new ArrayList<>();
    private String status = "Pending";

    public Order(String orderCode, Client client, LocalDate orderDate) {
        this.orderCode = orderCode;
        this.client = client;
        this.orderDate = orderDate;
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public void removeItemAt(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getLineTotal());
        }
        return total;
    }

    public String getOrderCode() { return orderCode; }
    public Client getClient() { return client; }
    public LocalDate getOrderDate() { return orderDate; }
    public List<OrderItem> getItems() { return items; }
    public String getStatus() { return status; }
}