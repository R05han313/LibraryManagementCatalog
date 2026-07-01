package ui.panels;

import data.LibraryData;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import models.Loan;
import ui.Theme;

public class DashboardPanel extends JPanel {
    private final LibraryData data;
    @SuppressWarnings("FieldMayBeFinal")
    private JPanel statsRow;
    @SuppressWarnings("FieldMayBeFinal")
    private JPanel activityPanel;
    @SuppressWarnings("FieldMayBeFinal")
    private JPanel overduePanel;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public DashboardPanel(LibraryData data) {
        this.data = data;
        setLayout(new BorderLayout(0, 20));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = Theme.heading("Dashboard Overview");
        title.setFont(Theme.FONT_TITLE);
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Theme.BG_DARK);

        statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setBackground(Theme.BG_DARK);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        center.add(statsRow);
        center.add(Box.createVerticalStrut(20));

        JPanel splitRow = new JPanel(new GridLayout(1, 2, 20, 0));
        splitRow.setBackground(Theme.BG_DARK);

        overduePanel = new JPanel();
        overduePanel.setLayout(new BorderLayout());
        overduePanel.setBackground(Theme.BG_CARD);
        overduePanel.setBorder(Theme.sectionBorder("Overdue Books"));

        activityPanel = new JPanel();
        activityPanel.setLayout(new BorderLayout());
        activityPanel.setBackground(Theme.BG_CARD);
        activityPanel.setBorder(Theme.sectionBorder("Recent Activity"));

        splitRow.add(overduePanel);
        splitRow.add(activityPanel);
        center.add(splitRow);

        add(center, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        statsRow.removeAll();
        statsRow.add(statCard("Total Titles", String.valueOf(data.getTotalBookTitles()), Theme.ACCENT, "📚"));
        statsRow.add(statCard("Books Available", String.valueOf(data.getTotalAvailableCopies()), Theme.SUCCESS, "✅"));
        statsRow.add(statCard("Books Borrowed", String.valueOf(data.getTotalBorrowedCopies()), Theme.WARNING, "📖"));
        statsRow.add(statCard("Active Members", String.valueOf(data.getMembers().size()), Theme.ACCENT, "👥"));

        overduePanel.removeAll();
        List<Loan> overdue = data.getOverdueLoans();
        if (overdue.isEmpty()) {
            JLabel empty = Theme.subtle("No overdue books. Great job!");
            empty.setBorder(new EmptyBorder(20, 16, 20, 16));
            overduePanel.add(empty, BorderLayout.NORTH);
        } else {
            JPanel list = new JPanel();
            list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
            list.setBackground(Theme.BG_CARD);
            for (Loan l : overdue) {
                JLabel row = new JLabel(String.format("<html><b>%s</b> — %s (%d days overdue, ₹%.2f)</html>",
                        l.getBookTitleSnapshot(), l.getMemberNameSnapshot(), l.daysOverdue(), l.calculateFine()));
                row.setForeground(Theme.DANGER);
                row.setFont(Theme.FONT_SMALL);
                row.setBorder(new EmptyBorder(6, 16, 6, 16));
                list.add(row);
            }
            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(null);
            scroll.getViewport().setBackground(Theme.BG_CARD);
            overduePanel.add(scroll, BorderLayout.CENTER);
        }

        activityPanel.removeAll();
        List<String> log = data.getActivityLog();
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Theme.BG_CARD);
        int start = Math.max(0, log.size() - 25);
        for (int i = log.size() - 1; i >= start; i--) {
            JLabel row = new JLabel(log.get(i));
            row.setForeground(Theme.TEXT_MUTED);
            row.setFont(Theme.FONT_SMALL);
            row.setBorder(new EmptyBorder(4, 16, 4, 16));
            list.add(row);
        }
        if (log.isEmpty()) {
            JLabel empty = Theme.subtle("No activity yet.");
            empty.setBorder(new EmptyBorder(20, 16, 20, 16));
            list.add(empty);
        }
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_CARD);
        activityPanel.add(scroll, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel statCard(String label, String value, Color accent, String icon) {
        Theme.RoundedPanel card = new Theme.RoundedPanel(14, Theme.BG_CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);

        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(Theme.FONT_SMALL);
        labelLabel.setForeground(Theme.TEXT_MUTED);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Theme.BG_CARD);
        textPanel.add(valueLabel);
        textPanel.add(labelLabel);

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.EAST);
        return card;
    }
}