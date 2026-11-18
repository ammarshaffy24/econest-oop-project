package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainMenuPanel extends JPanel {

    private static final Color BACKGROUND = new Color(0xeeeeee);
    private static final Color HEADER_GREEN = new Color(0x2f6b32);
    private static final Color MUTED_TEXT = new Color(0x555555);
    private static final Color STATUS_BAR = new Color(0xe4e4e4);
    private static final Color DIVIDER = new Color(0xc8c8c8);
    private static final int TILE_GAP = 12;
    private static final int PADDING = 16;

    private final Runnable onOpenProducts;
    private final Runnable onLogout;

    public MainMenuPanel(String username, Runnable onOpenProducts, Runnable onLogout) {
        this.onOpenProducts = onOpenProducts;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        add(buildHeader(username), BorderLayout.NORTH);
        add(buildTiles(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader(String username) {
        JLabel title = new JLabel("Main Menu");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(Color.WHITE);

        JLabel logout = new JLabel("<html>Logged in as: " + escapeHtml(username) + " · <u>Log out</u></html>");
        logout.setFont(logout.getFont().deriveFont(11f));
        logout.setForeground(Color.WHITE);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onLogout.run();
            }
        });

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_GREEN);
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        header.add(title, BorderLayout.WEST);
        header.add(logout, BorderLayout.EAST);
        return header;
    }

    private JPanel buildTiles() {
        JPanel tiles = new JPanel(new GridLayout(3, 3, TILE_GAP, TILE_GAP));
        tiles.setOpaque(false);
        tiles.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));

        tiles.add(tile("Products", "Task 1 · Catalogue", onOpenProducts));
        tiles.add(notBuiltTile("Clients", "Task 2 · Household / Office"));
        tiles.add(notBuiltTile("Stock", "Task 3 · Inventory"));
        tiles.add(notBuiltTile("Delivery Agents", "Task 4 · Staff & vehicles"));
        tiles.add(notBuiltTile("Orders", "Task 5 · Process orders"));
        tiles.add(notBuiltTile("Assign Delivery", "Task 6 · Schedule & track"));
        tiles.add(notBuiltTile("Reports", "Task 7 · Monthly sales & stock"));
        tiles.add(notBuiltTile("Email Log", "Tasks 8 & 9 · Notifications"));
        tiles.add(tile("Exit", "Close application", () -> System.exit(0)));
        return tiles;
    }

    private JButton tile(String title, String subtitle, Runnable action) {
        String text = "<html><center><b style='font-size:11px'>" + escapeHtml(title) + "</b><br>"
                + "<span style='font-size:8px;color:#666666'>" + escapeHtml(subtitle) + "</span></center></html>";
        JButton button = new StyledButton(text, StyledButton.GREY);
        button.addActionListener(e -> action.run());
        return button;
    }

    private JButton notBuiltTile(String title, String subtitle) {
        return tile(title, subtitle, () -> JOptionPane.showMessageDialog(this,
                title + " is not built yet.", title, JOptionPane.INFORMATION_MESSAGE));
    }

    private JPanel buildStatusBar() {
        JLabel hint = new JLabel("Choose a section to open it in a new window");
        hint.setFont(hint.getFont().deriveFont(11f));
        hint.setForeground(MUTED_TEXT);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(STATUS_BAR);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, DIVIDER),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        bar.add(hint, BorderLayout.WEST);
        return bar;
    }

    // usernames come from users.txt, so don't let them inject HTML into the label
    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
