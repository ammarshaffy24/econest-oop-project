package ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

// Colours and building blocks from the Figma design, shared by the Task 5-9 screens
final class Theme {
    static final Color BACKGROUND = new Color(0xeeeeee);
    static final Color HEADER_GREEN = new Color(0x2f6b32);
    static final Color LEGEND_BLUE = new Color(0x2f4f8f);
    static final Color TEXT = new Color(0x222222);
    static final Color MUTED_TEXT = new Color(0x555555);
    static final Color LABEL_TEXT = new Color(0x666666);
    static final Color FIELD_TEXT = new Color(0x333333);
    static final Color FIELD_BORDER = new Color(0x9c9c9c);
    static final Color FIELDSET_BORDER = new Color(0xb5b5b5);
    static final Color READ_ONLY_FIELD = new Color(0xf4f4f4);
    static final Color GRID = new Color(0xdcdcdc);
    static final Color TABLE_HEADER = new Color(0xe8e8e8);
    static final Color SELECTED_ROW = new Color(0xb8cfe5);
    static final Color STATUS_BAR = new Color(0xe4e4e4);
    static final Color DIVIDER = new Color(0xc8c8c8);
    static final Color ALERT = new Color(0xc24141);
    static final Color WARNING_BACKGROUND = new Color(0xfff4d6);
    static final Color WARNING_BORDER = new Color(0xd9a400);
    static final Color WARNING_TEXT = new Color(0x6b4e00);
    static final int LABEL_WIDTH = 110;
    static final int FIELD_HEIGHT = 24;
    static final int ROW_HEIGHT = 29;
    static final int GAP = 14;

    private Theme() { }

    static JPanel screen(String title, String subtitle, String hint, JComponent content) {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(BACKGROUND);
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
        screen.add(header(title, subtitle), BorderLayout.NORTH);
        screen.add(content, BorderLayout.CENTER);
        screen.add(statusBar(hint), BorderLayout.SOUTH);
        return screen;
    }

    private static JPanel header(String title, String subtitle) {
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(11f));
        subtitleLabel.setForeground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_GREEN);
        header.setBorder(BorderFactory.createEmptyBorder(10, GAP, 10, GAP));
        header.add(titleLabel, BorderLayout.WEST);
        header.add(subtitleLabel, BorderLayout.EAST);
        return header;
    }

    private static JPanel statusBar(String hint) {
        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(hintLabel.getFont().deriveFont(11f));
        hintLabel.setForeground(MUTED_TEXT);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(STATUS_BAR);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, DIVIDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        bar.add(hintLabel, BorderLayout.WEST);
        return bar;
    }

    static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    static JScrollPane table(JTable table, int... columnWidths) {
        table.setRowHeight(ROW_HEIGHT);
        table.setFont(table.getFont().deriveFont(13f));
        table.setForeground(TEXT);
        table.setGridColor(GRID);
        table.setShowGrid(true);
        table.setSelectionBackground(SELECTED_ROW);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        for (int i = 0; i < columnWidths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);

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

    static JPanel fieldSet(String legendText) {
        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        TitledBorder legend = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(FIELDSET_BORDER), legendText);
        legend.setTitleColor(LEGEND_BLUE);
        fields.setBorder(BorderFactory.createCompoundBorder(legend, BorderFactory.createEmptyBorder(4, 8, 8, 8)));
        return fields;
    }

    static void addFormRow(JPanel panel, int row, String labelText, JComponent field) {
        JLabel label = new JLabel(labelText);
        label.setForeground(TEXT);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, FIELD_HEIGHT));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, FIELD_HEIGHT));
        if (field instanceof JTextField) styleField((JTextField) field);
        if (field instanceof JComboBox) field.setBackground(Color.WHITE);

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

    static JTextField readOnlyField() {
        JTextField field = new JTextField();
        styleField(field);
        field.setEditable(false);
        field.setBackground(READ_ONLY_FIELD);
        field.setForeground(MUTED_TEXT);
        return field;
    }

    private static void styleField(JTextField field) {
        if (field.isEditable()) field.setForeground(FIELD_TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(0, 6, 0, 6)));
    }

    static JPanel row(int alignment, JComponent... parts) {
        JPanel row = new JPanel(new FlowLayout(alignment, 8, 0));
        row.setOpaque(false);
        for (JComponent part : parts) row.add(part);
        return row;
    }

    static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static String money(double amount) {
        return String.format("%,.2f", amount);
    }
}
