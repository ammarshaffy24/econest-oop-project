package ui;

import dao.OrderDAO;
import dao.StockDAO;
import model.StockItem;
import service.ReportService;
import service.ReportService.MonthlyReport;
import service.ReportService.ProductSales;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

// Task 7: monthly sales & inventory report with low-stock alerts
public class ReportsPanel extends JPanel {
    private static final int YEARS_SHOWN = 5;
    private static final int TILE_HEIGHT = 60;

    private final ReportService reportService = new ReportService();
    private final JComboBox<String> cmbMonth = new JComboBox<>();
    private final JComboBox<Integer> cmbYear = new JComboBox<>();
    private final JLabel lblRevenue = tileValue();
    private final JLabel lblOrders = tileValue();
    private final JLabel lblItems = tileValue();
    private final JLabel lblLowStock = tileValue();
    private final JLabel lblAlert = new JLabel();
    private final DefaultTableModel topModel = Theme.readOnlyModel("Product", "Qty Sold", "Revenue");
    private final DefaultTableModel stockModel = Theme.readOnlyModel("Product", "On Hand", "Reorder Lvl");

    private MonthlyReport report;

    public ReportsPanel() {
        LocalDate today = LocalDate.now();
        for (Month m : Month.values()) cmbMonth.addItem(m.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        for (int y = today.getYear(); y > today.getYear() - YEARS_SHOWN; y--) cmbYear.addItem(y);
        cmbMonth.setSelectedIndex(today.getMonthValue() - 1);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(buildControls(), BorderLayout.NORTH);
        top.add(buildTiles(), BorderLayout.CENTER);
        top.add(buildAlert(), BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.add(top, BorderLayout.NORTH);
        content.add(buildTables(), BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(Theme.screen("Sales & Inventory Report", "Task 7",
                "Report built by looping over the orders ArrayList for the chosen month", content));
        generate();
    }

    private JPanel buildControls() {
        JButton btnGenerate = new StyledButton("Generate", StyledButton.GREEN);
        JButton btnExport = new StyledButton("Export .txt", StyledButton.GREY);
        btnGenerate.addActionListener(e -> generate());
        btnExport.addActionListener(e -> export());
        for (JComboBox<?> box : new JComboBox<?>[]{cmbMonth, cmbYear}) {
            box.setBackground(Color.WHITE);
            box.setPreferredSize(new Dimension(150, Theme.FIELD_HEIGHT));
        }
        return Theme.row(FlowLayout.LEFT, new JLabel("Month"), cmbMonth, new JLabel("Year"), cmbYear,
                btnGenerate, btnExport);
    }

    private JPanel buildTiles() {
        lblLowStock.setForeground(Theme.ALERT);
        JPanel tiles = new JPanel(new GridLayout(1, 4, 10, 0));
        tiles.setOpaque(false);
        tiles.add(tile("Total Revenue", lblRevenue));
        tiles.add(tile("Orders", lblOrders));
        tiles.add(tile("Items Sold", lblItems));
        tiles.add(tile("Low-Stock Items", lblLowStock));
        return tiles;
    }

    private JPanel tile(String caption, JLabel value) {
        JLabel label = new JLabel(caption);
        label.setFont(label.getFont().deriveFont(10f));
        label.setForeground(Theme.LABEL_TEXT);

        JPanel tile = new JPanel(new BorderLayout(0, 4));
        tile.setBackground(Color.WHITE);
        tile.setPreferredSize(new Dimension(0, TILE_HEIGHT));
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.FIELDSET_BORDER), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        tile.add(label, BorderLayout.NORTH);
        tile.add(value, BorderLayout.CENTER);
        return tile;
    }

    private static JLabel tileValue() {
        JLabel value = new JLabel();
        value.setFont(value.getFont().deriveFont(Font.BOLD, 18f));
        value.setForeground(Theme.TEXT);
        return value;
    }

    private JLabel buildAlert() {
        lblAlert.setOpaque(true);
        lblAlert.setBackground(Theme.WARNING_BACKGROUND);
        lblAlert.setForeground(Theme.WARNING_TEXT);
        lblAlert.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.WARNING_BORDER), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return lblAlert;
    }

    private JPanel buildTables() {
        JPanel tables = new JPanel(new GridLayout(1, 2, Theme.GAP, 0));
        tables.setOpaque(false);
        tables.add(titled("Top Products", Theme.table(new JTable(topModel), 191, 105, 131)));
        tables.add(titled("Stock Summary", Theme.table(new JTable(stockModel), 184, 109, 135)));
        return tables;
    }

    private JPanel titled(String title, JComponent table) {
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setForeground(Theme.TEXT);
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(label, BorderLayout.NORTH);
        panel.add(table, BorderLayout.CENTER);
        return panel;
    }

    private void generate() {
        report = reportService.build(new OrderDAO().getAll(), new StockDAO().getAll(),
                (Integer) cmbYear.getSelectedItem(), cmbMonth.getSelectedIndex() + 1);

        lblRevenue.setText("LKR " + Theme.money(report.revenue));
        lblOrders.setText(String.valueOf(report.orderCount));
        lblItems.setText(String.valueOf(report.itemsSold));
        lblLowStock.setText(String.valueOf(report.lowStock.size()));
        lblAlert.setText(lowStockText());
        lblAlert.setVisible(!report.lowStock.isEmpty());

        topModel.setRowCount(0);
        for (ProductSales s : report.topProducts)
            topModel.addRow(new Object[]{s.name, s.quantity, Theme.money(s.revenue)});
        stockModel.setRowCount(0);
        for (StockItem s : report.stockSummary)
            stockModel.addRow(new Object[]{s.getProductName(), s.getQuantityOnHand(), s.getReorderLevel()});
    }

    private String lowStockText() {
        StringBuilder text = new StringBuilder("⚠ Low stock: ");
        for (int i = 0; i < report.lowStock.size(); i++) {
            StockItem s = report.lowStock.get(i);
            if (i > 0) text.append(" · ");
            text.append(s.getProductName()).append(" (").append(s.getQuantityOnHand())
                    .append(" left, reorder at ").append(s.getReorderLevel()).append(')');
        }
        return text.toString();
    }

    private void export() {
        String month = (String) cmbMonth.getSelectedItem();
        int year = (Integer) cmbYear.getSelectedItem();
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EcoNest-report-" + month + "-" + year + ".txt"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            Files.write(chooser.getSelectedFile().toPath(),
                    reportService.toText(report, month, year).getBytes(StandardCharsets.UTF_8));
            JOptionPane.showMessageDialog(this, "Report saved to " + chooser.getSelectedFile(),
                    "Exported", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            Theme.error(this, "Could not save the report: " + e.getMessage());
        }
    }
}
