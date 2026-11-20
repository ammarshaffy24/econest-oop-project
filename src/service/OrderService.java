package service;

import dao.OrderDAO;
import dao.StockDAO;
import model.Order;
import model.OrderItem;
import model.StockItem;

// Task 5: places an order only if every item is in stock, then reduces the stock quantities
public class OrderService {
    private final OrderDAO orders;
    private final StockDAO stock;

    public OrderService(OrderDAO orders, StockDAO stock) {
        this.orders = orders;
        this.stock = stock;
    }

    public int availableStock(String productId) {
        StockItem s = stock.findByProductId(productId);
        return s == null ? 0 : s.getQuantityOnHand();
    }

    // returns an error message, or null when the order was saved
    public String placeOrder(Order order) {
        if (order.getItems().isEmpty()) return "Add at least one product before placing the order.";
        for (OrderItem item : order.getItems()) {
            int available = availableStock(item.getProductId());
            if (item.getQuantity() > available)
                return "Only " + available + " left in stock for " + item.getProductName() + ".";
        }
        for (OrderItem item : order.getItems())
            stock.reduceStock(item.getProductId(), item.getQuantity());
        orders.add(order);
        return null;
    }
}
