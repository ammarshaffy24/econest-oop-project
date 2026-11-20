import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public List<Client> getAllClients() throws SQLException {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT client_id, name, client_type FROM clients ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Client(
                    rs.getInt("client_id"),
                    rs.getString("name"),
                    rs.getString("client_type")
                ));
            }
        }
        return list;
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT product_id, name, category, unit_price, eco_rating FROM products ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Product(
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getBigDecimal("unit_price"),
                    rs.getInt("eco_rating")
                ));
            }
        }
        return list;
    }

    public String generateNextOrderCode() throws SQLException {
        String sql = "SELECT COALESCE(MAX(order_id), 0) + 1 AS next_id FROM orders";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int nextNum = rs.getInt("next_id");
                return String.format("ORD-%03d", nextNum);
            }
        }
        return "ORD-001";
    }

    public int getAvailableStock(int productId) throws SQLException {
        String sql = "SELECT quantity_on_hand FROM inventory WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity_on_hand");
                }
            }
        }
        return 0;
    }

    public boolean saveOrder(Order order) throws SQLException {
        String insertOrderSql = "INSERT INTO orders (client_id, order_date, total_amount, status) VALUES (?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";
        String updateStockSql = "UPDATE inventory SET quantity_on_hand = quantity_on_hand - ? WHERE product_id = ? AND quantity_on_hand >= ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int generatedOrderId;
            try (PreparedStatement psOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, order.getClient().getId());
                psOrder.setDate(2, Date.valueOf(order.getOrderDate()));
                psOrder.setBigDecimal(3, order.getTotalAmount());
                psOrder.setString(4, order.getStatus());
                psOrder.executeUpdate();

                try (ResultSet keys = psOrder.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("Failed to retrieve generated order ID.");
                    }
                    generatedOrderId = keys.getInt(1);
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement(insertItemSql);
                 PreparedStatement psStock = conn.prepareStatement(updateStockSql)) {

                for (OrderItem item : order.getItems()) {
                    int prodId = item.getProduct().getId();
                    int qty = item.getQuantity();
                    BigDecimal unitPrice = item.getUnitPrice();
                    BigDecimal lineTotal = item.getLineTotal();

                    psStock.setInt(1, qty);
                    psStock.setInt(2, prodId);
                    psStock.setInt(3, qty);
                    int updatedRows = psStock.executeUpdate();
                    if (updatedRows == 0) {
                        throw new SQLException("Insufficient stock for: " + item.getProduct().getName());
                    }

                    psItem.setInt(1, generatedOrderId);
                    psItem.setInt(2, prodId);
                    psItem.setInt(3, qty);
                    psItem.setBigDecimal(4, unitPrice);
                    psItem.setBigDecimal(5, lineTotal);
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) {
                conn.rollback();
            }
            throw ex;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}