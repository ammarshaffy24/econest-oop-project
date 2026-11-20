package model;

// Task 5: one product line inside an order. The price is copied so later price changes don't alter old orders.
public class OrderItem {
    private final String productId;
    private final String productName;
    private int quantity;
    private final double unitPrice;

    public OrderItem(String productId, String productName, int quantity, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getLineTotal() { return unitPrice * quantity; }

    public void addQuantity(int extra) { quantity += extra; }
}
