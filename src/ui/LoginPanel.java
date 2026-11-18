package ui;

import dao.UserDAO;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {

    private static final Color BACKGROUND = new Color(0xeeeeee);
    private static final Color HEADER_GREEN = new Color(0x2f6b32);
    private static final Color TEXT = new Color(0x222222);
    private static final Color MUTED_TEXT = new Color(0x555555);
    private static final Color FIELD_TEXT = new Color(0x333333);
    private static final Color FIELD_BORDER = new Color(0x9c9c9c);
    private static final Color STATUS_BAR = new Color(0xe4e4e4);
    private static final Color DIVIDER = new Color(0xc8c8c8);
    private static final int LABEL_WIDTH = 90;
    private static final int FIELD_WIDTH = 254;
    private static final int FIELD_HEIGHT = 24;

    private final UserDAO userDao = new UserDAO();
    private final Consumer<String> onLoginSuccess;   // receives the logged-in username

    private JTextField txtUsername = new JTextField();
    private JPasswordField txtPassword = new JPasswordField();
    private JButton btnCancel = new StyledButton("Cancel", StyledButton.GREY);
    private JButton btnLogin = new StyledButton("Login", StyledButton.GREEN);

    public LoginPanel(Consumer<String> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        btnLogin.addActionListener(e -> login());
        txtPassword.addActionListener(e -> login());   // Enter key
        btnCancel.addActionListener(e -> System.exit(0));
    }

    private JPanel buildHeader() {
        JLabel title = new JLabel("EcoNest");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 15f));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Staff Login");
        subtitle.setFont(subtitle.getFont().deriveFont(11f));
        subtitle.setForeground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_GREEN);
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        header.add(title, BorderLayout.WEST);
        header.add(subtitle, BorderLayout.EAST);
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        addRow(form, 0, "User Name", txtUsername);
        addRow(form, 1, "Password", txtPassword);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(btnCancel);
        buttons.add(btnLogin);

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 2;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(10, 0, 0, -8);   // FlowLayout adds a trailing gap
        form.add(buttons, c);
        return form;
    }

    private void addRow(JPanel form, int row, String text, JTextField field) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, FIELD_HEIGHT));

        field.setPreferredSize(new Dimension(FIELD_WIDTH, FIELD_HEIGHT));
        field.setForeground(FIELD_TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER),
                BorderFactory.createEmptyBorder(0, 6, 0, 6)));

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(5, 0, 5, 8);
        form.add(label, c);

        c.gridx = 1;
        c.insets = new Insets(5, 0, 5, 0);
        form.add(field, c);
    }

    private JPanel buildStatusBar() {
        JLabel hint = new JLabel("Enter your staff account details");
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

    private void login() {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        if (userDao.isValidLogin(username, password)) {
            onLoginSuccess.accept(username.trim());
        } else {
            txtPassword.setText("");
            JOptionPane.showMessageDialog(this, "Wrong user name or password.",
                    "Login failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
