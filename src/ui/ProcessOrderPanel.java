package ui;

import dao.ClientDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import dao.StockDAO;
import model.Client;
import model.Order;
import model.OrderItem;
import model.Product;
import service.OrderService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;

// Task 5: create a client order, add products and calculate the total
public class ProcessOrderPanel extends JPanel {
    private static final double DELIVERY_FEE = 500.00;
    private static final int FORM_WIDTH = 330;
    private static final int MAX_QUANTITY = 999;

    private final OrderDAO orders = new OrderDAO();
    private final OrderService orderService = new OrderService(orders, new StockDAO());

    private final JTextField txtOrderId = Theme.readOnlyField();
    private final JTextField txtDate = Theme.readOnlyField();
    private final JComboBox<Client> cmbClient = new JComboBox<>();
    private final JComboBox<Product> cmbProduct = new JComboBox<>();
    private final JTextField txtInStock = Theme.readOnlyField();
    private final JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, MAX_QUANTITY, 1));

    private final DefaultTableModel tableModel = Theme.readOnlyModel("Product", "Unit Price", "Qty", "Line Total");
    private final JTable table = new JTable(tableModel);
    private final JLabel lblSubTotal = new JLabel();
    private final JLabel lblDelivery = new JLabel("Delivery: " + Theme.money(DELIVERY_FEE));
    private final JLabel lblTotal = new JLabel();

    private Order cart;

    public ProcessOrderPanel() {
        JPanel content = new JPanel(new BorderLayout(Theme.GAP, 0));
        content.add(buildForm(), BorderLayout.WEST);
        content.add(buildItems(), BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(Theme.screen("New Order", "Task 5", "Place Order saves the order and reduces stock quantity", content));

        for (Client c : new ClientDAO().getAll()) cmbClient.addItem(c);
        for (Product p : new ProductDAO().getAll()) cmbProduct.addItem(p);
        cmbClient.setRenderer(new LabelRenderer<Client>(c -> c.getName() + " (" + c.getType() + ")"));
        cmbProduct.setRenderer(new LabelRenderer<Product>(Product::getName));
        cmbProduct.addActionListener(e -> showStock());
        startNewOrder();
    }

    private JPanel buildForm() {
        JPanel order = Theme.fieldSet("Order");
        Theme.addFormRow(order, 0, "Order ID", txtOrderId);
        Theme.addFormRow(order, 1, "Date", txtDate);
        Theme.addFormRow(order, 2, "Client", cmbClient);

        JButton btnAdd = new StyledButton("Add Item", StyledButton.GREEN);
        JButton btnRemove = new StyledButton("Remove Item", StyledButton.RED);
        btnAdd.addActionListener(e -> addItem());
        btnRemove.addActionListener(e -> removeItem());

        JPanel item = Theme.fieldSet("Add Item");
        Theme.addFormRow(item, 0, "Product", cmbProduct);
        Theme.addFormRow(item, 1, "In Stock", txtInStock);
        Theme.addFormRow(item, 2, "Quantity", spnQuantity);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 3;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(6, 0, 0, 0);
        item.add(Theme.row(FlowLayout.LEFT, btnAdd, btnRemove), c);

        JPanel column = new JPanel(new BorderLayout(0, 10));
        column.setOpaque(false);
        column.add(order, BorderLayout.NORTH);
        column.add(item, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(FORM_WIDTH, 0));
        wrapper.add(column, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel buildItems() {
        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));
        lblTotal.setForeground(Theme.HEADER_GREEN);

        JButton btnCancel = new StyledButton("Cancel", StyledButton.GREY);
        JButton btnPlace = new StyledButton("Place Order", StyledButton.GREEN);
        btnCancel.addActionListener(e -> startNewOrder());
        btnPlace.addActionListener(e -> placeOrder());

        JPanel bottom = new JPanel(new GridLayout(0, 1, 0, 8));
        bottom.setOpaque(false);
        bottom.add(Theme.row(FlowLayout.RIGHT, lblSubTotal, lblDelivery, lblTotal));
        bottom.add(Theme.row(FlowLayout.RIGHT, btnCancel, btnPlace));

        JPanel items = new JPanel(new BorderLayout(0, 10));
        items.setOpaque(false);
        items.add(Theme.table(table, 210, 128, 59, 128), BorderLayout.CENTER);
        items.add(bottom, BorderLayout.SOUTH);
        return items;
    }

    private void startNewOrder() {
        cart = new Order(orders.nextId(), 0, LocalDate.now().toString(), DELIVERY_FEE);
        txtOrderId.setText(cart.getCode());
        txtDate.setText(cart.getOrderDate());
        spnQuantity.setValue(1);
        refreshItems();
        showStock();
    }

    private void showStock() {
        Product p = (Product) cmbProduct.getSelectedItem();
        txtInStock.setText(p == null ? "" : String.valueOf(orderService.availableStock(p.getId())));
    }

    private void addItem() {
        Product p = (Product) cmbProduct.getSelectedItem();
        if (p == null) { Theme.error(this, "Add products in the Products screen first."); return; }
        int quantity = (Integer) spnQuantity.getValue();
        int available = orderService.availableStock(p.getId());
        if (quantity + cart.quantityOf(p.getId()) > available) {
            Theme.error(this, "Only " + available + " units of " + p.getName() + " are in stock.");
            return;
        }
        cart.addItem(new OrderItem(p.getId(), p.getName(), quantity, p.getPrice()));
        spnQuantity.setValue(1);
        refreshItems();
    }

    private void removeItem() {
        int row = table.getSelectedRow();
        if (row < 0) { Theme.error(this, "Select an item in the table to remove."); return; }
        cart.getItems().remove(row);
        refreshItems();
    }

    private void placeOrder() {
        Client client = (Client) cmbClient.getSelectedItem();
        if (client == null) { Theme.error(this, "Add a client in the Clients screen first."); return; }

        Order order = new Order(0, client.getId(), cart.getOrderDate(), DELIVERY_FEE);
        for (OrderItem item : cart.getItems()) order.addItem(item);
        String problem = orderService.placeOrder(order);
        if (problem != null) { Theme.error(this, problem); return; }

        JOptionPane.showMessageDialog(this, "Order " + order.getCode() + " placed.\nTotal: LKR "
                + Theme.money(order.getTotal()), "Order Saved", JOptionPane.INFORMATION_MESSAGE);
        startNewOrder();
    }

    private void refreshItems() {
        tableModel.setRowCount(0);
        for (OrderItem i : cart.getItems())
            tableModel.addRow(new Object[]{i.getProductName(), Theme.money(i.getUnitPrice()),
                    i.getQuantity(), Theme.money(i.getLineTotal())});
        lblSubTotal.setText("Sub Total: " + Theme.money(cart.getSubTotal()));
        lblTotal.setText("Total: LKR " + Theme.money(cart.getItems().isEmpty() ? 0 : cart.getTotal()));
    }
}
