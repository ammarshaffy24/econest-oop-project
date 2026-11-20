package dao;

import model.Order;
import model.OrderItem;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Saves orders to data/orders.txt   (id|clientId|orderDate|deliveryFee|agentId|deliveryDate|status)
// and their items to data/order_items.txt   (orderId|productId|productName|quantity|unitPrice)
public class OrderDAO {
    private final List<Order> list = new ArrayList<>();
    private final File ordersFile = new File("data", "orders.txt");
    private final File itemsFile = new File("data", "order_items.txt");

    public OrderDAO() {
        loadOrders();
        loadItems();
    }

    public List<Order> getAll() { return list; }

    public Order findById(int id) {
        for (Order o : list) if (o.getId() == id) return o;
        return null;
    }

    public void add(Order order) {
        order.setId(list.stream().mapToInt(Order::getId).max().orElse(0) + 1);
        list.add(order);
        save();
    }

    public int nextId() {
        return list.stream().mapToInt(Order::getId).max().orElse(0) + 1;
    }

    public void save() {
        ordersFile.getParentFile().mkdirs();
        try (PrintWriter orders = new PrintWriter(ordersFile); PrintWriter items = new PrintWriter(itemsFile)) {
            for (Order o : list) {
                orders.println(o.getId() + "|" + o.getClientId() + "|" + o.getOrderDate() + "|" + o.getDeliveryFee()
                        + "|" + o.getAgentId() + "|" + o.getDeliveryDate() + "|" + o.getStatus());
                for (OrderItem i : o.getItems())
                    items.println(o.getId() + "|" + i.getProductId() + "|" + clean(i.getProductName()) + "|"
                            + i.getQuantity() + "|" + i.getUnitPrice());
            }
        } catch (IOException e) {
            System.err.println("Could not write orders: " + e.getMessage());
        }
    }

    private void loadOrders() {
        for (String[] p : readLines(ordersFile)) {
            Order o = new Order(Integer.parseInt(p[0]), Integer.parseInt(p[1]), p[2], Double.parseDouble(p[3]));
            o.setDelivery(Integer.parseInt(p[4]), p[5], p[6]);
            list.add(o);
        }
    }

    private void loadItems() {
        for (String[] p : readLines(itemsFile)) {
            Order o = findById(Integer.parseInt(p[0]));
            if (o != null)
                o.addItem(new OrderItem(p[1], p[2], Integer.parseInt(p[3]), Double.parseDouble(p[4])));
        }
    }

    private List<String[]> readLines(File file) {
        List<String[]> rows = new ArrayList<>();
        if (!file.exists()) return rows;
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null)
                if (!line.isEmpty()) rows.add(line.split("\\|", -1));
        } catch (IOException e) {
            System.err.println("Could not read " + file + ": " + e.getMessage());
        }
        return rows;
    }

    private static String clean(String s) {
        return s.replace("|", "/");
    }
}
