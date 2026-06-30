package ui;

import models.Book;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Centralized dark-theme styling constants and reusable UI component factories
 * so the whole application has a consistent, modern, minimalist look.
 */
public class Theme {
    // Core palette
    public static final Color BG_DARKEST = new Color(0x12, 0x13, 0x18);
    public static final Color BG_DARK = new Color(0x1A, 0x1C, 0x24);
    public static final Color BG_PANEL = new Color(0x21, 0x24, 0x2E);
    public static final Color BG_CARD = new Color(0x27, 0x2B, 0x37);
    public static final Color BG_HOVER = new Color(0x30, 0x35, 0x44);
    public static final Color ACCENT = new Color(0x6C, 0x8C, 0xFF);
    public static final Color ACCENT_DIM = new Color(0x4A, 0x5F, 0xB0);
    public static final Color ACCENT_SOFT = new Color(0x39, 0x40, 0x5C);
    public static final Color SUCCESS = new Color(0x4C, 0xD9, 0x7B);
    public static final Color WARNING = new Color(0xF5, 0xA6, 0x23);
    public static final Color DANGER = new Color(0xF0, 0x5C, 0x5C);
    public static final Color TEXT_PRIMARY = new Color(0xEC, 0xEE, 0xF3);
    public static final Color TEXT_MUTED = new Color(0x9A, 0x9F, 0xAE);
    public static final Color BORDER = new Color(0x33, 0x37, 0x44);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEAD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    public static void applyGlobalDefaults() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        UIManager.put("ToolTip.background", BG_CARD);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);
        UIManager.put("ToolTip.border", new LineBorder(BORDER, 1));

        UIManager.put("OptionPane.background", BG_PANEL);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("Panel.background", BG_PANEL);

        UIManager.put("ScrollBar.thumb", BG_HOVER);
        UIManager.put("ScrollBar.track", BG_DARK);
        UIManager.put("ScrollBar.width", 10);
    }

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, ACCENT, Color.WHITE, ACCENT.brighter());
        return btn;
    }

    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, BG_CARD, TEXT_PRIMARY, BG_HOVER);
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, DANGER, Color.WHITE, DANGER.brighter());
        return btn;
    }

    public static JButton successButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, SUCCESS, BG_DARKEST, SUCCESS.brighter());
        return btn;
    }

    private static void styleButton(JButton btn, Color bg, Color fg, Color hover) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(FONT_SUBHEAD);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(hover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
    }

    public static JTextField textField() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    public static JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        styleField(field);
        return field;
    }

    private static void styleField(JTextField field) {
        field.setBackground(BG_CARD);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT);
        field.setFont(FONT_BODY);
        field.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(8, 10, 8, 10)));
    }

    public static JComboBox<Object> comboBox(Object[] items) {
        JComboBox<Object> box = new JComboBox<>(items);
        box.setBackground(BG_CARD);
        box.setForeground(TEXT_PRIMARY);
        box.setFont(FONT_BODY);
        box.setFocusable(false);
        box.setBorder(new EmptyBorder(4, 6, 4, 6));
        return box;
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADING);
        label.setForeground(TEXT_PRIMARY);
        return label;
    }

    public static JLabel subtle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT_MUTED);
        return label;
    }

    public static JPanel card() {
        JPanel panel = new RoundedPanel(14, BG_CARD);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        return panel;
    }

    public static TitledBorder sectionBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER, 1), title);
        border.setTitleColor(TEXT_MUTED);
        border.setTitleFont(FONT_SUBHEAD);
        return border;
    }

    /**
     * A JPanel with rounded corners and a flat fill color, used for card-style
     * sections.
     */
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;

        public RoundedPanel(int radius, Color fill) {
            this.radius = radius;
            this.fill = fill;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static Color statusColor(Book.Status status) {
        switch (status) {
            case AVAILABLE:
                return SUCCESS;
            case BORROWED:
                return WARNING;
            case RESERVED:
                return ACCENT;
            case LOST:
            case DAMAGED:
                return DANGER;
            default:
                return TEXT_MUTED;
        }
    }
}