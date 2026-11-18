package econest;

import econest.ui.ClientPanel;
import econest.ui.DeliveryAgentPanel;
import javax.swing.*;

// Test window for Tasks 2 and 4. In the final project, the group's main
// window just adds these two panels as tabs / menu items.
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("EcoNest - Clients & Delivery Agents");
            JTabbedPane tabs = new JTabbedPane();
            tabs.add("Clients", new ClientPanel());
            tabs.add("Delivery Agents", new DeliveryAgentPanel());
            f.add(tabs);
            f.setSize(750, 520);
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
        });
    }
}
