package hrms.ui;

import hrms.dao.GenericDAO;
import hrms.model.User;
import hrms.ui.EntityConfig.FieldDef;
import hrms.ui.EntityConfig.InputType;
import hrms.util.DBConnection;
import hrms.util.Validator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;

/** Modern reusable CRUD screen for the HRMS data modules. */
public final class EntityPanel extends JPanel {
    private final GenericDAO dao;
    private final EntityConfig config;
    private final User user;
    private final Map<String, JComponent> controls = new LinkedHashMap<>();
    private final JTable table = new JTable();
    private final JTextField searchField = new JTextField();
    private final JLabel countLabel = new JLabel("0 records");
    private Integer selectedId;
    private String loadedLeaveStatus = "PENDING";

    public EntityPanel(GenericDAO dao, User user) {
        super(new BorderLayout(0, 18));
        this.dao = dao;
        this.config = dao.getConfig();
        this.user = user;
        setBorder(new EmptyBorder(22, 26, 22, 26));
        setBackground(UITheme.BG);
        add(buildPageHeading(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() >= 0) loadSelectedRecord();
        });
        refreshTable();
    }

    private JPanel buildPageHeading() {
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = new JLabel(config.title + " management");
        title.setFont(UITheme.H2);
        title.setForeground(UITheme.TEXT);
        JLabel subtitle = new JLabel(descriptionFor(config.table));
        subtitle.setFont(UITheme.BODY);
        subtitle.setForeground(UITheme.MUTED);
        subtitle.setBorder(new EmptyBorder(5, 0, 0, 0));
        text.add(title);
        text.add(subtitle);
        heading.add(text, BorderLayout.WEST);
        JLabel connected = new JLabel("JDBC  •  APACHE DERBY");
        connected.setFont(new Font("SansSerif", Font.BOLD, 9));
        connected.setForeground(UITheme.TEAL);
        connected.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(190, 230, 220)), new EmptyBorder(8, 10, 8, 10)));
        connected.setOpaque(true);
        connected.setBackground(UITheme.GREEN_PALE);
        heading.add(connected, BorderLayout.EAST);
        return heading;
    }

    private String descriptionFor(String tableName) {
        return switch (tableName) {
            case "employees" -> "Create employee profiles, assign departments and positions, and maintain employment status.";
            case "departments" -> "Maintain the organizational departments used by employee records.";
            case "positions" -> "Maintain job titles and short descriptions of assigned duties.";
            case "attendance" -> "Record attendance using AM/PM. An earlier Time Out is treated as the following day for overnight shifts.";
            case "leave_requests" -> "Record leave requests and track their review status.";
            case "users" -> "Manage HRMS login accounts and role-based access.";
            default -> "Manage and maintain HRMS records.";
        };
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        JPanel editor = buildEditor();
        JPanel records = buildTableArea();
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editor, records);
        split.setBorder(null);
        split.setOpaque(false);
        split.setDividerSize(9);
        split.setResizeWeight(0.31);
        split.setDividerLocation(350);
        split.setContinuousLayout(true);
        content.add(split, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildEditor() {
        JPanel editor = UITheme.card(16);
        editor.setLayout(new BorderLayout(0, 12));
        editor.setBorder(new EmptyBorder(18, 18, 16, 18));
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Record details");
        title.setFont(UITheme.H2);
        title.setForeground(UITheme.TEXT);
        JLabel required = new JLabel("* Required field");
        required.setFont(UITheme.SMALL);
        required.setForeground(UITheme.MUTED);
        heading.add(title, BorderLayout.WEST);
        heading.add(required, BorderLayout.SOUTH);
        editor.add(heading, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setBackground(UITheme.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(1, 0, 3, 0);
        List<FieldDef> defs = config.fields;
        for (int i = 0; i < defs.size(); i++) {
            FieldDef field = defs.get(i);
            c.gridy = i * 2;
            c.insets = new Insets(i == 0 ? 0 : 8, 0, 4, 0);
            JLabel label = new JLabel(field.label + (field.required ? "  *" : ""));
            label.setFont(UITheme.BODY_BOLD);
            label.setForeground(UITheme.TEXT);
            fields.add(label, c);
            c.gridy++;
            c.insets = new Insets(0, 0, 2, 0);
            JComponent control = createControl(field);
            control.setPreferredSize(new Dimension(260, 38));
            if (control instanceof TimeInput timeInput) timeInput.styleTimeInput();
            else UITheme.styleInput(control);
            if (control instanceof JComboBox<?> combo) combo.setMaximumRowCount(12);
            fields.add(control, c);
            controls.put(field.name, control);
        }
        c.gridy = defs.size() * 2;
        c.weighty = 1;
        fields.add(new JPanel() {{ setOpaque(false); }}, c);
        JScrollPane fieldScroll = new JScrollPane(fields);
        fieldScroll.setBorder(null);
        fieldScroll.getVerticalScrollBar().setUnitIncrement(16);
        fieldScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        editor.add(fieldScroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridBagLayout());
        buttons.setOpaque(false);
        JButton add = UITheme.button("＋  Add new", UITheme.BLUE, Color.WHITE);
        JButton update = UITheme.button("Save changes", UITheme.NAVY_2, Color.WHITE);
        JButton delete = UITheme.button("Delete", new Color(255, 242, 243), new Color(180, 54, 65));
        delete.setEnabled(user.isAdministrator());
        delete.setToolTipText(user.isAdministrator() ? "Delete selected record" : "Only an Administrator may delete records.");
        JButton clear = UITheme.button("Clear form", UITheme.WHITE, UITheme.TEXT);
        add.addActionListener(event -> save(false));
        update.addActionListener(event -> save(true));
        delete.addActionListener(event -> deleteSelected());
        clear.addActionListener(event -> clearForm());
        GridBagConstraints b = new GridBagConstraints();
        b.fill = GridBagConstraints.HORIZONTAL;
        b.weightx = 1;
        b.insets = new Insets(4, 3, 4, 3);
        b.gridx = 0; b.gridy = 0; buttons.add(add, b);
        b.gridx = 1; buttons.add(update, b);
        b.gridx = 0; b.gridy = 1; buttons.add(delete, b);
        b.gridx = 1; buttons.add(clear, b);
        editor.add(buttons, BorderLayout.SOUTH);
        return editor;
    }

    private JComponent createControl(FieldDef field) {
        if (field.type == InputType.PASSWORD) {
            JPasswordField password = new JPasswordField();
            password.setToolTipText("Leave blank while editing to keep the current password.");
            return password;
        }
        if (field.type == InputType.COMBO) {
            JComboBox<String> combo = new JComboBox<>(field.options.toArray(String[]::new));
            if (field.name.equals("status") && config.table.equals("leave_requests") && !user.isAdministrator()) {
                combo.setSelectedItem("PENDING");
                combo.setEnabled(false);
            }
            return combo;
        }
        if (field.type == InputType.FOREIGN_KEY) {
            JComboBox<LookupOption> combo = new JComboBox<>();
            loadLookupOptions(combo, field);
            return combo;
        }
        if (field.type == InputType.TIME) return new TimeInput();
        JTextField text = new JTextField();
        if (!field.defaultValue.isBlank()) text.setText(field.defaultValue);
        if (field.type == InputType.DATE) text.setToolTipText("Enter date as YYYY-MM-DD, e.g. " + LocalDate.now());
        return text;
    }

    private void loadLookupOptions(JComboBox<LookupOption> combo, FieldDef field) {
        try (var connection = DBConnection.getConnection();
             var statement = connection.createStatement();
             var result = statement.executeQuery(field.lookupSql)) {
            while (result.next()) combo.addItem(new LookupOption(result.getInt(1), result.getString(2)));
        } catch (SQLException ex) {
            combo.setToolTipText("Add the related records first. " + ex.getMessage());
        }
    }

    private JPanel buildTableArea() {
        JPanel area = UITheme.card(16);
        area.setLayout(new BorderLayout(0, 14));
        area.setBorder(new EmptyBorder(18, 18, 16, 18));
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        JPanel labelLine = new JPanel(new BorderLayout());
        labelLine.setOpaque(false);
        JLabel title = new JLabel("Saved records");
        title.setFont(UITheme.H2);
        title.setForeground(UITheme.TEXT);
        countLabel.setFont(UITheme.BODY_BOLD);
        countLabel.setForeground(UITheme.BLUE);
        labelLine.add(title, BorderLayout.WEST);
        labelLine.add(countLabel, BorderLayout.EAST);
        top.add(labelLine, BorderLayout.NORTH);

        JPanel search = new JPanel(new BorderLayout(8, 0));
        search.setOpaque(false);
        searchField.setFont(UITheme.BODY);
        searchField.setToolTipText("Search by keyword");
        UITheme.styleInput(searchField);
        JButton find = UITheme.button("Search", UITheme.BLUE, Color.WHITE);
        JButton refresh = UITheme.button("↻ Refresh", UITheme.WHITE, UITheme.TEXT);
        find.addActionListener(event -> refreshTable());
        searchField.addActionListener(event -> refreshTable());
        refresh.addActionListener(event -> { searchField.setText(""); refreshTable(); });
        JPanel action = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        action.setOpaque(false);
        action.add(find);
        action.add(refresh);
        search.add(searchField, BorderLayout.CENTER);
        search.add(action, BorderLayout.EAST);
        top.add(search, BorderLayout.CENTER);
        area.add(top, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        area.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JLabel note = new JLabel("Select a row to load its details into the form.");
        note.setFont(UITheme.SMALL);
        note.setForeground(UITheme.MUTED);
        JLabel db = new JLabel("JDBC  •  Apache Derby");
        db.setFont(UITheme.SMALL);
        db.setForeground(UITheme.MUTED);
        bottom.add(note, BorderLayout.WEST);
        bottom.add(db, BorderLayout.EAST);
        area.add(bottom, BorderLayout.SOUTH);
        return area;
    }

    private void save(boolean update) {
        if ("users".equals(config.table) && !user.isAdministrator()) {
            message("Only an Administrator can manage user accounts.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (update && selectedId == null) {
            message("Select a record from the table before saving changes.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Map<String, Object> values = collectValues(update);
            if ("users".equals(config.table) && update && selectedId != null) {
                Map<String, Object> existing = dao.findById(selectedId);
                if (existing != null && "ADMIN".equalsIgnoreCase(String.valueOf(existing.get("role")))
                        && !"ADMIN".equalsIgnoreCase(String.valueOf(values.get("role")))
                        && countAdministrators() <= 1) {
                    throw new IllegalArgumentException("The last Administrator account cannot be changed to HR Staff.");
                }
            }
            boolean overnightAttendance = isOvernightAttendance(values);
            if (update) dao.update(selectedId, values); else dao.insert(values);
            String successMessage = update ? "Record updated successfully." : "Record added successfully.";
            if (overnightAttendance) {
                successMessage += "\nTime Out is earlier than Time In, so it is treated as the following day.";
            }
            message(successMessage, JOptionPane.INFORMATION_MESSAGE);
            clearForm();
            refreshTable();
            reloadLookupsIfNeeded();
        } catch (IllegalArgumentException ex) {
            message(ex.getMessage(), JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            message(friendlySqlError(ex), JOptionPane.ERROR_MESSAGE);
        }
    }

    private Map<String, Object> collectValues(boolean update) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (FieldDef field : config.fields) {
            Object value;
            JComponent component = controls.get(field.name);
            if (component instanceof JPasswordField password) value = new String(password.getPassword());
            else if (component instanceof TimeInput timeInput && field.type == InputType.TIME) value = timeInput.get24HourValue(field.label);
            else if (component instanceof JComboBox<?> combo && field.type == InputType.FOREIGN_KEY) {
                LookupOption option = (LookupOption) combo.getSelectedItem();
                value = option == null ? "" : Integer.toString(option.id);
            } else if (component instanceof JComboBox<?> combo) value = combo.getSelectedItem() == null ? "" : combo.getSelectedItem().toString();
            else if (component instanceof JTextField text) value = text.getText().trim();
            else value = "";

            String textValue = value == null ? "" : value.toString().trim();
            if (field.required && textValue.isBlank() && !(update && field.type == InputType.PASSWORD)) {
                throw new IllegalArgumentException(field.label.replace(" (YYYY-MM-DD)", "").replace(" (HH:MM)", "") + " is required.");
            }
            if (field.type == InputType.DATE && !textValue.isBlank()) Validator.parseDate(textValue, field.label);
            if (field.type == InputType.TIME && !textValue.isBlank()) Validator.parseTime(textValue, field.label);
            if (field.name.equals("email")) Validator.validateEmail(textValue);
            values.put(field.name, textValue.isBlank() ? null : value);
        }
        if (config.table.equals("leave_requests")) {
            Object start = values.get("start_date");
            Object end = values.get("end_date");
            if (start != null && end != null && end.toString().compareTo(start.toString()) < 0) {
                throw new IllegalArgumentException("End Date cannot be earlier than Start Date.");
            }
            if (!user.isAdministrator()) values.put("status", update && selectedId != null ? loadedLeaveStatus : "PENDING");
        }
        // For attendance, Time Out earlier than Time In is valid and means the shift ended
        // on the following day. We keep attendance_date as the shift's start date.
        return values;
    }

    private boolean isOvernightAttendance(Map<String, Object> values) {
        if (!config.table.equals("attendance")) return false;
        Object in = values.get("time_in");
        Object out = values.get("time_out");
        if (in == null || out == null) return false;
        return Validator.parseTime(out.toString(), "Time Out")
            .before(Validator.parseTime(in.toString(), "Time In"));
    }

    private void deleteSelected() {
        if (!user.isAdministrator()) {
            message("Only an Administrator may delete records.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("users".equals(config.table) && selectedId != null && selectedId == user.getUserId()) {
            message("You cannot delete the account that is currently signed in.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ("users".equals(config.table) && selectedId != null) {
            try {
                Map<String, Object> selected = dao.findById(selectedId);
                if (selected != null && "ADMIN".equalsIgnoreCase(String.valueOf(selected.get("role"))) && countAdministrators() <= 1) {
                    message("The last Administrator account cannot be deleted.", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (SQLException ex) {
                message(friendlySqlError(ex), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (selectedId == null) {
            message("Select a record from the table before deleting.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
            "Delete the selected " + config.title.toLowerCase() + " record?\nThis action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(selectedId);
            clearForm();
            refreshTable();
            reloadLookupsIfNeeded();
            message("Record deleted successfully.", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            message(friendlySqlError(ex), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedRecord() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object idValue = table.getModel().getValueAt(modelRow, 0);
        if (!(idValue instanceof Number)) return;
        selectedId = ((Number) idValue).intValue();
        try {
            Map<String, Object> record = dao.findById(selectedId);
            if (record == null) return;
            if (config.table.equals("leave_requests") && record.get("status") != null) loadedLeaveStatus = record.get("status").toString();
            for (FieldDef field : config.fields) {
                JComponent component = controls.get(field.name);
                Object value = record.get(field.name);
                if (field.type == InputType.PASSWORD) {
                    ((JPasswordField) component).setText("");
                    continue;
                }
                if (component instanceof JComboBox<?> combo && field.type == InputType.FOREIGN_KEY) {
                    for (int i = 0; i < combo.getItemCount(); i++) {
                        LookupOption option = (LookupOption) combo.getItemAt(i);
                        if (value instanceof Number number && option.id == number.intValue()) { combo.setSelectedIndex(i); break; }
                    }
                } else if (component instanceof TimeInput timeInput) {
                    timeInput.setFromDatabase(value);
                } else if (component instanceof JComboBox<?> combo && value != null) {
                    combo.setSelectedItem(value.toString());
                } else if (component instanceof JTextField text) {
                    text.setText(value == null ? "" : value.toString());
                }
            }
        } catch (SQLException ex) {
            message(friendlySqlError(ex), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        selectedId = null;
        table.clearSelection();
        for (FieldDef field : config.fields) {
            JComponent component = controls.get(field.name);
            if (component instanceof JPasswordField password) password.setText("");
            else if (component instanceof TimeInput timeInput) timeInput.clearTime();
            else if (component instanceof JTextField text) text.setText(field.defaultValue);
            else if (component instanceof JComboBox<?> combo) {
                if (field.name.equals("status") && config.table.equals("leave_requests") && !user.isAdministrator()) combo.setSelectedItem("PENDING");
                else if (combo.getItemCount() > 0) combo.setSelectedIndex(0);
            }
        }
    }

    public void refreshPanel() {
        reloadLookupsIfNeeded();
        refreshTable();
    }

    private void refreshTable() {
        selectedId = null;
        table.clearSelection();
        try {
            DefaultTableModel model = dao.list(searchField.getText());
            table.setModel(model);
            UITheme.styleTable(table);
            configureReadableColumns();
            countLabel.setText(model.getRowCount() + (model.getRowCount() == 1 ? " record" : " records"));
        } catch (SQLException ex) {
            countLabel.setText("Unable to load");
            message(friendlySqlError(ex), JOptionPane.ERROR_MESSAGE);
        }
    }


    /** Keep long field names and values readable; use horizontal scrolling rather than truncating every column. */
    private void configureReadableColumns() {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int index = 0; index < table.getColumnModel().getColumnCount(); index++) {
            String column = table.getColumnName(index).toLowerCase(java.util.Locale.ROOT);
            int width = switch (column) {
                case "id", "user_id", "department_id", "position_id", "employee_id", "attendance_id", "leave_id" -> 72;
                case "employee_no", "date_hired", "birth_date", "attendance_date", "start_date", "end_date", "date_filed" -> 118;
                case "first_name", "last_name", "department_name", "position_title", "leave_type", "status", "role", "gender" -> 125;
                case "email" -> 185;
                case "phone" -> 130;
                case "address", "description", "reason", "full_name" -> 205;
                case "time_in", "time_out" -> 100;
                case "username" -> 135;
                default -> Math.max(110, Math.min(210, column.length() * 9 + 35));
            };
            table.getColumnModel().getColumn(index).setPreferredWidth(width);
        }
    }

    private void reloadLookupsIfNeeded() {
        for (FieldDef field : config.fields) {
            if (field.type != InputType.FOREIGN_KEY) continue;
            JComponent comp = controls.get(field.name);
            if (!(comp instanceof JComboBox<?> combo)) continue;
            @SuppressWarnings("unchecked") JComboBox<LookupOption> typedCombo = (JComboBox<LookupOption>) combo;
            Object keep = typedCombo.getSelectedItem() instanceof LookupOption selected ? selected.id : null;
            typedCombo.removeAllItems();
            loadLookupOptions(typedCombo, field);
            if (keep != null) {
                for (int i = 0; i < typedCombo.getItemCount(); i++) {
                    if (typedCombo.getItemAt(i).id == (Integer) keep) typedCombo.setSelectedIndex(i);
                }
            }
        }
    }

    private long countAdministrators() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'");
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getLong(1);
        }
    }

    private String friendlySqlError(SQLException ex) {
        String state = ex.getSQLState();
        if (state != null && state.startsWith("23")) {
            if (state.equals("23505")) return "A record with that unique value already exists. Check the employee number, username, department name, or position title.";
            if (state.equals("23503")) return "This record is referenced by another record and cannot be deleted or changed yet. Remove or update the related records first.";
            if (state.equals("23513")) return "The value violates a database rule. Check the selected status and date values.";
            return "The data could not be saved because it violates a database constraint. Check the values and required relationships.";
        }
        String message = ex.getMessage();
        return "The database operation could not be completed." + (message == null ? "" : "\n" + message);
    }

    private void message(String text, int kind) {
        JOptionPane.showMessageDialog(this, text, config.title, kind);
    }

    /** A 12-hour time editor with mutually exclusive AM and PM checkboxes. */
    private final class TimeInput extends JPanel {
        private final JTextField timeText = new JTextField();
        private final JCheckBox amCheck = new JCheckBox("AM");
        private final JCheckBox pmCheck = new JCheckBox("PM");

        private TimeInput() {
            super(new BorderLayout(6, 0));
            setOpaque(false);
            setToolTipText("Enter hour:minute and select AM or PM. Time Out earlier than Time In is treated as next day.");
            timeText.setToolTipText("Enter time as h:mm, for example 07:00");
            timeText.setPreferredSize(new Dimension(106, 36));

            ButtonGroup periodGroup = new ButtonGroup();
            periodGroup.add(amCheck);
            periodGroup.add(pmCheck);
            amCheck.setSelected(true);
            // Keep one period selected even though JCheckBox normally permits deselection.
            amCheck.addActionListener(event -> {
                if (!amCheck.isSelected() && !pmCheck.isSelected()) amCheck.setSelected(true);
            });
            pmCheck.addActionListener(event -> {
                if (!amCheck.isSelected() && !pmCheck.isSelected()) pmCheck.setSelected(true);
            });

            JPanel periodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            periodPanel.setOpaque(false);
            periodPanel.add(amCheck);
            periodPanel.add(pmCheck);
            add(timeText, BorderLayout.CENTER);
            add(periodPanel, BorderLayout.EAST);
        }

        private void styleTimeInput() {
            UITheme.styleInput(timeText);
            amCheck.setFont(UITheme.SMALL);
            pmCheck.setFont(UITheme.SMALL);
            amCheck.setForeground(UITheme.TEXT);
            pmCheck.setForeground(UITheme.TEXT);
            amCheck.setOpaque(false);
            pmCheck.setOpaque(false);
        }

        private String get24HourValue(String label) {
            return Validator.parseAmPmTime(timeText.getText(), pmCheck.isSelected() ? "PM" : "AM", label);
        }

        private void setFromDatabase(Object value) {
            if (value == null) {
                clearTime();
                return;
            }
            LocalTime localTime;
            if (value instanceof java.sql.Time sqlTime) {
                localTime = sqlTime.toLocalTime();
            } else {
                String text = value.toString();
                if (text.length() > 8) text = text.substring(0, 8);
                localTime = LocalTime.parse(text);
            }
            int hour24 = localTime.getHour();
            int hour12 = hour24 % 12;
            if (hour12 == 0) hour12 = 12;
            timeText.setText(String.format(Locale.ROOT, "%02d:%02d", hour12, localTime.getMinute()));
            if (hour24 >= 12) pmCheck.setSelected(true);
            else amCheck.setSelected(true);
        }

        private void clearTime() {
            timeText.setText("");
            amCheck.setSelected(true);
        }
    }

    private static final class LookupOption {
        private final int id;
        private final String label;
        private LookupOption(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }
}
