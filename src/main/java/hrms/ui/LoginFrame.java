package hrms.ui;

import hrms.dao.UserDAO;
import hrms.model.User;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/** Modern sign-in screen for the HRMS desktop application. */
public final class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField("", 22);
    private final JPasswordField passwordField = new JPasswordField(22);
    private final UserDAO userDAO = new UserDAO();
    private final JButton loginButton = UITheme.button("Sign in to HRMS  →", UITheme.BLUE, Color.WHITE);

    public LoginFrame() {
        super("HRMS | Sign In");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 660));
        setSize(1100, 730);
        setLocationRelativeTo(null);
        setContentPane(buildScreen());
        getRootPane().setDefaultButton(loginButton);
        loginButton.addActionListener(event -> signIn());
        passwordField.addActionListener(event -> signIn());
    }

    private JPanel buildScreen() {
        JPanel screen = new JPanel(new BorderLayout());
        screen.setBackground(UITheme.BG);
        screen.setBorder(new EmptyBorder(24, 24, 24, 24));

        BrandPanel brand = new BrandPanel();
        brand.setOpaque(false);
        brand.setPreferredSize(new Dimension(420, 600));
        brand.setLayout(new BorderLayout());
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new javax.swing.BoxLayout(brandText, javax.swing.BoxLayout.Y_AXIS));
        brandText.setBorder(new EmptyBorder(54, 42, 42, 42));
        JLabel logo = new JLabel("HR");
        logo.setOpaque(true);
        logo.setBackground(new Color(255, 255, 255, 32));
        logo.setForeground(Color.WHITE);
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setFont(new Font("SansSerif", Font.BOLD, 23));
        logo.setPreferredSize(new Dimension(58, 58));
        logo.setMaximumSize(new Dimension(58, 58));
        logo.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 90), 1, true));
        brandText.add(logo);
        brandText.add(javax.swing.Box.createVerticalStrut(42));
        JLabel eyebrow = new JLabel("PEOPLE. RECORDS. ORGANIZATION.");
        eyebrow.setForeground(new Color(180, 215, 255));
        eyebrow.setFont(new Font("SansSerif", Font.BOLD, 11));
        brandText.add(eyebrow);
        brandText.add(javax.swing.Box.createVerticalStrut(14));
        JLabel headline = new JLabel("A better way to\nmanage your people.");
        headline.setText("<html>A better way to<br>manage your people.</html>");
        headline.setForeground(Color.WHITE);
        headline.setFont(new Font("SansSerif", Font.BOLD, 31));
        brandText.add(headline);
        brandText.add(javax.swing.Box.createVerticalStrut(18));
        JLabel description = new JLabel("<html>One workspace for employee profiles,<br>attendance, departments and leave requests.</html>");
        description.setForeground(new Color(218, 230, 246));
        description.setFont(new Font("SansSerif", Font.PLAIN, 14));
        brandText.add(description);
        brand.add(brandText, BorderLayout.NORTH);

        JPanel brandFooter = new JPanel(new BorderLayout());
        brandFooter.setOpaque(false);
        brandFooter.setBorder(new EmptyBorder(0, 42, 32, 42));
        JLabel footer = new JLabel("HUMAN RESOURCE MANAGEMENT SYSTEM  •  TUP TAGUIG");
        footer.setForeground(new Color(196, 215, 239));
        footer.setFont(new Font("SansSerif", Font.PLAIN, 10));
        brandFooter.add(footer, BorderLayout.SOUTH);
        brand.add(brandFooter, BorderLayout.SOUTH);
        screen.add(brand, BorderLayout.WEST);

        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(UITheme.WHITE);
        right.setBorder(new EmptyBorder(28, 46, 28, 46));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 0, 0, 0);
        c.gridy = 0;
        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(new Font("SansSerif", Font.BOLD, 28));
        welcome.setForeground(UITheme.TEXT);
        right.add(welcome, c);
        c.gridy++;
        c.insets = new Insets(8, 0, 30, 0);
        JLabel sub = new JLabel("Sign in to continue to your HR workspace.");
        sub.setForeground(UITheme.MUTED);
        sub.setFont(UITheme.BODY);
        right.add(sub, c);

        c.insets = new Insets(0, 0, 7, 0);
        c.gridy++;
        right.add(formLabel("Username"), c);
        c.gridy++;
        usernameField.setText("");
        UITheme.styleInput(usernameField);
        usernameField.setPreferredSize(new Dimension(320, 44));
        right.add(usernameField, c);
        c.gridy++;
        c.insets = new Insets(20, 0, 7, 0);
        right.add(formLabel("Password"), c);
        c.gridy++;
        UITheme.styleInput(passwordField);
        passwordField.setPreferredSize(new Dimension(320, 44));
        right.add(passwordField, c);
        c.gridy++;
        c.insets = new Insets(8, 0, 20, 0);
        JCheckBox showPassword = new JCheckBox("Show password");
        showPassword.setFont(UITheme.SMALL);
        showPassword.setForeground(UITheme.MUTED);
        showPassword.setBackground(UITheme.WHITE);
        showPassword.addActionListener(event -> passwordField.setEchoChar(showPassword.isSelected() ? (char) 0 : '•'));
        right.add(showPassword, c);
        c.gridy++;
        c.insets = new Insets(0, 0, 18, 0);
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(320, 46));
        right.add(loginButton, c);
        c.gridy++;
        c.insets = new Insets(8, 0, 16, 0);
        JPanel secondaryActions = new JPanel(new GridLayout(1, 2, 10, 0));
        secondaryActions.setBackground(UITheme.WHITE);
        JButton clearButton = UITheme.button("Clear / Reset", UITheme.WHITE, UITheme.TEXT);
        JButton exitButton = UITheme.button("Exit", new Color(255, 242, 243), new Color(180, 54, 65));
        clearButton.addActionListener(event -> { usernameField.setText(""); passwordField.setText(""); usernameField.requestFocusInWindow(); });
        exitButton.addActionListener(event -> {
            int choice = JOptionPane.showConfirmDialog(this, "Exit the Human Resource Management System?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) System.exit(0);
        });
        secondaryActions.add(clearButton); secondaryActions.add(exitButton);
        right.add(secondaryActions, c);
        // Keep the login form uncluttered: no demo-access box, demo credentials,
        // or promotional footer is displayed beneath the login controls.

        screen.add(right, BorderLayout.CENTER);
        return screen;
    }

    private JLabel formLabel(String label) {
        JLabel result = new JLabel(label);
        result.setFont(UITheme.BODY_BOLD);
        result.setForeground(UITheme.TEXT);
        return result;
    }

    private void signIn() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");
        try {
            User user = userDAO.authenticate(username, password);
            if (user == null) {
                passwordField.setText("");
                JOptionPane.showMessageDialog(this, "The username or password is incorrect.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                passwordField.requestFocusInWindow();
                return;
            }
            new MainFrame(user, this).setVisible(true);
            setVisible(false);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Could not verify the account. Check the local Derby database connection.\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            loginButton.setEnabled(true);
            loginButton.setText("Sign in to HRMS  →");
        }
    }

    private static final class BrandPanel extends JPanel {
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, new Color(15, 43, 77), getWidth(), getHeight(), new Color(42, 103, 171)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(255, 255, 255, 18));
            g.fillOval(getWidth() - 180, getHeight() - 230, 260, 260);
            g.fillOval(getWidth() - 130, getHeight() - 175, 160, 160);
            g.setColor(new Color(54, 190, 177, 38));
            g.fillOval(getWidth() - 240, getHeight() - 340, 180, 180);
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
