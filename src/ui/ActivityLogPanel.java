package ui;

import model.Email;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Tasks 8 & 9: live log under the Assign Delivery table; email lines can be clicked to read the email
class ActivityLogPanel extends JPanel {
    private static final int HEIGHT = 130;
    private static final Color EMAIL_TEXT = new Color(0x2f6b32);
    private static final Color FAILED_TEXT = new Color(0xc24141);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private static class Entry {
        final String text;
        final Email email;
        final boolean failed;

        Entry(String text, Email email, boolean failed) {
            this.text = text;
            this.email = email;
            this.failed = failed;
        }
    }

    private final DefaultListModel<Entry> entries = new DefaultListModel<>();
    private final JList<Entry> list = new JList<>(entries);

    ActivityLogPanel(List<Email> history) {
        super(new BorderLayout(0, 6));
        setOpaque(false);
        setPreferredSize(new Dimension(0, HEIGHT));

        JLabel title = new JLabel("Activity Log  ·  click an ✉ line to read the email");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 12f));
        title.setForeground(Theme.TEXT);

        list.setFont(list.getFont().deriveFont(12f));
        list.setCellRenderer(new EntryRenderer());
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { openSelectedEmail(); }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.FIELD_BORDER));

        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        for (Email e : history)
            entries.add(0, new Entry(e.getSentAt() + "   ✉ ORD-" + e.getOrderId() + " · email sent to " + e.getTo(), e, false));
    }

    void addEmailSent(String action, Email email) {
        add(new Entry(now() + "   ✉ " + action + " · email sent to " + email.getTo(), email, false));
    }

    void addEmailFailed(String action, String reason) {
        add(new Entry(now() + "   ✗ " + action + " · email NOT sent: " + reason, null, true));
    }

    void addAction(String action) {
        add(new Entry(now() + "   " + action, null, false));
    }

    private void add(Entry entry) {
        entries.add(0, entry);
        list.setSelectedIndex(0);   // highlight the newest line
        list.ensureIndexIsVisible(0);
    }

    private void openSelectedEmail() {
        Entry entry = list.getSelectedValue();
        if (entry != null && entry.email != null)
            new EmailSentDialog(this, "Email sent", "Tasks 8 & 9", "Sent " + entry.email.getSentAt() + ".", entry.email)
                    .setVisible(true);
    }

    private static String now() {
        return LocalTime.now().format(TIME);
    }

    private static class EntryRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean selected, boolean focused) {
            Entry entry = (Entry) value;
            super.getListCellRendererComponent(list, entry.text, index, selected, focused);
            setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            if (selected) setBackground(Theme.SELECTED_ROW);
            setForeground(entry.failed ? FAILED_TEXT : entry.email != null ? EMAIL_TEXT : Theme.TEXT);
            return this;
        }
    }
}
