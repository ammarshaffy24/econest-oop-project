package ui;

import dao.ClientDAO;
import model.Client;
import util.Validator;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

// Task 2: Manage Clients screen (add, update, remove, search)
public class ClientPanel extends JPanel {
    private final ClientDAO dao = new ClientDAO();

    private final JTextField name = new JTextField(15), phone = new JTextField(15),
            email = new JTextField(15), address = new JTextField(15), search = new JTextField(15);
    private final JComboBox<String> type = new JComboBox<>(new String[]{"Household", "Office"});
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Name", "Type", "Phone", "Email", "Address"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public ClientPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // form
        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Name"));    form.add(name);
        form.add(new JLabel("Type"));    form.add(type);
        form.add(new JLabel("Phone"));   form.add(phone);
        form.add(new JLabel("Email"));   form.add(email);
        form.add(new JLabel("Address")); form.add(address);

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
        add.addActionListener(e -> addClient());
        upd.addActionListener(e -> updateClient());
        del.addActionListener(e -> removeClient());
        clr.addActionListener(e -> clearForm());
        table.getSelectionModel().addListSelectionListener(e -> fillForm());
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });

        refresh();
    }

    private void addClient() {
        if (!valid()) return;
        dao.add(readForm(0));
        refresh();
        clearForm();
    }

    private void updateClient() {
        int id = selectedId();
        if (id < 0) { error("Select a client from the table first."); return; }
        if (!valid()) return;
        dao.update(readForm(id));
        refresh();
        clearForm();
    }

    private void removeClient() {
        int id = selectedId();
        if (id < 0) { error("Select a client from the table first."); return; }
        if (JOptionPane.showConfirmDialog(this, "Remove this client?", "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dao.remove(id);
            refresh();
            clearForm();
        }
    }

    private Client readForm(int id) {
        return new Client(id, name.getText().trim(), phone.getText().trim(),
                email.getText().trim(), (String) type.getSelectedItem(), address.getText().trim());
    }

    private boolean valid() {
        String msg = Validator.check(name.getText(), phone.getText(), email.getText(),
                address.getText());
        if (msg != null) error(msg);
        return msg == null;
    }

    private void fillForm() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        name.setText((String) model.getValueAt(r, 1));
        type.setSelectedItem(model.getValueAt(r, 2));
        phone.setText((String) model.getValueAt(r, 3));
        email.setText((String) model.getValueAt(r, 4));
        address.setText((String) model.getValueAt(r, 5));
    }

    private void clearForm() {
        name.setText(""); phone.setText(""); email.setText(""); address.setText("");
        type.setSelectedIndex(0);
        table.clearSelection();
    }

    private int selectedId() {
        int r = table.getSelectedRow();
        return r < 0 ? -1 : (int) model.getValueAt(r, 0);
    }

    private void refresh() {
        model.setRowCount(0);
        String key = search.getText().trim().toLowerCase();
        for (Client c : dao.getAll())
            if (c.getName().toLowerCase().contains(key))
                model.addRow(new Object[]{c.getId(), c.getName(), c.getType(),
                        c.getPhone(), c.getEmail(), c.getAddress()});
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
