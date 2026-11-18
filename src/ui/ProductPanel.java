package ui;

import dao.ProductDAO;
import model.GardeningKit;
import model.Plant;
import model.Planter;
import model.Product;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

public class ProductPanel extends JPanel {

    private static final Color BACKGROUND = new Color(0xeeeeee);
    private static final Color HEADER_GREEN = new Color(0x2f6b32);
    private static final Color LEGEND_BLUE = new Color(0x2f4f8f);
    private static final Color TEXT = new Color(0x222222);
    private static final Color MUTED_TEXT = new Color(0x555555);
    private static final Color FIELD_TEXT = new Color(0x333333);
    private static final Color FIELD_BORDER = new Color(0x9c9c9c);
    private static final Color FIELDSET_BORDER = new Color(0xb5b5b5);
    private static final Color READ_ONLY_FIELD = new Color(0xf4f4f4);
    private static final Color GRID = new Color(0xdcdcdc);
    private static final Color TABLE_HEADER = new Color(0xe8e8e8);
    private static final Color SELECTED_ROW = new Color(0xb8cfe5);
    private static final Color STATUS_BAR = new Color(0xe4e4e4);
    private static final Color DIVIDER = new Color(0xc8c8c8);
    private static final int LABEL_WIDTH = 110;
    private static final int FIELD_WIDTH = 180;
    private static final int FIELD_HEIGHT = 24;
    private static final int FORM_WIDTH = 330;
    private static final int ROW_HEIGHT = 29;
    private static final int GAP = 14;

    private ProductDAO dao = new ProductDAO();

    // table
    private DefaultTableModel tableModel;
    private JTable table;

    // form fields
    private JTextField txtId = new JTextField();
    private JTextField txtName = new JTextField();
    private JComboBox<String> cmbCategory =
            new JComboBox<>(new String[]{"Plant", "Planter", "Gardening Kit"});
    private JTextField txtPrice = new JTextField();
    private JSpinner spnEco = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
    private JLabel lblDetails = new JLabel("Light level");
    private JTextField txtDetails = new JTextField();
    private JTextField txtSearch = new JTextField();

    // buttons
    private JButton btnAdd = new StyledButton("Add", StyledButton.GREEN);
    private JButton btnUpdate = new StyledButton("Update", StyledButton.GREY);
    private JButton btnRemove = new StyledButton("Remove", StyledButton.RED);
    private JButton btnClear = new StyledButton("Clear", StyledButton.GREY);
    private JButton btnSearch = new StyledButton("Search", StyledButton.GREY);

    public ProductPanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);

        JPanel content = new JPanel(new BorderLayout(GAP, 0));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        content.add(buildForm(), BorderLayout.WEST);
        content.add(buildTable(), BorderLayout.CENTER);

        add(buildHeader(), BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        txtId.setEditable(false);   // the id is generated automatically

        // clicking a row fills the form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromSelection();
            }
        });

        // changing the category changes the label of the last field
        cmbCategory.addActionListener(e -> updateDetailsLabel());

        // buttons
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnRemove.addActionListener(e -> removeProduct());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> searchProducts());
        txtSearch.addActionListener(e -> searchProducts());   // Enter key

        refreshTable(dao.getAll());
    }

    private JPanel buildHeader() {
        JLabel title = new JLabel("Manage Products");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_GREEN);
        header.setBorder(BorderFactory.createEmptyBorder(10, GAP, 10, GAP));
        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel buildStatusBar() {
        JLabel hint = new JLabel("Click a table row to load it into the form");
        hint.setFont(hint.getFont().deriveFont(11f));
        hint.setForeground(MUTED_TEXT);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(STATUS_BAR);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, DIVIDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        bar.add(hint, BorderLayout.WEST);
        return bar;
    }

    private JScrollPane buildTable() {
        String[] columns = {"ID", "Name", "Category", "Price", "Eco", "Details"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // user can't type directly in the table
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(ROW_HEIGHT);
        table.setFont(table.getFont().deriveFont(13f));
        table.setForeground(TEXT);
        table.setGridColor(GRID);
        table.setShowGrid(true);
        table.setSelectionBackground(SELECTED_ROW);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = table.getTableHeader();
        header.setFont(header.getFont().deriveFont(13f));
        header.setBackground(TABLE_HEADER);
        header.setForeground(TEXT);
        header.setPreferredSize(new Dimension(0, ROW_HEIGHT));
        header.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(FIELD_BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel buildForm() {
        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        for (JComponent part : new JComponent[]{buildFieldSet(), buildButtonRow(), buildSearchRow()}) {
            part.setAlignmentX(LEFT_ALIGNMENT);
            column.add(part);
            column.add(Box.createVerticalStrut(10));
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(FORM_WIDTH, 0));
        wrapper.add(column, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel buildFieldSet() {
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        TitledBorder legend = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(FIELDSET_BORDER), "Product Details");
        legend.setTitleColor(LEGEND_BLUE);
        fields.setBorder(BorderFactory.createCompoundBorder(
                legend, BorderFactory.createEmptyBorder(4, 8, 8, 8)));

        addFormRow(fields, 0, new JLabel("Product ID"), txtId);
        addFormRow(fields, 1, new JLabel("Name"), txtName);
        addFormRow(fields, 2, new JLabel("Category"), cmbCategory);
        addFormRow(fields, 3, new JLabel("Price (LKR)"), txtPrice);
        addFormRow(fields, 4, new JLabel("Eco-Rating (1-5)"), spnEco);
        addFormRow(fields, 5, lblDetails, txtDetails);

        styleField(txtId);
        txtId.setBackground(READ_ONLY_FIELD);
        txtId.setForeground(MUTED_TEXT);
        styleField(txtName);
        styleField(txtPrice);
        styleField(txtDetails);
        cmbCategory.setBackground(Color.WHITE);
        return fields;
    }

    private JPanel buildButtonRow() {
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        for (JButton button : new JButton[]{btnAdd, btnUpdate, btnRemove, btnClear}) {
            buttons.add(button);
            buttons.add(Box.createHorizontalStrut(8));
        }
        return buttons;
    }

    private JPanel buildSearchRow() {
        styleField(txtSearch);
        JLabel label = new JLabel("Search");
        label.setForeground(TEXT);

        JPanel search = new JPanel(new BorderLayout(8, 0));
        search.setOpaque(false);
        search.add(label, BorderLayout.WEST);
        search.add(txtSearch, BorderLayout.CENTER);
        search.add(btnSearch, BorderLayout.EAST);
        return search;
    }

    private void addFormRow(JPanel panel, int row, JLabel label, JComponent field) {
        label.setForeground(TEXT);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, FIELD_HEIGHT));
        field.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(4, 0, 4, 8);
        c.anchor = GridBagConstraints.WEST;
        panel.add(label, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 0, 4, 0);
        panel.add(field, c);
    }

    private void styleField(JTextField field) {
        field.setForeground(FIELD_TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(0, 6, 0, 6)));
    }

    private void refreshTable(List<Product> list) {
        tableModel.setRowCount(0);   // clear the table
        for (Product p : list) {
            tableModel.addRow(new Object[]{
                    p.getId(), p.getName(), p.getCategory(),
                    String.format("%.2f", p.getPrice()),
                    p.getEcoRating(), p.getDetails()
            });
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;   // nothing selected

        Product p = dao.findById((String) tableModel.getValueAt(row, 0));
        if (p == null) return;

        txtId.setText(p.getId());
        txtName.setText(p.getName());
        cmbCategory.setSelectedItem(p.getCategory());
        txtPrice.setText(String.valueOf(p.getPrice()));
        spnEco.setValue(p.getEcoRating());
        txtDetails.setText(p.getDetails());
        lblDetails.setText(p.getDetailsLabel().replace(":", ""));   // form labels have no colons
    }

    private void updateDetailsLabel() {
        String category = (String) cmbCategory.getSelectedItem();
        if ("Plant".equals(category)) {
            lblDetails.setText("Light level");
        } else if ("Planter".equals(category)) {
            lblDetails.setText("Material");
        } else {
            lblDetails.setText("No. of items");
        }
    }

    private void addProduct() {
        try {
            Product p = readForm(dao.nextId());
            dao.add(p);
            refreshTable(dao.getAll());
            clearForm();
            JOptionPane.showMessageDialog(this, "Product " + p.getId() + " added.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateProduct() {
        if (txtId.getText().isEmpty()) {
            showError("Click a product in the table first.");
            return;
        }
        try {
            Product p = readForm(txtId.getText());
            dao.update(p);
            refreshTable(dao.getAll());
            clearForm();
            JOptionPane.showMessageDialog(this, "Product " + p.getId() + " updated.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void removeProduct() {
        if (txtId.getText().isEmpty()) {
            showError("Click a product in the table first.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Remove " + txtId.getText() + " (" + txtName.getText() + ")?",
                "Confirm removal", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dao.remove(txtId.getText());
            refreshTable(dao.getAll());
            clearForm();
        }
    }

    private void searchProducts() {
        refreshTable(dao.search(txtSearch.getText()));
    }

    private void clearForm() {
        table.clearSelection();
        txtId.setText("");
        txtName.setText("");
        cmbCategory.setSelectedIndex(0);
        txtPrice.setText("");
        spnEco.setValue(3);
        txtDetails.setText("");
        updateDetailsLabel();
    }

    // Reads the form and builds the right kind of product
    private Product readForm(String id) {
        String name = txtName.getText();
        String category = (String) cmbCategory.getSelectedItem();
        String details = txtDetails.getText().trim();
        int eco = (Integer) spnEco.getValue();

        double price;
        try {
            price = Double.parseDouble(txtPrice.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Price must be a number.");
        }

        if (details.isEmpty()) {
            throw new IllegalArgumentException("Please fill in the last field (" + lblDetails.getText() + ")");
        }

        if (category.equals("Plant")) {
            return new Plant(id, name, price, eco, details);
        } else if (category.equals("Planter")) {
            return new Planter(id, name, price, eco, details);
        } else {
            int count;
            try {
                count = Integer.parseInt(details);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("No. of items must be a whole number.");
            }
            return new GardeningKit(id, name, price, eco, count);
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid input", JOptionPane.ERROR_MESSAGE);
    }
}