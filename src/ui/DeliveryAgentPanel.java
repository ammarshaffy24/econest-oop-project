package ui;

import dao.DeliveryAgentDAO;
import model.DeliveryAgent;
import util.Validator;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

// Task 4: Manage Delivery Agents screen (add, update, remove, search)
public class DeliveryAgentPanel extends JPanel {
    private final DeliveryAgentDAO dao = new DeliveryAgentDAO();

    private final JTextField name = new JTextField(15), phone = new JTextField(15),
            email = new JTextField(15), vehicleNo = new JTextField(15), search = new JTextField(15);
    private final JComboBox<String> vehicleType =
            new JComboBox<>(new String[]{"Motorbike", "Three-wheeler", "Van", "Lorry"});
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Phone", "Email", "Vehicle Type", "Vehicle No"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public DeliveryAgentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // form
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Name"));         form.add(name);
        form.add(new JLabel("Phone"));        form.add(phone);
        form.add(new JLabel("Email"));        form.add(email);
        form.add(new JLabel("Vehicle Type")); form.add(vehicleType);
        form.add(new JLabel("Vehicle No"));   form.add(vehicleNo);

        // buttons
        JButton add = new JButton("Add"), upd = new JButton("Update"),
                del = new JButton("Remove"), clr = new JButton("Clear");
        JPanel buttons = new JPanel();
        buttons.add(add); buttons.add(upd); buttons.add(del); buttons.add(clr);

        JPanel top = new JPanel(new BorderLayout(5, 5));
        top.add(form, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        // search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search name:"));
        searchBar.add(search);

        JPanel centre = new JPanel(new BorderLayout(5, 5));
        centre.add(searchBar, BorderLayout.NORTH);
        centre.add(new JScrollPane(table), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);

        // events
        add.addActionListener(e -> addAgent());
        upd.addActionListener(e -> updateAgent());
        del.addActionListener(e -> removeAgent());
        clr.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> fillForm());
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });

        refresh();
    }

    private void addAgent() {
        if (!valid()) return;
        dao.add(readForm(0));
        refresh();
        clearForm();
    }

    private void updateAgent() {
        int id = selectedId();
        if (id < 0) { error("Select an agent from the table first."); return; }
        if (!valid()) return;
        dao.update(readForm(id));
        refresh();
        clearForm();
    }

    private void removeAgent() {
        int id = selectedId();
        if (id < 0) { error("Select an agent from the table first."); return; }
        if (JOptionPane.showConfirmDialog(this, "Remove this delivery agent?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dao.remove(id);
            refresh();
            clearForm();
        }
    }

    private DeliveryAgent readForm(int id) {
        return new DeliveryAgent(id, name.getText().trim(), phone.getText().trim(),
                email.getText().trim(), (String) vehicleType.getSelectedItem(),
                vehicleNo.getText().trim());
    }

    private boolean valid() {
        String msg = Validator.check(name.getText(), phone.getText(), email.getText(),
                vehicleNo.getText());
        if (msg != null) error(msg);
        return msg == null;
    }

    private void fillForm() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        name.setText((String) model.getValueAt(r, 1));
        phone.setText((String) model.getValueAt(r, 2));
        email.setText((String) model.getValueAt(r, 3));
        vehicleType.setSelectedItem(model.getValueAt(r, 4));
        vehicleNo.setText((String) model.getValueAt(r, 5));
    }

    private void clearForm() {
        name.setText(""); phone.setText(""); email.setText(""); vehicleNo.setText("");
        vehicleType.setSelectedIndex(0);
        table.clearSelection();
    }

    private int selectedId() {
        int r = table.getSelectedRow();
        return r < 0 ? -1 : (int) model.getValueAt(r, 0);
    }

    private void refresh() {
        model.setRowCount(0);
        String key = search.getText().trim().toLowerCase();
        for (DeliveryAgent a : dao.getAll())
            if (a.getName().toLowerCase().contains(key))
                model.addRow(new Object[]{a.getId(), a.getName(), a.getPhone(),
                        a.getEmail(), a.getVehicleType(), a.getVehicleNo()});
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
