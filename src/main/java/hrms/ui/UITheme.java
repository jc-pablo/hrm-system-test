package hrms.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;

/** Shared visual styling for the HRMS desktop interface. */
public final class UITheme {
    public static final Color NAVY = new Color(20, 43, 74);
    public static final Color NAVY_2 = new Color(29, 62, 101);
    // Darker action/accent colors are chosen to keep text readable on light surfaces
    // and to maintain strong contrast with white text on filled buttons.
    public static final Color BLUE = new Color(31, 83, 154);       // #1F539A
    public static final Color TEAL = new Color(0, 105, 92);        // #00695C
    public static final Color PURPLE = new Color(87, 60, 143);     // #573C8F
    public static final Color ORANGE = new Color(138, 75, 8);      // #8A4B08
    public static final Color BG = new Color(245, 247, 251);
    public static final Color WHITE = Color.WHITE;
    public static final Color TEXT = new Color(38, 51, 72);
    public static final Color MUTED = new Color(86, 101, 121);
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color GREEN_PALE = new Color(230, 247, 242);
    public static final Color BLUE_PALE = new Color(234, 241, 255);
    public static final Color PURPLE_PALE = new Color(242, 237, 253);
    public static final Color ORANGE_PALE = new Color(255, 244, 230);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BODY_BOLD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font SMALL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font H1 = new Font("SansSerif", Font.BOLD, 27);
    public static final Font H2 = new Font("SansSerif", Font.BOLD, 18);

    static {
        // BasicButtonUI uses this UI default for disabled text; keep it darker than
        // the common system look-and-feel default so disabled labels remain legible.
        UIManager.put("Button.disabledText", new Color(70, 80, 94));
    }

    private UITheme() { }

    public static JButton button(String text, Color background, Color foreground) {
        Color baseBackground = background;
        Color baseForeground = readableForeground(baseBackground, foreground);

        JButton button = new JButton(text);
        // BasicButtonUI reliably respects the explicit theme colors across installed
        // system look-and-feels instead of substituting low-contrast default states.
        button.setUI(new BasicButtonUI());
        button.setFont(BODY_BOLD);
        button.setForeground(baseForeground);
        button.setBackground(baseBackground);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(baseBackground.equals(WHITE) ? BORDER : baseBackground.darker()),
            new EmptyBorder(9, 14, 9, 14)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setRolloverEnabled(true);
        button.setMargin(new Insets(0, 0, 0, 0));

        Color hoverBackground = blend(baseBackground, isDark(baseBackground) ? WHITE : Color.BLACK, 0.07f);
        Color pressedBackground = blend(baseBackground, Color.BLACK, 0.12f);
        Color disabledBackground = new Color(232, 236, 242);
        Color disabledForeground = new Color(70, 80, 94);
        button.addPropertyChangeListener("enabled", event -> {
            if (Boolean.FALSE.equals(event.getNewValue())) {
                applyButtonColors(button, disabledBackground, disabledForeground);
            } else {
                applyButtonColors(button, baseBackground, baseForeground);
            }
        });
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent event) {
                if (button.isEnabled()) applyButtonColors(button, hoverBackground, baseForeground);
            }
            @Override public void mouseExited(MouseEvent event) {
                if (button.isEnabled()) applyButtonColors(button, baseBackground, baseForeground);
            }
            @Override public void mousePressed(MouseEvent event) {
                if (button.isEnabled()) applyButtonColors(button, pressedBackground, baseForeground);
            }
            @Override public void mouseReleased(MouseEvent event) {
                if (!button.isEnabled()) return;
                Color next = button.contains(event.getPoint()) ? hoverBackground : baseBackground;
                applyButtonColors(button, next, baseForeground);
            }
        });
        return button;
    }

    private static void applyButtonColors(JButton button, Color background, Color preferredForeground) {
        button.setBackground(background);
        button.setForeground(readableForeground(background, preferredForeground));
    }

    /** Returns the preferred text color if it meets WCAG AA contrast for normal text;
     * otherwise uses whichever of a dark ink color or white has better contrast. */
    private static Color readableForeground(Color background, Color preferredForeground) {
        if (contrastRatio(background, preferredForeground) >= 4.5) return preferredForeground;
        Color darkInk = new Color(27, 39, 56);
        return contrastRatio(background, darkInk) >= contrastRatio(background, Color.WHITE)
            ? darkInk : Color.WHITE;
    }

    private static double contrastRatio(Color first, Color second) {
        double a = relativeLuminance(first);
        double b = relativeLuminance(second);
        double lighter = Math.max(a, b);
        double darker = Math.min(a, b);
        return (lighter + 0.05) / (darker + 0.05);
    }

    private static double relativeLuminance(Color color) {
        double red = linearChannel(color.getRed() / 255.0);
        double green = linearChannel(color.getGreen() / 255.0);
        double blue = linearChannel(color.getBlue() / 255.0);
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue;
    }

    private static double linearChannel(double channel) {
        return channel <= 0.04045 ? channel / 12.92 : Math.pow((channel + 0.055) / 1.055, 2.4);
    }

    private static boolean isDark(Color color) {
        return relativeLuminance(color) < 0.42;
    }

    private static Color blend(Color base, Color target, float amount) {
        float inverse = 1f - amount;
        int red = Math.round(base.getRed() * inverse + target.getRed() * amount);
        int green = Math.round(base.getGreen() * inverse + target.getGreen() * amount);
        int blue = Math.round(base.getBlue() * inverse + target.getBlue() * amount);
        return new Color(red, green, blue);
    }

    public static void styleInput(JComponent component) {
        component.setFont(BODY);
        component.setForeground(TEXT);
        component.setBackground(WHITE);
        component.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER), new EmptyBorder(7, 9, 7, 9)));
        component.setMinimumSize(new java.awt.Dimension(120, 36));
        if (component instanceof JComboBox<?> combo) combo.setFocusable(true);
        if (component instanceof JTextField field) field.setCaretColor(BLUE);
    }

    public static void styleTable(JTable table) {
        table.setFont(BODY);
        table.setForeground(TEXT);
        table.setBackground(WHITE);
        table.setGridColor(new Color(238, 241, 246));
        table.setSelectionBackground(BLUE_PALE);
        table.setSelectionForeground(TEXT);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        JTableHeader header = table.getTableHeader();
        header.setFont(BODY_BOLD);
        header.setForeground(new Color(83, 98, 119));
        header.setBackground(new Color(248, 250, 253));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setReorderingAllowed(false);
    }

    public static RoundedPanel card(int radius) {
        return new RoundedPanel(radius, WHITE, BORDER);
    }

    /** Lightweight rounded panel drawn without an external UI dependency. */
    public static class RoundedPanel extends javax.swing.JPanel {
        private final int radius;
        private final Color fill;
        private final Color outline;

        public RoundedPanel(int radius, Color fill, Color outline) {
            this.radius = radius;
            this.fill = fill;
            this.outline = outline;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            if (outline != null) {
                g.setStroke(new BasicStroke(1f));
                g.setColor(outline);
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    public static void setHandCursor(Component component) {
        component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
