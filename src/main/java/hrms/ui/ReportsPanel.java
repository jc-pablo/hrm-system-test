package hrms.ui;

import hrms.util.DBConnection;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;

/** Read-only reports for common HR tasks, with CSV export. */
public final class ReportsPanel extends JPanel {
    private record ReportDefinition(String name, String sql) {
        @Override public String toString() { return name; }
    }

    private final JComboBox<ReportDefinition> reportPicker = new JComboBox<>(new ReportDefinition[] {
        new ReportDefinition("Employee Directory", "SELECT e.employee_no AS \"Employee No.\", e.first_name AS \"First Name\", e.last_name AS \"Last Name\", e.status AS Status, d.department_name AS Department, p.position_title AS Position, e.email AS Email, e.phone AS Phone FROM employees e JOIN departments d ON e.department_id = d.department_id JOIN positions p ON e.position_id = p.position_id ORDER BY e.last_name, e.first_name"),
        new ReportDefinition("Department Headcount", "SELECT d.department_name AS Department, COUNT(e.employee_id) AS \"Employee Count\" FROM departments d LEFT JOIN employees e ON e.department_id = d.department_id GROUP BY d.department_id, d.department_name ORDER BY d.department_name"),
        new ReportDefinition("Attendance Register", "SELECT a.attendance_date AS \"Attendance Date\", e.employee_no AS \"Employee No.\", e.first_name || ' ' || e.last_name AS Employee, a.time_in AS \"Time In\", a.time_out AS \"Time Out\", a.status AS Status FROM attendance a JOIN employees e ON e.employee_id = a.employee_id ORDER BY a.attendance_date DESC, e.last_name, e.first_name"),
        new ReportDefinition("Leave Register", "SELECT e.employee_no AS \"Employee No.\", e.first_name || ' ' || e.last_name AS Employee, l.leave_type AS \"Leave Type\", l.start_date AS \"Start Date\", l.end_date AS \"End Date\", l.status AS Status, l.reason AS Reason, l.date_filed AS \"Date Filed\" FROM leave_requests l JOIN employees e ON e.employee_id = l.employee_id ORDER BY l.date_filed DESC"),
        new ReportDefinition("Pending Leave Requests", "SELECT e.employee_no AS \"Employee No.\", e.first_name || ' ' || e.last_name AS Employee, l.leave_type AS \"Leave Type\", l.start_date AS \"Start Date\", l.end_date AS \"End Date\", l.reason AS Reason, l.date_filed AS \"Date Filed\" FROM leave_requests l JOIN employees e ON e.employee_id = l.employee_id WHERE l.status = 'PENDING' ORDER BY l.date_filed DESC")
    });
    private final JTable table = new JTable();
    private final JLabel resultSummary = new JLabel("Choose a report and click Refresh.");
    private DefaultTableModel currentModel = emptyModel();

    public ReportsPanel() {
        super(new BorderLayout(0, 18));
        setBackground(UITheme.BG);
        setBorder(new EmptyBorder(24, 26, 24, 26));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildResults(), BorderLayout.CENTER);
        UITheme.styleTable(table);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        refreshReport();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 14));
        header.setOpaque(false);
        JPanel titleArea = new JPanel();
        titleArea.setOpaque(false);
        titleArea.setLayout(new javax.swing.BoxLayout(titleArea, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = new JLabel("HR Reports");
        title.setFont(UITheme.H1);
        title.setForeground(UITheme.TEXT);
        JLabel subtitle = new JLabel("View useful summaries of employee, attendance, and leave information.");
        subtitle.setFont(UITheme.BODY);
        subtitle.setForeground(UITheme.MUTED);
        subtitle.setBorder(new EmptyBorder(5, 0, 0, 0));
        titleArea.add(title); titleArea.add(subtitle);
        header.add(titleArea, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);
        JLabel reportLabel = new JLabel("Report");
        reportLabel.setFont(UITheme.BODY_BOLD);
        reportLabel.setForeground(UITheme.TEXT);
        reportPicker.setFont(UITheme.BODY);
        reportPicker.setBackground(Color.WHITE);
        reportPicker.setPreferredSize(new java.awt.Dimension(260, 40));
        JButton refresh = UITheme.button("Refresh report", UITheme.BLUE, Color.WHITE);
        JButton export = UITheme.button("Export CSV", UITheme.WHITE, UITheme.TEXT);
        refresh.addActionListener(event -> refreshReport());
        reportPicker.addActionListener(event -> refreshReport());
        export.addActionListener(event -> exportCsv());
        controls.add(reportLabel); controls.add(reportPicker); controls.add(refresh); controls.add(export);
        header.add(controls, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildResults() {
        JPanel card = UITheme.card(16);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel title = new JLabel("Report results");
        title.setFont(UITheme.H2);
        title.setForeground(UITheme.TEXT);
        resultSummary.setFont(UITheme.BODY);
        resultSummary.setForeground(UITheme.MUTED);
        top.add(title, BorderLayout.WEST); top.add(resultSummary, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);
        JLabel note = new JLabel("Reports are read-only. Overnight shifts are assigned to their start date; Time Out before Time In is marked as next day. Export saves the displayed rows to CSV.");
        note.setFont(UITheme.SMALL); note.setForeground(UITheme.MUTED);
        card.add(note, BorderLayout.SOUTH);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false); wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    public void refreshData() { refreshReport(); }

    private void refreshReport() {
        ReportDefinition report = (ReportDefinition) reportPicker.getSelectedItem();
        if (report == null) return;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(report.sql());
             ResultSet result = statement.executeQuery()) {
            ResultSetMetaData meta = result.getMetaData();
            String[] columns = new String[meta.getColumnCount()];
            for (int i = 0; i < columns.length; i++) columns[i] = meta.getColumnLabel(i + 1);
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            while (result.next()) {
                Object[] values = new Object[columns.length];
                for (int i = 0; i < values.length; i++) values[i] = result.getObject(i + 1);
                if (report.name().equals("Attendance Register")) formatAttendanceTimes(columns, values);
                model.addRow(values);
            }
            currentModel = model;
            table.setModel(model);
            UITheme.styleTable(table);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            for (int column = 0; column < table.getColumnModel().getColumnCount(); column++) {
                table.getColumnModel().getColumn(column).setPreferredWidth(150);
            }
            resultSummary.setText(model.getRowCount() + (model.getRowCount() == 1 ? " record" : " records"));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "The report could not be loaded.\n" + ex.getMessage(), "Report Error", JOptionPane.ERROR_MESSAGE);
            resultSummary.setText("Unable to load report");
        }
    }

    private static void formatAttendanceTimes(String[] columns, Object[] values) {
        int inIndex = columnIndex(columns, "Time In");
        int outIndex = columnIndex(columns, "Time Out");
        if (inIndex < 0 || outIndex < 0) return;
        LocalTime timeIn = toLocalTime(values[inIndex]);
        LocalTime timeOut = toLocalTime(values[outIndex]);
        values[inIndex] = formatTime(timeIn);
        String formattedOut = formatTime(timeOut);
        if (timeIn != null && timeOut != null && timeOut.isBefore(timeIn)) formattedOut += " (next day)";
        values[outIndex] = formattedOut;
    }

    private static int columnIndex(String[] columns, String target) {
        for (int i = 0; i < columns.length; i++) {
            if (target.equalsIgnoreCase(columns[i])) return i;
        }
        return -1;
    }

    private static LocalTime toLocalTime(Object value) {
        if (value == null) return null;
        if (value instanceof java.sql.Time time) return time.toLocalTime();
        if (value instanceof LocalTime time) return time;
        String text = value.toString();
        if (text.length() > 8) text = text.substring(0, 8);
        try { return LocalTime.parse(text); }
        catch (RuntimeException ex) { return null; }
    }

    private static String formatTime(LocalTime time) {
        return time == null ? "" : time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
    }

    private void exportCsv() {
        if (currentModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "There are no rows to export.", "Export CSV", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save HR Report as CSV");
        chooser.setSelectedFile(new java.io.File("hrms-report.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try (BufferedWriter writer = Files.newBufferedWriter(chooser.getSelectedFile().toPath(), StandardCharsets.UTF_8)) {
            for (int column = 0; column < currentModel.getColumnCount(); column++) {
                if (column > 0) writer.write(',');
                writer.write(csv(currentModel.getColumnName(column)));
            }
            writer.newLine();
            for (int row = 0; row < currentModel.getRowCount(); row++) {
                for (int column = 0; column < currentModel.getColumnCount(); column++) {
                    if (column > 0) writer.write(',');
                    Object value = currentModel.getValueAt(row, column);
                    writer.write(csv(value == null ? "" : value.toString()));
                }
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Report exported successfully.", "Export CSV", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "The report could not be exported.\n" + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String csv(String value) {
        String safe = value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private static DefaultTableModel emptyModel() {
        return new DefaultTableModel(new Object[0][0], new Object[0]) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }
}
