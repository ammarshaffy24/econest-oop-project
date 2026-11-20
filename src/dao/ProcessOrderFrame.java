import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class ProcessOrderFrame extends JFrame {
    private final OrderDAO orderDAO = new OrderDAO();
    private final List<OrderItem> currentItems = new ArrayList<>();
    private final DecimalFormat currencyFmt = new DecimalFormat("#,##0.00");

    private JTextField txtOrderId;
    private JTextField txtDate;
    private JComboBox<Client> cmbClient;
    private JComboBox<Product> cmbProduct;
    private JTextField txtUnitPrice;
    private JSpinner spnQty;
    private JTextField txtLineTotal;
    private DefaultTableModel tableModel;
    private JTable tblItems;
    private JLabel lblTotal;

    public ProcessOrderFrame() {
        setTitle("EcoNest - Process Order (Task 5)");
        setSize(920, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        initComponents();
        loadInitialData();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(27, 94, 32));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel lblTitle = new JLabel("New Order");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerPanel.add(lblTitle, BorderLayout.WEST);
        root.add(headerPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(330);

        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Order & Item Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtOrderId = new JTextField();
        txtOrderId.setEditable(false);

        txtDate = new JTextField(LocalDate.now().toString());
        txtDate.setEditable(false);

        cmbClient = new JComboBox<>();
        cmbProduct = new JComboBox<>();

        txtUnitPrice = new JTextField("0.00");
        txtUnitPrice.setEditable(false);

        spnQty = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));

        txtLineTotal = new JTextField("0.00");
        txtLineTotal.setEditable(false);

        int row = 0;
        addField(leftPanel, gbc, row++, "Order ID:", txtOrderId);
        addField(leftPanel, gbc, row++, "Date:", txtDate);
        addField(leftPanel, gbc, row++, "Client:", cmbClient);
        addField(leftPanel, gbc, row++, "Product:", cmbProduct);
        addField(leftPanel, gbc, row++, "Unit Price (LKR):", txtUnitPrice);
        addField(leftPanel, gbc, row++, "Quantity:", spnQty);
        addField(leftPanel, gbc, row++, "Line Total (LKR):", txtLineTotal);

        JButton btnAddItem = new JButton("Add Item");
        JButton btnRemoveItem = new JButton("Remove Item");
        btnAddItem.setBackground(new Color(46, 125, 50));
        btnAddItem.setForeground(Color.WHITE);
        btnRemoveItem.setBackground(new Color(198, 40, 40));
        btnRemoveItem.setForeground(Color.WHITE);

        JPanel itemBtnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        itemBtnPanel.add(btnAddItem);
        itemBtnPanel.add(btnRemoveItem);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        leftPanel.add(itemBtnPanel, gbc);

        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Order Items"));

        String[] columns = {"Product", "Unit Price", "Qty", "Line Total"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblItems = new JTable(tableModel);
        tblItems.setRowHeight(24);
        rightPanel.add(new JScrollPane(tblItems), BorderLayout.CENTER);

        JPanel bottomRight = new JPanel(new BorderLayout(8, 8));
        lblTotal = new JLabel("Total: LKR 0.00", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        bottomRight.add(lblTotal, BorderLayout.NORTH);

        JButton btnClear = new JButton("Clear");
        JButton btnSaveOrder = new JButton("Save Order");
        btnSaveOrder.setBackground(new Color(27, 94, 32));
        btnSaveOrder.setForeground(Color.WHITE);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.add(btnClear);
        actionPanel.add(btnSaveOrder);
        bottomRight.add(actionPanel, BorderLayout.SOUTH);

        rightPanel.add(bottomRight, BorderLayout.SOUTH);

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        root.add(splitPane, BorderLayout.CENTER);

        setContentPane(root);

        cmbProduct.addActionListener(e -> updatePreviewPrices());
        spnQty.addChangeListener(e -> updatePreviewPrices());
        btnAddItem.addActionListener(e -> handleAddItem());
        btnRemoveItem.addActionListener(e -> handleRemoveItem());
        btnClear.addActionListener(e -> clearOrderForm());
        btnSaveOrder.addActionListener(e -> handleSaveOrder());
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component field) {
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    private void loadInitialData() {
        try {
            txtOrderId.setText(orderDAO.generateNextOrderCode());
            txtDate.setText(LocalDate.now().toString());

            cmbClient.removeAllItems();
            for (Client c : orderDAO.getAllClients()) {
                cmbClient.addItem(c);
            }

            cmbProduct.removeAllItems();
            for (Product p : orderDAO.getAllProducts()) {
                cmbProduct.addItem(p);
            }

            updatePreviewPrices();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updatePreviewPrices() {
        Product selected = (Product) cmbProduct.getSelectedItem();
        if (selected == null) {
            txtUnitPrice.setText("0.00");
            txtLineTotal.setText("0.00");
            return;
        }
        int qty = (Integer) spnQty.getValue();
        BigDecimal price = selected.getUnitPrice();
        BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

        txtUnitPrice.setText(currencyFmt.format(price));
        txtLineTotal.setText(currencyFmt.format(lineTotal));
    }

    private void handleAddItem() {
        Product selected = (Product) cmbProduct.getSelectedItem();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a product.");
            return;
        }
        int qty = (Integer) spnQty.getValue();
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
            return;
        }

        try {
            int available = orderDAO.getAvailableStock(selected.getId());
            int alreadyInCart = 0;
            for (OrderItem oi : currentItems) {
                if (oi.getProduct().getId() == selected.getId()) {
                    alreadyInCart += oi.getQuantity();
                }
            }
            if (qty + alreadyInCart > available) {
                JOptionPane.showMessageDialog(this,
                    "Only " + available + " units available in stock for " + selected.getName() + ".",
                    "Stock Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Stock check failed: " + ex.getMessage());
            return;
        }

        OrderItem item = new OrderItem(selected, qty);
        currentItems.add(item);
        refreshTableAndTotal();
        spnQty.setValue(1);
    }

    private void handleRemoveItem() {
        int selectedRow = tblItems.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a row from the order table to remove.");
            return;
        }
        currentItems.remove(selectedRow);
        refreshTableAndTotal();
    }

    private void refreshTableAndTotal() {
        tableModel.setRowCount(0);
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (OrderItem item : currentItems) {
            tableModel.addRow(new Object[]{
                item.getProduct().getName(),
                currencyFmt.format(item.getUnitPrice()),
                item.getQuantity(),
                currencyFmt.format(item.getLineTotal())
            });
            grandTotal = grandTotal.add(item.getLineTotal());
        }

        lblTotal.setText("Total: LKR " + currencyFmt.format(grandTotal));
    }

    private void clearOrderForm() {
        currentItems.clear();
        refreshTableAndTotal();
        spnQty.setValue(1);
        try {
            txtOrderId.setText(orderDAO.generateNextOrderCode());
            txtDate.setText(LocalDate.now().toString());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to refresh Order ID: " + ex.getMessage());
        }
    }

    private void handleSaveOrder() {
        Client client = (Client) cmbClient.getSelectedItem();
        if (client == null) {
            JOptionPane.showMessageDialog(this, "Please select a client.");
            return;
        }
        if (currentItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one product before saving the order.");
            return;
        }

        Order order = new Order(txtOrderId.getText(), client, LocalDate.now());
        for (OrderItem item : currentItems) {
            order.addItem(item);
        }

        try {
            boolean saved = orderDAO.saveOrder(order);
            if (saved) {
                JOptionPane.showMessageDialog(this,
                    "Order " + order.getOrderCode() + " saved successfully!\nTotal: LKR " + currencyFmt.format(order.getTotalAmount()),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearOrderForm();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving order: " + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProcessOrderFrame().setVisible(true));
    }
}