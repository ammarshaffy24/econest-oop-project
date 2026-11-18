package ui;

import dao.ProductDAO;
import model.GardeningKit;
import model.Plant;
import model.Planter;
import model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductPanel extends JPanel {

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
    private JLabel lblDetails = new JLabel("Light level:");
    private JTextField txtDetails = new JTextField();

    // buttons
    private JButton btnAdd = new JButton("Add");
    private JButton btnUpdate = new JButton("Update");
    private JButton btnRemove = new JButton("Remove");
    private JButton btnClear = new JButton("Clear");

    public ProductPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);

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

        refreshTable(dao.getAll());
    }

    private JScrollPane buildTable() {
        String[] columns = {"ID", "Name", "Category", "Price (Rs.)", "Eco Rating", "Details"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;   // user can't type directly in the table
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(24);
        return new JScrollPane(table);
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridLayout(3, 4, 8, 8));
        form.add(new JLabel("Product ID:"));       form.add(txtId);
        form.add(new JLabel("Name:"));             form.add(txtName);
        form.add(new JLabel("Category:"));         form.add(cmbCategory);
        form.add(new JLabel("Price (Rs.):"));      form.add(txtPrice);
        form.add(new JLabel("Eco rating (1-5):")); form.add(spnEco);
        form.add(lblDetails);                      form.add(txtDetails);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnRemove);
        buttons.add(btnClear);

        JPanel south = new JPanel(new BorderLayout(5, 5));
        south.setBorder(BorderFactory.createTitledBorder("Product details"));
        south.add(form, BorderLayout.CENTER);
        south.add(buttons, BorderLayout.SOUTH);
        return south;
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
        lblDetails.setText(p.getDetailsLabel());
    }

    private void updateDetailsLabel() {
        String category = (String) cmbCategory.getSelectedItem();
        if ("Plant".equals(category)) {
            lblDetails.setText("Light level:");
        } else if ("Planter".equals(category)) {
            lblDetails.setText("Material:");
        } else {
            lblDetails.setText("No. of items:");
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