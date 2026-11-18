import ui.ProductPanel;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("EcoNest - Product Catalogue");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new ProductPanel());
            frame.setSize(850, 550);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}