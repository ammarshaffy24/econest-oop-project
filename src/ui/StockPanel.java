package ui;

import dao.ProductDAO;
import dao.StockDAO;
import model.Product;
import model.StockItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Task 3 - Manage Stock / Inventory.
 * Shows quantity on hand, reorder level, supplier and low-stock status.
 */
public class StockPanel extends JPanel {

    private final StockDAO stockDao = new StockDAO();

    // table
    private DefaultTableModel tableModel;
    private JTable table;
    private final JCheckBox chkLowOnly = new JCheckBox("Show low stock only");
    private final JLabel lblAlert = new JLabel(" ");

    // form
    private final JComboBox<ProductOption> cmbProduct = new JComboBox<>();
    private final JSpinner spnQty = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
    private final JSpinner spnReorder = new JSpinner(new SpinnerNumberModel(5, 0, 100000, 1));
    private final JTextField txtSupplier = new JTextField();

    // buttons
    private final JButton btnStockIn = new JButton("Stock In");
    private final JButton btnReorder = new JButton("Update Reorder Level");
    private final JButton btnClear = new JButton("Clear");
    private final JButton btnRefresh = new JButton("Refresh");

    public StockPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);

        wireEvents();
        refresh();
    }

    // ================= UI building =================

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        lblAlert.setFont(lblAlert.getFont().deriveFont(Font.BOLD));
        lblAlert.setForeground(new Color(180, 30, 30));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(chkLowOnly);
        right.add(btnRefresh);
        top.add(lblAlert, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JScrollPane buildTable() {
        String[] cols = {"Product ID", "Name", "Qty on Hand", "Reorder Level",
                "Supplier", "Last Stock-In", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // highlight LOW rows in red
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean selected, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                boolean low = "LOW".equals(t.getValueAt(row, 6));
                if (!selected) {
                    c.setBackground(low ? new Color(255, 220, 220) : t.getBackground());
                    c.setForeground(low ? new Color(150, 0, 0) : t.getForeground());
                }
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        return new JScrollPane(table);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Stock Details"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, g, 0, "Product:", cmbProduct, "Stock-in Quantity:", spnQty);
        addRow(form, g, 1, "Reorder Level:", spnReorder, "Supplier:", txtSupplier);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(btnStockIn);
        buttons.add(btnReorder);
        buttons.add(btnClear);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 4; g.weightx = 1;
        form.add(buttons, g);
        return form;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row,
                        String l1, JComponent c1, String l2, JComponent c2) {
        g.gridy = row; g.gridwidth = 1;
        g.gridx = 0; g.weightx = 0; p.add(new JLabel(l1), g);
        g.gridx = 1; g.weightx = 1; p.add(c1, g);
        g.gridx = 2; g.weightx = 0; p.add(new JLabel(l2), g);
        g.gridx = 3; g.weightx = 1; p.add(c2, g);
    }

    // ================= events =================

    private void wireEvents() {
        btnRefresh.addActionListener(e -> refresh());
        chkLowOnly.addActionListener(e -> reloadTable());
        btnClear.addActionListener(e -> clearForm());
        btnStockIn.addActionListener(e -> doStockIn());
        btnReorder.addActionListener(e -> doUpdateReorder());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });
    }

    private void doStockIn() {
        ProductOption opt = (ProductOption) cmbProduct.getSelectedItem();
        if (opt == null) {
            warn("Please select a product.");
            return;
        }
        int qty = (Integer) spnQty.getValue();
        if (stockDao.stockIn(opt.id, qty, txtSupplier.getText())) {
            reloadTable();
            JOptionPane.showMessageDialog(this,
                    "Added " + qty + " units to " + opt.name + ".");
        } else {
            warn("Stock-in failed. Check the quantity.");
        }
    }

    private void doUpdateReorder() {
        ProductOption opt = (ProductOption) cmbProduct.getSelectedItem();
        if (opt == null) {
            warn("Please select a product.");
            return;
        }
        int level = (Integer) spnReorder.getValue();
        if (stockDao.updateReorderSettings(opt.id, level, txtSupplier.getText())) {
            reloadTable();
        } else {
            warn("Could not update the reorder level.");
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String id = String.valueOf(tableModel.getValueAt(row, 0));
        for (int i = 0; i < cmbProduct.getItemCount(); i++) {
            if (cmbProduct.getItemAt(i).id.equals(id)) {
                cmbProduct.setSelectedIndex(i);
                break;
            }
        }
        spnReorder.setValue(tableModel.getValueAt(row, 3));
        txtSupplier.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        spnQty.setValue(1);
    }

    private void clearForm() {
        table.clearSelection();
        if (cmbProduct.getItemCount() > 0) cmbProduct.setSelectedIndex(0);
        spnQty.setValue(1);
        spnReorder.setValue(5);
        txtSupplier.setText("");
    }

    // ================= data =================

    /**
     * Re-reads products from Task 1, creates stock records for new products,
     * removes records for deleted products, and redraws the table.
     * Call this whenever the Stock tab is opened.
     */
    public void refresh() {
        List<Product> products = loadProducts();

        List<String> ids = new ArrayList<>();
        ProductOption selected = (ProductOption) cmbProduct.getSelectedItem();
        cmbProduct.removeAllItems();
        for (Product p : products) {
            String id = String.valueOf(p.getId());
            String name = p.getName();
            ids.add(id);
            stockDao.ensureRecord(id, name);
            cmbProduct.addItem(new ProductOption(id, name));
        }
        stockDao.removeMissing(ids);

        if (selected != null) {
            for (int i = 0; i < cmbProduct.getItemCount(); i++) {
                if (cmbProduct.getItemAt(i).id.equals(selected.id)) {
                    cmbProduct.setSelectedIndex(i);
                    break;
                }
            }
        }
        reloadTable();
    }

    /**
     * ONE PLACE that depends on your Task 1 code.
     * If your ProductDAO method is not called getAll(), change it here.
     */
    private List<Product> loadProducts() {
        return new ProductDAO().getAll();   // re-reads products.txt every time
    }

    private void reloadTable() {
        tableModel.setRowCount(0);
        List<StockItem> list = chkLowOnly.isSelected()
                ? stockDao.getLowStockItems() : stockDao.getAll();
        for (StockItem s : list) {
            tableModel.addRow(new Object[]{
                    s.getProductId(), s.getProductName(), s.getQuantityOnHand(),
                    s.getReorderLevel(), s.getSupplier(), s.getLastStockIn(),
                    s.isLowStock() ? "LOW" : "OK"});
        }
        int lowCount = stockDao.getLowStockItems().size();
        lblAlert.setText(lowCount > 0 ? "\u26A0 " + lowCount + " item(s) at or below reorder level" : " ");
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Stock", JOptionPane.WARNING_MESSAGE);
    }

    // combo box item
    private static class ProductOption {
        final String id;
        final String name;
        ProductOption(String id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return id + " - " + name; }
    }
}
