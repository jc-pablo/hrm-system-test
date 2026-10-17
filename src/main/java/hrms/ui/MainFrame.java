package hrms.ui;

import hrms.dao.AttendanceDAO;
import hrms.dao.DepartmentDAO;
import hrms.dao.EmployeeDAO;
import hrms.dao.LeaveDAO;
import hrms.dao.PositionDAO;
import hrms.dao.UserDAO;
import hrms.model.User;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/** Main shell with sidebar navigation, dashboard, and role-aware management screens. */
public final class MainFrame extends JFrame {
    private final User user;
    private final LoginFrame loginFrame;
    private final CardLayout pageLayout = new CardLayout();
    private final JPanel pageDeck = new JPanel(pageLayout);
    private final JPanel sidebar = new JPanel(new BorderLayout());
    private final JPanel navigation = new JPanel();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();
    private final Map<String, JPanel> pages = new LinkedHashMap<>();
    private final JLabel pageTitle = new JLabel("Overview");
    private final JLabel pageSubtitle = new JLabel("Monitor your organization's people records.");

    public MainFrame(User user, LoginFrame loginFrame) {
        super("HRMS | Human Resource Management System");
        this.user = user;
        this.loginFrame = loginFrame;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setSize(1420, 900);
        setMinimumSize(new Dimension(1150, 740));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout());

        buildSidebar();
        buildPages();
        add(sidebar, BorderLayout.WEST);
        add(buildWorkspace(), BorderLayout.CENTER);
        showPage("Dashboard");

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) { logout(); }
        });
    }

    private void buildSidebar() {
        sidebar.setBackground(UITheme.NAVY);
        sidebar.setPreferredSize(new Dimension(248, 800));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setBackground(UITheme.NAVY);
        brand.setBorder(new EmptyBorder(26, 22, 26, 14));
        JLabel mark = new JLabel("HR");
        mark.setOpaque(true);
        mark.setBackground(UITheme.BLUE);
        mark.setForeground(Color.WHITE);
        mark.setFont(new Font("SansSerif", Font.BOLD, 18));
        mark.setHorizontalAlignment(SwingConstants.CENTER);
        mark.setPreferredSize(new Dimension(44, 44));
        mark.setBorder(BorderFactory.createLineBorder(new Color(83, 142, 241)));
        JPanel brandText = new JPanel();
        brandText.setOpaque(false);
        brandText.setLayout(new javax.swing.BoxLayout(brandText, javax.swing.BoxLayout.Y_AXIS));
        JLabel appName = new JLabel("HRMS");
        appName.setFont(new Font("SansSerif", Font.BOLD, 18));
        appName.setForeground(Color.WHITE);
        JLabel appSub = new JLabel("PEOPLE OPERATIONS");
        appSub.setFont(new Font("SansSerif", Font.BOLD, 9));
        appSub.setForeground(new Color(159, 182, 213));
        appSub.setBorder(new EmptyBorder(3, 0, 0, 0));
        brandText.add(appName);
        brandText.add(appSub);
        brand.add(mark, BorderLayout.WEST);
        brand.add(brandText, BorderLayout.CENTER);
        sidebar.add(brand, BorderLayout.NORTH);

        navigation.setLayout(new javax.swing.BoxLayout(navigation, javax.swing.BoxLayout.Y_AXIS));
        navigation.setBackground(UITheme.NAVY);
        navigation.setBorder(new EmptyBorder(4, 13, 18, 13));
        addSection("WORKSPACE");
        addNavigation("Dashboard", "▦", "Dashboard");
        addNavigation("Reports", "▤", "Reports");
        addSection("PEOPLE MANAGEMENT");
        addNavigation("Employees", "♙", "Employees");
        addNavigation("Departments", "▤", "Departments");
        addNavigation("Positions", "▥", "Positions");
        addSection("TIME & LEAVE");
        addNavigation("Attendance", "◷", "Attendance");
        addNavigation("Leave Requests", "▧", "Leave Requests");
        if (user.isAdministrator()) {
            addSection("ADMINISTRATION");
            addNavigation("User Accounts", "⚙", "User Accounts");
        }
        sidebar.add(navigation, BorderLayout.CENTER);

        JPanel sideBottom = new JPanel(new BorderLayout(0, 9));
        sideBottom.setBackground(UITheme.NAVY);
        sideBottom.setBorder(new EmptyBorder(15, 18, 18, 18));
        JPanel account = new JPanel(new BorderLayout(10, 0));
        account.setBackground(new Color(31, 59, 94));
        account.setBorder(new EmptyBorder(11, 10, 11, 10));
        JLabel avatar = new JLabel(initials(user.getFullName()), SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setOpaque(true);
        avatar.setBackground(new Color(66, 105, 155));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 12));
        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new javax.swing.BoxLayout(details, javax.swing.BoxLayout.Y_AXIS));
        JLabel name = new JLabel(shortName(user.getFullName()));
        name.setForeground(Color.WHITE);
        name.setFont(UITheme.BODY_BOLD);
        JLabel role = new JLabel(user.isAdministrator() ? "Administrator" : "HR Staff");
        role.setForeground(new Color(174, 195, 222));
        role.setFont(UITheme.SMALL);
        details.add(name);
        details.add(role);
        account.add(avatar, BorderLayout.WEST);
        account.add(details, BorderLayout.CENTER);
        JButton logout = new JButton("⇥  Sign out");
        logout.setUI(new BasicButtonUI());
        logout.setForeground(Color.WHITE);
        logout.setBackground(UITheme.NAVY);
        logout.setFont(UITheme.BODY_BOLD);
        logout.setHorizontalAlignment(SwingConstants.LEFT);
        logout.setBorder(new EmptyBorder(10, 12, 10, 7));
        logout.setFocusPainted(false);
        logout.setContentAreaFilled(true);
        logout.setOpaque(true);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent event) {
                if (logout.isEnabled()) logout.setBackground(SIDEBAR_HOVER);
            }
            @Override public void mouseExited(MouseEvent event) {
                if (logout.isEnabled()) logout.setBackground(UITheme.NAVY);
            }
            @Override public void mousePressed(MouseEvent event) {
                if (logout.isEnabled()) logout.setBackground(SIDEBAR_PRESSED);
            }
            @Override public void mouseReleased(MouseEvent event) {
                if (logout.isEnabled()) logout.setBackground(logout.contains(event.getPoint()) ? SIDEBAR_HOVER : UITheme.NAVY);
            }
        });
        logout.addActionListener(event -> logout());
        sideBottom.add(account, BorderLayout.NORTH);
        sideBottom.add(logout, BorderLayout.SOUTH);
        sidebar.add(sideBottom, BorderLayout.SOUTH);
    }

    private void addSection(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 9));
        label.setForeground(new Color(128, 156, 190));
        label.setBorder(new EmptyBorder(18, 12, 8, 4));
        label.setAlignmentX(LEFT_ALIGNMENT);
        navigation.add(label);
    }

    private static final Color SIDEBAR_TEXT = new Color(245, 248, 253);
    private static final Color SIDEBAR_HOVER = new Color(38, 70, 108);
    private static final Color SIDEBAR_PRESSED = new Color(15, 35, 61);
    private static final Color SIDEBAR_ACTIVE = new Color(31, 83, 154);
    private static final Color SIDEBAR_ACTIVE_BAR = new Color(154, 199, 255);

    private void addNavigation(String key, String icon, String title) {
        JButton button = new JButton("  " + icon + "     " + title);
        button.setUI(new BasicButtonUI());
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
        button.setPreferredSize(new Dimension(218, 43));
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setForeground(SIDEBAR_TEXT);
        button.setBackground(UITheme.NAVY);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, UITheme.NAVY),
            new EmptyBorder(0, 12, 0, 8)));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setRolloverEnabled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("hrms.active", Boolean.FALSE);
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent event) {
                if (button.isEnabled()) applyNavigationStyle(button,
                    Boolean.TRUE.equals(button.getClientProperty("hrms.active")), true, false);
            }
            @Override public void mouseExited(MouseEvent event) {
                applyNavigationStyle(button,
                    Boolean.TRUE.equals(button.getClientProperty("hrms.active")), false, false);
            }
            @Override public void mousePressed(MouseEvent event) {
                if (button.isEnabled()) applyNavigationStyle(button,
                    Boolean.TRUE.equals(button.getClientProperty("hrms.active")), true, true);
            }
            @Override public void mouseReleased(MouseEvent event) {
                if (!button.isEnabled()) return;
                applyNavigationStyle(button,
                    Boolean.TRUE.equals(button.getClientProperty("hrms.active")),
                    button.contains(event.getPoint()), false);
            }
        });
        button.addActionListener(event -> showPage(key));
        navButtons.put(key, button);
        navigation.add(button);
        navigation.add(javax.swing.Box.createVerticalStrut(3));
    }

    private void applyNavigationStyle(JButton button, boolean active, boolean hover, boolean pressed) {
        Color background;
        if (pressed) background = active ? new Color(23, 62, 112) : SIDEBAR_PRESSED;
        else if (active) background = hover ? new Color(38, 95, 164) : SIDEBAR_ACTIVE;
        else if (hover) background = SIDEBAR_HOVER;
        else background = UITheme.NAVY;

        // Explicit BasicButtonUI and opaque fill prevent the operating-system look
        // and feel from replacing these text/background colors with low-contrast defaults.
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", active ? Font.BOLD : Font.PLAIN, 13));
        Color bar = active ? SIDEBAR_ACTIVE_BAR : (hover ? SIDEBAR_HOVER : UITheme.NAVY);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, bar),
            new EmptyBorder(0, 12, 0, 8)));
        button.repaint();
    }

    private JPanel buildWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(UITheme.BG);
        JPanel topbar = new JPanel(new BorderLayout());
        topbar.setBackground(UITheme.WHITE);
        topbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
            new EmptyBorder(17, 28, 16, 28)));
        JPanel titleStack = new JPanel();
        titleStack.setOpaque(false);
        titleStack.setLayout(new javax.swing.BoxLayout(titleStack, javax.swing.BoxLayout.Y_AXIS));
        pageTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        pageTitle.setForeground(UITheme.TEXT);
        pageSubtitle.setFont(UITheme.SMALL);
        pageSubtitle.setForeground(UITheme.MUTED);
        pageSubtitle.setBorder(new EmptyBorder(4, 0, 0, 0));
        titleStack.add(pageTitle);
        titleStack.add(pageSubtitle);
        topbar.add(titleStack, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        date.setFont(UITheme.BODY);
        date.setForeground(UITheme.MUTED);
        JLabel profile = new JLabel(user.getFullName() + "  ▾");
        profile.setFont(UITheme.BODY_BOLD);
        profile.setForeground(UITheme.NAVY_2);
        profile.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.BORDER), new EmptyBorder(9, 12, 9, 12)));
        profile.setOpaque(true);
        profile.setBackground(UITheme.WHITE);
        right.add(date);
        right.add(profile);
        topbar.add(right, BorderLayout.EAST);

        pageDeck.setBackground(UITheme.BG);
        workspace.add(topbar, BorderLayout.NORTH);
        workspace.add(pageDeck, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER),
            new EmptyBorder(8, 28, 8, 28)));
        JLabel system = new JLabel("HRMS • Apache Derby database");
        system.setForeground(UITheme.MUTED);
        system.setFont(UITheme.SMALL);
        JLabel security = new JLabel("Signed in securely  •  " + (user.isAdministrator() ? "Administrator access" : "HR Staff access"));
        security.setForeground(UITheme.MUTED);
        security.setFont(UITheme.SMALL);
        footer.add(system, BorderLayout.WEST);
        footer.add(security, BorderLayout.EAST);
        workspace.add(footer, BorderLayout.SOUTH);
        return workspace;
    }

    private void buildPages() {
        DashboardPanel dashboard = new DashboardPanel(user, this::showPage);
        addPage("Dashboard", dashboard);
        addPage("Reports", new ReportsPanel());
        addPage("Employees", new EntityPanel(new EmployeeDAO(), user));
        addPage("Departments", new EntityPanel(new DepartmentDAO(), user));
        addPage("Positions", new EntityPanel(new PositionDAO(), user));
        addPage("Attendance", new EntityPanel(new AttendanceDAO(), user));
        addPage("Leave Requests", new EntityPanel(new LeaveDAO(), user));
        if (user.isAdministrator()) addPage("User Accounts", new EntityPanel(new UserDAO(), user));
    }

    private void addPage(String key, JPanel panel) {
        pages.put(key, panel);
        pageDeck.add(panel, key);
    }

    private void showPage(String key) {
        if (!pages.containsKey(key)) return;
        pageLayout.show(pageDeck, key);
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            JButton button = entry.getValue();
            button.putClientProperty("hrms.active", active);
            applyNavigationStyle(button, active, button.getModel().isRollover(), false);
        }
        pageTitle.setText(key.equals("Dashboard") ? "Overview" : key);
        pageSubtitle.setText(switch (key) {
            case "Dashboard" -> "Monitor your organization's people records.";
            case "Reports" -> "View and export employee, attendance, department, and leave reports.";
            case "Employees" -> "Maintain employee information and employment details.";
            case "Departments" -> "Organize teams and departmental information.";
            case "Positions" -> "Manage job titles and position descriptions.";
            case "Attendance" -> "Record daily attendance, time in, and time out.";
            case "Leave Requests" -> user.isAdministrator() ? "Review requests and update approval decisions." : "File and track employee leave requests.";
            case "User Accounts" -> "Manage sign-in accounts and access roles.";
            default -> "Human Resource Management System";
        });
        JPanel panel = pages.get(key);
        if (panel instanceof EntityPanel entityPanel) entityPanel.refreshPanel();
        if (panel instanceof DashboardPanel dashboardPanel) dashboardPanel.refreshData();
        if (panel instanceof ReportsPanel reportsPanel) reportsPanel.refreshData();
        pageDeck.revalidate();
        pageDeck.repaint();
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Sign Out", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            loginFrame.setVisible(true);
        }
    }

    private String initials(String value) {
        if (value == null || value.isBlank()) return "HR";
        String[] parts = value.trim().split("\\s+");
        return (parts[0].substring(0, 1) + (parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "")).toUpperCase();
    }

    private String shortName(String value) {
        if (value == null || value.isBlank()) return "HR User";
        String[] parts = value.trim().split("\\s+");
        return parts.length > 1 ? parts[0] + " " + parts[parts.length - 1] : value;
    }
}
