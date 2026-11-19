import ui.ProductPanel;
import ui.StockPanel;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("EcoNest");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JTabbedPane tabs = new JTabbedPane();

            ProductPanel productPanel = new ProductPanel();
            StockPanel stockPanel = new StockPanel();

            tabs.addTab("Products", productPanel);
            tabs.addTab("Stock", stockPanel);

            // refresh the stock list every time the Stock tab is opened
            tabs.addChangeListener(e -> {
                if (tabs.getSelectedComponent() == stockPanel) {
                    stockPanel.refresh();
                }
            });

            frame.setContentPane(tabs);
            frame.setSize(950, 650);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}