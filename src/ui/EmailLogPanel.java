package ui;

import model.Email;
import service.EmailService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.List;

// Tasks 8 & 9: every email notification the app has sent, newest first
public class EmailLogPanel extends JPanel {
    private static final int BODY_ROWS = 10;

    private final List<Email> emails = new EmailService().getAll();
    private final DefaultTableModel tableModel = Theme.readOnlyModel("Sent", "Order", "To", "Subject");
    private final JTable table = new JTable(tableModel);
    private final JTextArea txtBody = new JTextArea(BODY_ROWS, 0);

    public EmailLogPanel() {
        txtBody.setEditable(false);
        txtBody.setLineWrap(true);
        txtBody.setWrapStyleWord(true);
        txtBody.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane bodyScroll = new JScrollPane(txtBody);
        bodyScroll.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER));

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.add(Theme.table(table, 120, 70, 180, 330), BorderLayout.CENTER);
        content.add(bodyScroll, BorderLayout.SOUTH);
        setLayout(new BorderLayout());
        add(Theme.screen("Email Log", "Tasks 8 & 9",
                "Agents are emailed on assign (Task 9); clients are emailed on dispatch (Task 8)", content));

        Collections.reverse(emails);
        for (Email e : emails)
            tableModel.addRow(new Object[]{e.getSentAt(), "ORD-" + e.getOrderId(), e.getTo(), e.getSubject()});
        table.getSelectionModel().addListSelectionListener(e -> showSelectedBody());
        if (!emails.isEmpty()) table.setRowSelectionInterval(0, 0);
    }

    private void showSelectedBody() {
        int row = table.getSelectedRow();
        txtBody.setText(row < 0 ? "" : emails.get(row).getBody());
        txtBody.setCaretPosition(0);
    }
}
