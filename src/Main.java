import ui.LoginPanel;
import ui.MainMenuPanel;
import ui.ProductPanel;

import javax.swing.*;

public class Main {

    // only one Products window at a time, so two windows can't overwrite each other's saves
    private static JFrame productsFrame;

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
        JFrame menuFrame = new JFrame("EcoNest Manager 1.0");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setContentPane(new MainMenuPanel(username, Main::showProducts, () -> {
            closeProducts();
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

    private static void closeProducts() {
        if (productsFrame != null) {
            productsFrame.dispose();
            productsFrame = null;
        }
    }
}
