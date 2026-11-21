package ui;

import dao.ClientDAO;
import dao.DeliveryAgentDAO;
import dao.OrderDAO;
import model.Client;
import model.DeliveryAgent;
import model.Email;
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
    private final ActivityLogPanel activityLog = new ActivityLogPanel(emailService.getAll());

    public AssignDeliveryPanel() {
        JPanel content = new JPanel(new BorderLayout(Theme.GAP, 0));
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        left.add(Theme.table(table, 76, 129, 90, 69, 82, 99), BorderLayout.CENTER);
        left.add(activityLog, BorderLayout.SOUTH);
        content.add(left, BorderLayout.CENTER);
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
        refreshAndReselect();
        String action = order.getCode() + " assigned to " + agent.getName() + " for " + date;
        if (chkEmailAgent.isSelected()) sendAgentEmail(order, agent, action);
        else logWithoutEmail(order, action);
    }

    private void updateStatus() {
        Order order = selectedOrder();
        if (order == null) { Theme.error(this, "Select an order from the table first."); return; }
        if (!order.hasAgent()) { Theme.error(this, "Assign a delivery agent before changing the status."); return; }
        String newStatus = (String) cmbStatus.getSelectedItem();
        if (newStatus.equals(order.getStatus())) { Theme.error(this, "The order is already " + newStatus + "."); return; }

        order.setStatus(newStatus);
        orders.save();
        refreshAndReselect();
        boolean emailClient = Order.DISPATCHED.equals(newStatus) && chkEmailClient.isSelected();
        String action = order.getCode() + " marked " + newStatus;
        if (emailClient) sendClientEmail(order, action);
        else logWithoutEmail(order, action);
    }

    private void sendAgentEmail(Order order, DeliveryAgent agent, String action) {
        Client client = clients.findById(order.getClientId());
        if (client == null) { emailFailed(order, action, "the client no longer exists"); return; }
        try {
            Email email = agentNotifier.notifyAgent(agent, client, order);
            emailSent(order, action, email, "Email sent to delivery agent", "Task 9");
        } catch (RuntimeException e) {
            emailFailed(order, action, e.getMessage());
        }
    }

    private void sendClientEmail(Order order, String action) {
        Client client = clients.findById(order.getClientId());
        DeliveryAgent agent = agents.findById(order.getAgentId());
        if (client == null || agent == null) { emailFailed(order, action, "the client or agent no longer exists"); return; }
        try {
            Email email = clientNotifier.notifyClient(client, agent, order);
            emailSent(order, action, email, "Email sent to client", "Task 8");
        } catch (RuntimeException e) {
            emailFailed(order, action, e.getMessage());
        }
    }

    private void emailSent(Order order, String action, Email email, String title, String task) {
        activityLog.addEmailSent(action, email);
        new EmailSentDialog(this, title, task, savedText(order), email).setVisible(true);
    }

    private void emailFailed(Order order, String action, String reason) {
        activityLog.addEmailFailed(action, reason);
        showSaved(order, "\nNo email sent: " + reason + ".");
    }

    private void logWithoutEmail(Order order, String action) {
        activityLog.addAction(action);
        showSaved(order, "");
    }

    private void refreshAndReselect() {
        int row = table.getSelectedRow();
        refreshTable();
        table.setRowSelectionInterval(row, row);
    }

    private void showSaved(Order order, String emailNote) {
        JOptionPane.showMessageDialog(this, savedText(order) + emailNote, "Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String savedText(Order order) {
        return order.getCode() + " is now " + order.getStatus() + ".";
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
