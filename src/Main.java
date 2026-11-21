import ui.AssignDeliveryPanel;
import ui.ClientPanel;
import ui.DeliveryAgentPanel;
import ui.EmailLogPanel;
import ui.LoginPanel;
import ui.MainMenuPanel;
import ui.ProcessOrderPanel;
import ui.ProductPanel;
import ui.ReportsPanel;
import ui.StockPanel;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Main {

    private static final int SMALL_WIDTH = 750;
    private static final int SMALL_HEIGHT = 520;
    private static final int WIDE_WIDTH = 920;

    // only one window per section at a time, so two windows can't overwrite each other's saves
    private static JFrame productsFrame;
    private static final Map<String, JFrame> openFrames = new LinkedHashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("Using default look and feel: " + e.getMessage());
            }
            showLogin();
        });
    }

    private static void showLogin() {
        JFrame loginFrame = new JFrame("EcoNest - Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setContentPane(new LoginPanel(username -> {
            loginFrame.dispose();
            showMainMenu(username);
        }));
        loginFrame.pack();
        loginFrame.setResizable(false);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }

    private static void showMainMenu(String username) {
        Map<String, Runnable> sections = new LinkedHashMap<>();
        sections.put("Products", Main::showProducts);
        sections.put("Clients", () -> openWindow("EcoNest - Clients", ClientPanel::new, SMALL_WIDTH, SMALL_HEIGHT));
        sections.put("Stock", () -> openWindow("EcoNest - Stock", StockPanel::new, SMALL_WIDTH, SMALL_HEIGHT));
        sections.put("Delivery Agents", () ->
                openWindow("EcoNest - Delivery Agents", DeliveryAgentPanel::new, SMALL_WIDTH, SMALL_HEIGHT));
        sections.put("Orders", () -> openWindow("EcoNest - Process Client Order", ProcessOrderPanel::new, WIDE_WIDTH, 460));
        sections.put("Assign Delivery", () ->
                openWindow("EcoNest - Assign Delivery", AssignDeliveryPanel::new, WIDE_WIDTH, 580));
        sections.put("Reports", () -> openWindow("EcoNest - Monthly Reports", ReportsPanel::new, WIDE_WIDTH, 470));
        sections.put("Email Log", () -> openWindow("EcoNest - Email Log", EmailLogPanel::new, SMALL_WIDTH, SMALL_HEIGHT));

        JFrame menuFrame = new JFrame("EcoNest Manager 1.0");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setContentPane(new MainMenuPanel(username, sections, () -> {
            closeProducts();
            for (JFrame frame : openFrames.values()) frame.dispose();
            openFrames.clear();
            menuFrame.dispose();
            showLogin();
        }));
        menuFrame.setSize(740, 420);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setVisible(true);
    }

    private static void showProducts() {
        if (productsFrame != null && productsFrame.isDisplayable()) {
            productsFrame.toFront();
            return;
        }
        productsFrame = new JFrame("EcoNest - Product Catalogue");
        productsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);   // closing it returns to the menu
        productsFrame.setContentPane(new ProductPanel());
        productsFrame.setSize(940, 480);
        productsFrame.setLocationRelativeTo(null);
        productsFrame.setVisible(true);
    }

    private static void openWindow(String title, Supplier<JPanel> content, int width, int height) {
        JFrame existing = openFrames.get(title);
        if (existing != null && existing.isDisplayable()) {
            existing.toFront();
            return;
        }
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);   // closing it returns to the menu
        frame.setContentPane(content.get());
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        openFrames.put(title, frame);
    }

    private static void closeProducts() {
        if (productsFrame != null) {
            productsFrame.dispose();
            productsFrame = null;
        }
    }
}
