package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class DarkTableCellRenderer extends DefaultTableCellRenderer {
    private final int statusColumnIndex; // -1 if no status coloring needed

    public DarkTableCellRenderer(int statusColumnIndex) {
        this.statusColumnIndex = statusColumnIndex;
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        label.setFont(Theme.FONT_BODY);

        if (isSelected) {
            label.setBackground(Theme.ACCENT_SOFT);
            label.setForeground(Theme.TEXT_PRIMARY);
        } else {
            label.setBackground(row % 2 == 0 ? Theme.BG_PANEL : Theme.BG_DARK);
            label.setForeground(Theme.TEXT_PRIMARY);
        }

        if (column == statusColumnIndex && value != null) {
            String text = value.toString();
            Color dot;
            switch (text) {
                case "AVAILABLE":
                case "Active":
                case "Returned":
                    dot = Theme.SUCCESS;
                    break;
                case "BORROWED":
                    dot = Theme.WARNING;
                    break;
                case "Overdue":
                    dot = Theme.DANGER;
                    break;
                case "RESERVED":
                    dot = Theme.ACCENT;
                    break;
                case "LOST":
                case "DAMAGED":
                    dot = Theme.DANGER;
                    break;
                default:
                    dot = Theme.TEXT_MUTED;
            }
            if (!isSelected)
                label.setForeground(dot);
            label.setFont(Theme.FONT_SUBHEAD);
        }

        setHorizontalAlignment(column == 0 ? SwingConstants.LEFT : SwingConstants.LEFT);
        return label;
    }
}