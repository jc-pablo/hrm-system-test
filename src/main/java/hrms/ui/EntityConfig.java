package hrms.ui;

import java.util.List;

/** Metadata used by the reusable CRUD panel and common JDBC repository. */
public final class EntityConfig {
    public enum InputType { TEXT, PASSWORD, DATE, TIME, COMBO, FOREIGN_KEY }

    public static final class FieldDef {
        public final String name;
        public final String label;
        public final InputType type;
        public final boolean required;
        public final List<String> options;
        public final String lookupSql;
        public final String defaultValue;

        public FieldDef(String name, String label, InputType type, boolean required,
                        List<String> options, String lookupSql, String defaultValue) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.required = required;
            this.options = options == null ? List.of() : List.copyOf(options);
            this.lookupSql = lookupSql;
            this.defaultValue = defaultValue == null ? "" : defaultValue;
        }

        public static FieldDef text(String name, String label, boolean required) {
            return new FieldDef(name, label, InputType.TEXT, required, null, null, "");
        }
        public static FieldDef password(String name, String label, boolean required) {
            return new FieldDef(name, label, InputType.PASSWORD, required, null, null, "");
        }
        public static FieldDef date(String name, String label, boolean required, String defaultValue) {
            return new FieldDef(name, label, InputType.DATE, required, null, null, defaultValue);
        }
        public static FieldDef time(String name, String label, boolean required) {
            return new FieldDef(name, label, InputType.TIME, required, null, null, "");
        }
        public static FieldDef combo(String name, String label, boolean required, String... options) {
            return new FieldDef(name, label, InputType.COMBO, required, List.of(options), null, "");
        }
        public static FieldDef foreignKey(String name, String label, boolean required, String lookupSql) {
            return new FieldDef(name, label, InputType.FOREIGN_KEY, required, null, lookupSql, "");
        }
    }

    public final String title;
    public final String table;
    public final String idColumn;
    public final String displaySql;
    public final List<FieldDef> fields;
    public final List<String> searchExpressions;

    public EntityConfig(String title, String table, String idColumn, String displaySql,
                        List<FieldDef> fields, List<String> searchExpressions) {
        this.title = title;
        this.table = table;
        this.idColumn = idColumn;
        this.displaySql = displaySql;
        this.fields = List.copyOf(fields);
        this.searchExpressions = List.copyOf(searchExpressions);
    }
}
