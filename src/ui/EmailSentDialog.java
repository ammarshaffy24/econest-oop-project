package ui;

import model.Email;

import javax.swing.*;
import java.awt.*;

// Tasks 8 & 9: pops up straight after an email is sent, showing exactly what went to whom
class EmailSentDialog extends JDialog {
    private static final int WIDTH = 540;
    private static final int HEIGHT = 470;
    private static final int BODY_ROWS = 12;

    EmailSentDialog(Component parent, String title, String task, String savedMessage, Email email) {
        super(SwingUtilities.getWindowAncestor(parent), "EcoNest - Email Sent", ModalityType.APPLICATION_MODAL);

        JPanel fields = Theme.fieldSet("Email");
        Theme.addFormRow(fields, 0, "To", readOnly(email.getTo()));
        Theme.addFormRow(fields, 1, "Subject", readOnly(email.getSubject()));
        Theme.addFormRow(fields, 2, "Sent at", readOnly(email.getSentAt()));

        JTextArea body = new JTextArea(email.getBody(), BODY_ROWS, 0);
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        body.setCaretPosition(0);
        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER));

        JButton ok = new StyledButton("OK", StyledButton.GREEN);
        ok.addActionListener(e -> dispose());

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.add(fields, BorderLayout.NORTH);
        content.add(bodyScroll, BorderLayout.CENTER);
        content.add(Theme.row(FlowLayout.RIGHT, ok), BorderLayout.SOUTH);

        setContentPane(Theme.screen("✓ " + title, task, savedMessage + " Also saved to the Email Log.", content));
        getRootPane().setDefaultButton(ok);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(parent);
    }

    private static JTextField readOnly(String text) {
        JTextField field = Theme.readOnlyField();
        field.setText(text);
        field.setCaretPosition(0);
        return field;
    }
}
