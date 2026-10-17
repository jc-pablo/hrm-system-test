package hrms.ui;

import hrms.model.User;
import hrms.util.DBConnection;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;

/** Dashboard overview with live database metrics, attendance status, and recent employees. */
public final class DashboardPanel extends JPanel {
    private final User user;
    private final Consumer<String> navigate;
    private final JPanel metricGrid = new JPanel(new GridLayout(1, 4, 14, 0));
    private final JPanel attendanceRows = new JPanel(new GridLayout(4, 1, 0, 11));
    private final JTable recentTable = new JTable();
    private final JLabel refreshLabel = new JLabel("Updated just now");

    public DashboardPanel(User user, Consumer<String> navigate) {
        super(new BorderLayout(0, 20));
        this.user = user;
        this.navigate = navigate;
        setBackground(UITheme.BG);
        setBorder(new EmptyBorder(26, 28, 26, 28));
        buildLayout();
        refreshData();
    }

    private void buildLayout() {
        JPanel pageTop = new JPanel(new BorderLayout());
        pageTop.setOpaque(false);
        JPanel intro = new JPanel();
        intro.setOpaque(false);
        intro.setLayout(new javax.swing.BoxLayout(intro, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Good day, " + firstName(user.getFullName()) + " 👋");
        title.setFont(UITheme.H1);
        title.setForeground(UITheme.TEXT);
        JLabel subtitle = new JLabel("Here's what's happening across your HR workspace.");
        subtitle.setFont(UITheme.BODY);
        subtitle.setForeground(UITheme.MUTED);
        subtitle.setBorder(new EmptyBorder(6, 0, 0, 0));
        intro.add(title);
        intro.add(subtitle);
        pageTop.add(intro, BorderLayout.WEST);

        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRight.setOpaque(false);
        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        date.setFont(UITheme.BODY_BOLD);
        date.setForeground(UITheme.NAVY_2);
        date.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.BORDER), new EmptyBorder(10, 14, 10, 14)));
        date.setOpaque(true);
        date.setBackground(UITheme.WHITE);
        JButton refresh = UITheme.button("↻ Refresh", UITheme.WHITE, UITheme.TEXT);
        refresh.addActionListener(event -> refreshData());
        topRight.add(date);
        topRight.add(refresh);
        pageTop.add(topRight, BorderLayout.EAST);
        add(pageTop, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        metricGrid.setOpaque(false);
        body.add(metricGrid, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 2, 18, 0));
        lower.setOpaque(false);
        lower.add(buildAttendanceCard());
        lower.add(buildRecentCard());
        body.add(lower, BorderLayout.CENTER);

        JPanel quick = buildQuickActions();
        body.add(quick, BorderLayout.SOUTH);
        add(body, BorderLayout.CENTER);
    }

    private JPanel buildAttendanceCard() {
        JPanel card = UITheme.card(18);
        card.setLayout(new BorderLayout(0, 18));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));
        JPanel titleLine = new JPanel(new BorderLayout());
        titleLine.setOpaque(false);
        JLabel title = new JLabel("Today's attendance");
        title.setFont(UITheme.H2);
        title.setForeground(UITheme.TEXT);
        JLabel tag = new JLabel("LIVE SUMMARY");
        tag.setFont(new Font("SansSerif", Font.BOLD, 9));
        tag.setForeground(UITheme.TEAL);
        tag.setBorder(new EmptyBorder(6, 8, 6, 8));
        tag.setOpaque(true);
        tag.setBackground(UITheme.GREEN_PALE);
        titleLine.add(title, BorderLayout.WEST);
        titleLine.add(tag, BorderLayout.EAST);
        card.add(titleLine, BorderLayout.NORTH);

        attendanceRows.setOpaque(false);
        card.add(attendanceRows, BorderLayout.CENTER);
        JLabel foot = new JLabel("Attendance is entered manually by authorized HR staff.");
        foot.setFont(UITheme.SMALL);
        foot.setForeground(UITheme.MUTED);
        card.add(foot, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildRecentCard() {
        JPanel card = UITheme.card(18);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(22, 22, 22, 22));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Recently added employees");
        title.setFont(UITheme.H2);
        title.setForeground(UITheme.TEXT);
        JButton viewAll = new JButton("View all  →");
        viewAll.setFont(UITheme.BODY_BOLD);
        viewAll.setForeground(UITheme.BLUE);
        viewAll.setBorder(new EmptyBorder(4, 6, 4, 0));
        viewAll.setContentAreaFilled(false);
        viewAll.setFocusPainted(false);
        viewAll.addActionListener(event -> navigate.accept("Employees"));
        heading.add(title, BorderLayout.WEST);
        heading.add(viewAll, BorderLayout.EAST);
        card.add(heading, BorderLayout.NORTH);

        recentTable.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Employee", "Department", "Status"}) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        UITheme.styleTable(recentTable);
        recentTable.setRowHeight(36);
        JScrollPane scroll = new JScrollPane(recentTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        card.add(scroll, BorderLayout.CENTER);
        refreshLabel.setFont(UITheme.SMALL);
        refreshLabel.setForeground(UITheme.MUTED);
        card.add(refreshLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildQuickActions() {
        JPanel wrap = UITheme.card(16);
        wrap.setLayout(new BorderLayout(0, 13));
        wrap.setBorder(new EmptyBorder(17, 22, 17, 22));
        JLabel title = new JLabel("Quick actions");
        title.setFont(UITheme.H2);
        title.setForeground(UITheme.TEXT);
        wrap.add(title, BorderLayout.NORTH);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.add(actionButton("＋  Add employee", "Employees", UITheme.BLUE));
        actions.add(actionButton("◷  Record attendance", "Attendance", UITheme.TEAL));
        actions.add(actionButton("▤  Review leave", "Leave Requests", UITheme.PURPLE));
        actions.add(actionButton("▦  Departments", "Departments", UITheme.NAVY_2));
        wrap.add(actions, BorderLayout.CENTER);
        return wrap;
    }

    private JButton actionButton(String text, String destination, Color color) {
        JButton button = UITheme.button(text, UITheme.WHITE, color);
        button.addActionListener(event -> navigate.accept(destination));
        return button;
    }

    public void refreshData() {
        metricGrid.removeAll();
        try {
            long total = count("SELECT COUNT(*) FROM employees");
            long active = count("SELECT COUNT(*) FROM employees WHERE status = 'ACTIVE'");
            long departments = count("SELECT COUNT(*) FROM departments");
            long pending = count("SELECT COUNT(*) FROM leave_requests WHERE status = 'PENDING'");
            long presentToday = count("SELECT COUNT(*) FROM attendance WHERE attendance_date = CURRENT_DATE AND status = 'PRESENT'");
            metricGrid.add(metricCard("TOTAL EMPLOYEES", String.valueOf(total), "All employee profiles", "EMP", UITheme.BLUE, UITheme.BLUE_PALE));
            metricGrid.add(metricCard("ACTIVE EMPLOYEES", String.valueOf(active), "Currently active", "ACT", UITheme.TEAL, UITheme.GREEN_PALE));
            metricGrid.add(metricCard("DEPARTMENTS", String.valueOf(departments), "Organizational units", "ORG", UITheme.PURPLE, UITheme.PURPLE_PALE));
            metricGrid.add(metricCard("PENDING LEAVES", String.valueOf(pending), "Waiting for review", "LV", UITheme.ORANGE, UITheme.ORANGE_PALE));
            loadAttendanceRows(presentToday);
            loadRecentEmployees();
            refreshLabel.setText("Updated at " + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        } catch (SQLException ex) {
            metricGrid.add(metricCard("DATABASE STATUS", "Offline", "Check database setup", "!", UITheme.ORANGE, UITheme.ORANGE_PALE));
            try {
                loadAttendanceRows(0);
            } catch (SQLException ignored) {
                attendanceRows.removeAll();
                JLabel unavailable = new JLabel("Attendance summary is unavailable.");
                unavailable.setForeground(UITheme.MUTED);
                attendanceRows.add(unavailable);
            }
            recentTable.setModel(new DefaultTableModel(new Object[][]{{"Unavailable", "—", "—"}}, new String[]{"Employee", "Department", "Status"}));
            javax.swing.JOptionPane.showMessageDialog(this, "The dashboard could not load its summary.\n" + ex.getMessage(), "Dashboard", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        metricGrid.revalidate();
        metricGrid.repaint();
    }

    private JPanel metricCard(String heading, String value, String note, String mark, Color accent, Color pale) {
        JPanel card = UITheme.card(16);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(17, 18, 17, 18));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel label = new JLabel(heading);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        label.setForeground(UITheme.MUTED);
        JLabel badge = new JLabel(mark, SwingConstants.CENTER);
        badge.setFont(new Font("SansSerif", Font.BOLD, 10));
        badge.setForeground(accent);
        badge.setOpaque(true);
        badge.setBackground(pale);
        badge.setBorder(new EmptyBorder(7, 8, 7, 8));
        top.add(label, BorderLayout.WEST);
        top.add(badge, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);
        JLabel number = new JLabel(value);
        number.setFont(new Font("SansSerif", Font.BOLD, 30));
        number.setForeground(UITheme.TEXT);
        card.add(number, BorderLayout.CENTER);
        JLabel description = new JLabel(note);
        description.setFont(UITheme.SMALL);
        description.setForeground(UITheme.MUTED);
        card.add(description, BorderLayout.SOUTH);
        return card;
    }

    private void loadAttendanceRows(long presentToday) throws SQLException {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("PRESENT", 0L);
        counts.put("LATE", 0L);
        counts.put("ABSENT", 0L);
        counts.put("ON LEAVE", 0L);
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT status, COUNT(*) AS total FROM attendance WHERE attendance_date = CURRENT_DATE GROUP BY status");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) counts.put(result.getString("status"), result.getLong("total"));
        }
        attendanceRows.removeAll();
        attendanceRows.setLayout(new GridLayout(4, 1, 0, 11));
        addAttendanceRow("Present", counts.get("PRESENT"), UITheme.TEAL, UITheme.GREEN_PALE);
        addAttendanceRow("Late", counts.get("LATE"), UITheme.ORANGE, UITheme.ORANGE_PALE);
        addAttendanceRow("Absent", counts.get("ABSENT"), new Color(180, 35, 24), new Color(255, 235, 238));
        addAttendanceRow("On leave", counts.get("ON LEAVE"), UITheme.PURPLE, UITheme.PURPLE_PALE);
        attendanceRows.revalidate();
        attendanceRows.repaint();
    }

    private void addAttendanceRow(String label, long count, Color accent, Color pale) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        JLabel dot = new JLabel("●");
        dot.setForeground(accent);
        dot.setFont(new Font("SansSerif", Font.BOLD, 14));
        dot.setBorder(new EmptyBorder(0, 1, 0, 1));
        JLabel name = new JLabel(label);
        name.setFont(UITheme.BODY_BOLD);
        name.setForeground(UITheme.TEXT);
        JLabel value = new JLabel(String.valueOf(count), SwingConstants.CENTER);
        value.setFont(UITheme.BODY_BOLD);
        value.setForeground(accent);
        value.setOpaque(true);
        value.setBackground(pale);
        value.setBorder(new EmptyBorder(5, 10, 5, 10));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        left.setOpaque(false);
        left.add(dot); left.add(name);
        row.add(left, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        attendanceRows.add(row);
    }

    private void loadRecentEmployees() throws SQLException {
        String sql = "SELECT e.employee_no, e.first_name, e.last_name, d.department_name, e.status "
                   + "FROM employees e JOIN departments d ON e.department_id = d.department_id "
                   + "ORDER BY e.employee_id DESC FETCH FIRST 6 ROWS ONLY";
        DefaultTableModel model = new DefaultTableModel(new Object[][]{}, new String[]{"Employee", "Department", "Status"}) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                String name = result.getString("first_name") + " " + result.getString("last_name");
                model.addRow(new Object[]{name, result.getString("department_name"), result.getString("status")});
            }
        }
        if (model.getRowCount() == 0) model.addRow(new Object[]{"No employees yet", "Add a department first", "—"});
        recentTable.setModel(model);
        UITheme.styleTable(recentTable);
        recentTable.setRowHeight(36);
    }

    private long count(String sql) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getLong(1);
        }
    }

    private String firstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "there";
        return fullName.trim().split("\\s+")[0];
    }
}
