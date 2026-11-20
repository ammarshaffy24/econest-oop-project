package ui;

import dao.ClientDAO;
import dao.DeliveryAgentDAO;
import dao.OrderDAO;
import model.Client;
import model.DeliveryAgent;
import model.Order;
import service.DeliveryNotifier;
import service.DispatchNotifier;
import service.EmailService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

// Task 6: assign delivery agents to orders and track delivery status (sends the Task 8 & 9 emails)
public class AssignDeliveryPanel extends JPanel {
    private static final int FORM_WIDTH = 310;
    private static final String NONE = "-";

    private final OrderDAO orders = new OrderDAO();
    private final ClientDAO clients = new ClientDAO();
    private final DeliveryAgentDAO agents = new DeliveryAgentDAO();
    private final EmailService emailService = new EmailService();
    private final DeliveryNotifier agentNotifier = new DeliveryNotifier(emailService);
    private final DispatchNotifier clientNotifier = new DispatchNotifier(emailService);

    private final DefaultTableModel tableModel = Theme.readOnlyModel("Order", "Client", "Total", "Agent", "Date", "Status");
    private final JTable table = new JTable(tableModel);
    private final JTextField txtOrder = Theme.readOnlyField();
    private final JComboBox<DeliveryAgent> cmbAgent = new JComboBox<>();
    private final JTextField txtDate = new JTextField(LocalDate.now().toString());
    private final JComboBox<String> cmbStatus =
            new JComboBox<>(new String[]{Order.ASSIGNED, Order.DISPATCHED, Order.DELIVERED});
    private final JCheckBox chkEmailAgent = new JCheckBox("Email agent on assign", true);
    private final JCheckBox chkEmailClient = new JCheckBox("Email client on dispatch", true);

    public AssignDeliveryPanel() {
        JPanel content = new JPanel(new BorderLayout(Theme.GAP, 0));
        content.add(Theme.table(table, 76, 129, 90, 69, 82, 99), BorderLayout.CENTER);
        content.add(buildForm(), BorderLayout.EAST);
        setLayout(new BorderLayout());
        add(Theme.screen("Delivery Scheduling", "Task 6 · triggers emails (Tasks 8 & 9)",
                "Status: Pending → Assigned → Dispatched → Delivered", content));

        for (DeliveryAgent a : agents.getAll()) cmbAgent.addItem(a);
        cmbAgent.setRenderer(new LabelRenderer<DeliveryAgent>(
                a -> a.getName() + " (" + a.getVehicleType() + " " + a.getVehicleNo() + ")"));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });
        refreshTable();
    }

    private JPanel buildForm() {
        JPanel fields = Theme.fieldSet("Assign Selected Order");
        Theme.addFormRow(fields, 0, "Order", txtOrder);
        Theme.addFormRow(fields, 1, "Agent", cmbAgent);
        Theme.addFormRow(fields, 2, "Delivery Date", txtDate);
        Theme.addFormRow(fields, 3, "Status", cmbStatus);
        addCheckBox(fields, 4, chkEmailAgent);
        addCheckBox(fields, 5, chkEmailClient);

        JButton btnAssign = new StyledButton("Assign", StyledButton.GREEN);
        JButton btnUpdate = new StyledButton("Update Status", StyledButton.GREY);
        btnAssign.addActionListener(e -> assignAgent());
        btnUpdate.addActionListener(e -> updateStatus());

        JPanel column = new JPanel(new BorderLayout(0, 10));
        column.setOpaque(false);
        column.add(fields, BorderLayout.NORTH);
        column.add(Theme.row(FlowLayout.LEFT, btnAssign, btnUpdate), BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(FORM_WIDTH, 0));
        wrapper.add(column, BorderLayout.NORTH);
        return wrapper;
    }

    private static void addCheckBox(JPanel fields, int row, JCheckBox box) {
        box.setOpaque(false);
        box.setForeground(Theme.TEXT);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        fields.add(box, c);
    }

    private void assignAgent() {
        Order order = selectedOrder();
        if (order == null) { Theme.error(this, "Select an order from the table first."); return; }
        DeliveryAgent agent = (DeliveryAgent) cmbAgent.getSelectedItem();
        if (agent == null) { Theme.error(this, "Add a delivery agent first (Delivery Agents screen)."); return; }
        if (!Order.PENDING.equals(order.getStatus()) && !Order.ASSIGNED.equals(order.getStatus())) {
            Theme.error(this, "This order has already been " + order.getStatus().toLowerCase() + ".");
            return;
        }
        String date = txtDate.getText().trim();
        if (!isValidDate(date)) { Theme.error(this, "Delivery date must be a real date in the format YYYY-MM-DD."); return; }

        order.assign(agent.getId(), date);
        orders.save();
        String emailNote = chkEmailAgent.isSelected() ? sendAgentEmail(order, agent) : "";
        refreshAndReselect(order, emailNote);
    }

    private void updateStatus() {
        Order order = selectedOrder();
        if (order == null) { Theme.error(this, "Select an order from the table first."); return; }
        if (!order.hasAgent()) { Theme.error(this, "Assign a delivery agent before changing the status."); return; }
        String newStatus = (String) cmbStatus.getSelectedItem();
        if (newStatus.equals(order.getStatus())) { Theme.error(this, "The order is already " + newStatus + "."); return; }

        order.setStatus(newStatus);
        orders.save();
        boolean emailClient = Order.DISPATCHED.equals(newStatus) && chkEmailClient.isSelected();
        refreshAndReselect(order, emailClient ? sendClientEmail(order) : "");
    }

    private String sendAgentEmail(Order order, DeliveryAgent agent) {
        Client client = clients.findById(order.getClientId());
        if (client == null) return "\nNo email sent: the client no longer exists.";
        try {
            agentNotifier.notifyAgent(agent, client, order);
            return "\nEmail sent to " + agent.getEmail() + ".";
        } catch (RuntimeException e) {
            return "\nEmail to agent failed: " + e.getMessage();
        }
    }

    private String sendClientEmail(Order order) {
        Client client = clients.findById(order.getClientId());
        DeliveryAgent agent = agents.findById(order.getAgentId());
        if (client == null || agent == null) return "\nNo email sent: the client or agent no longer exists.";
        try {
            clientNotifier.notifyClient(client, agent, order);
            return "\nEmail sent to " + client.getEmail() + ".";
        } catch (RuntimeException e) {
            return "\nEmail to client failed: " + e.getMessage();
        }
    }

    private void refreshAndReselect(Order order, String emailNote) {
        int row = table.getSelectedRow();
        refreshTable();
        table.setRowSelectionInterval(row, row);
        JOptionPane.showMessageDialog(this, order.getCode() + " is now " + order.getStatus() + "." + emailNote,
                "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void fillFormFromSelection() {
        Order order = selectedOrder();
        if (order == null) return;
        txtOrder.setText(order.getCode());
        if (order.hasAgent()) {
            cmbAgent.setSelectedItem(agents.findById(order.getAgentId()));
            txtDate.setText(order.getDeliveryDate());
            cmbStatus.setSelectedItem(order.getStatus());
        } else {
            txtDate.setText(LocalDate.now().toString());
            cmbStatus.setSelectedItem(Order.ASSIGNED);
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Order o : orders.getAll()) {
            Client client = clients.findById(o.getClientId());
            DeliveryAgent agent = agents.findById(o.getAgentId());
            tableModel.addRow(new Object[]{
                    o.getCode(), client == null ? "Client #" + o.getClientId() : client.getName(),
                    Theme.money(o.getTotal()), agent == null ? NONE : agent.getName(),
                    o.hasAgent() ? o.getDeliveryDate() : NONE, o.getStatus()});
        }
    }

    private Order selectedOrder() {
        int row = table.getSelectedRow();
        return row < 0 ? null : orders.getAll().get(row);
    }

    private static boolean isValidDate(String text) {
        try {
            LocalDate.parse(text);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
