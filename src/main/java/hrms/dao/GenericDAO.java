package hrms.dao;

import hrms.ui.EntityConfig;
import hrms.ui.EntityConfig.FieldDef;
import hrms.ui.EntityConfig.InputType;
import hrms.util.DBConnection;
import hrms.util.PasswordUtil;
import hrms.util.Validator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.table.DefaultTableModel;

/** Shared prepared-statement CRUD implementation for the system's fixed entity configurations. */
public class GenericDAO {
    protected final EntityConfig config;

    public GenericDAO(EntityConfig config) { this.config = config; }
    public EntityConfig getConfig() { return config; }

    public DefaultTableModel list(String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder(config.displaySql);
        boolean searching = keyword != null && !keyword.isBlank() && !config.searchExpressions.isEmpty();
        if (searching) {
            sql.append(" WHERE (");
            for (int i = 0; i < config.searchExpressions.size(); i++) {
                if (i > 0) sql.append(" OR ");
                sql.append("LOWER(CAST(").append(config.searchExpressions.get(i)).append(" AS VARCHAR(255))) LIKE ?");
            }
            sql.append(")");
        }
        sql.append(" ORDER BY 1 DESC");
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (searching) {
                String like = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                for (int i = 1; i <= config.searchExpressions.size(); i++) ps.setString(i, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                String[] columns = new String[md.getColumnCount()];
                for (int i = 1; i <= columns.length; i++) columns[i - 1] = md.getColumnLabel(i);
                DefaultTableModel model = new DefaultTableModel(columns, 0) {
                    @Override public boolean isCellEditable(int row, int column) { return false; }
                };
                while (rs.next()) {
                    Object[] row = new Object[columns.length];
                    for (int i = 1; i <= row.length; i++) row[i - 1] = rs.getObject(i);
                    if ("attendance".equals(config.table)) formatAttendanceTimes(columns, row);
                    model.addRow(row);
                }
                return model;
            }
        }
    }

    public Map<String, Object> findById(int id) throws SQLException {
        String sql = "SELECT * FROM " + config.table + " WHERE " + config.idColumn + " = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ResultSetMetaData md = rs.getMetaData();
                Map<String, Object> record = new LinkedHashMap<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    record.put(md.getColumnLabel(i).toLowerCase(Locale.ROOT), rs.getObject(i));
                }
                return record;
            }
        }
    }

    public void insert(Map<String, Object> values) throws SQLException {
        List<FieldDef> fields = config.fields;
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(config.table).append(" (");
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) { sql.append(", "); placeholders.append(", "); }
            sql.append(fields.get(i).name);
            placeholders.append("?");
        }
        sql.append(") VALUES (").append(placeholders).append(")");
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < fields.size(); i++) bind(ps, i + 1, fields.get(i), values.get(fields.get(i).name));
            ps.executeUpdate();
        }
    }

    public void update(int id, Map<String, Object> values) throws SQLException {
        List<FieldDef> updateFields = new ArrayList<>();
        for (FieldDef field : config.fields) {
            Object value = values.get(field.name);
            // A blank password on edit means "keep the existing password".
            if (field.type == InputType.PASSWORD && (value == null || value.toString().isBlank())) continue;
            updateFields.add(field);
        }
        if (updateFields.isEmpty()) return;
        StringBuilder sql = new StringBuilder("UPDATE ").append(config.table).append(" SET ");
        for (int i = 0; i < updateFields.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append(updateFields.get(i).name).append(" = ?");
        }
        sql.append(" WHERE ").append(config.idColumn).append(" = ?");
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int index = 1;
            for (FieldDef field : updateFields) bind(ps, index++, field, values.get(field.name));
            ps.setInt(index, id);
            if (ps.executeUpdate() == 0) throw new SQLException("The selected record no longer exists.");
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM " + config.table + " WHERE " + config.idColumn + " = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) throw new SQLException("The selected record no longer exists.");
        }
    }

    private static void formatAttendanceTimes(String[] columns, Object[] row) {
        int timeInIndex = columnIndex(columns, "Time In");
        int timeOutIndex = columnIndex(columns, "Time Out");
        if (timeInIndex < 0 || timeOutIndex < 0) return;
        LocalTime timeIn = toLocalTime(row[timeInIndex]);
        LocalTime timeOut = toLocalTime(row[timeOutIndex]);
        row[timeInIndex] = formatTime(timeIn);
        String formattedOut = formatTime(timeOut);
        if (timeIn != null && timeOut != null && timeOut.isBefore(timeIn)) formattedOut += " (next day)";
        row[timeOutIndex] = formattedOut;
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

    private void bind(PreparedStatement ps, int index, FieldDef field, Object raw) throws SQLException {
        if (raw == null || raw.toString().isBlank()) {
            int sqlType = switch (field.type) {
                case DATE -> Types.DATE;
                case TIME -> Types.TIME;
                case FOREIGN_KEY -> Types.INTEGER;
                default -> Types.VARCHAR;
            };
            ps.setNull(index, sqlType);
            return;
        }
        String value = raw.toString().trim();
        try {
            switch (field.type) {
                case PASSWORD -> ps.setString(index, PasswordUtil.hash(value));
                case DATE -> ps.setDate(index, Validator.parseDate(value, field.label));
                case TIME -> ps.setTime(index, Validator.parseTime(value, field.label));
                case FOREIGN_KEY -> ps.setInt(index, Integer.parseInt(value));
                default -> ps.setString(index, value);
            }
        } catch (IllegalArgumentException ex) {
            throw new SQLException(ex.getMessage(), ex);
        }
    }
}
