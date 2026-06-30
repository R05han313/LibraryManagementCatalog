package ui.panels;

import data.LibraryData;
import models.Book;
import ui.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class StatisticsPanel extends JPanel {
    private final LibraryData data;
    private JPanel contentArea;

    public StatisticsPanel(LibraryData data) {
        this.data = data;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = Theme.heading("Statistics & Insights");
        title.setFont(Theme.FONT_TITLE);
        add(title, BorderLayout.NORTH);

        contentArea = new JPanel();
        contentArea.setBackground(Theme.BG_DARK);
        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Theme.BG_DARK);
        add(scroll, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        contentArea.removeAll();

        // Summary cards
        JPanel summaryRow = new JPanel(new GridLayout(1, 4, 16, 0));
        summaryRow.setBackground(Theme.BG_DARK);
        summaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        summaryRow.add(summaryCard("Catalog Value", String.format("₹%.0f", data.getTotalCatalogValue()), Theme.ACCENT));
        summaryRow.add(summaryCard("Total Loans Issued", String.valueOf(data.getLoans().size()), Theme.SUCCESS));
        summaryRow.add(summaryCard("Currently Overdue", String.valueOf(data.getOverdueLoans().size()), Theme.DANGER));
        summaryRow.add(summaryCard("Fines Outstanding", String.format("₹%.2f", data.getTotalFinesCollectable()),
                Theme.WARNING));
        contentArea.add(summaryRow);
        contentArea.add(Box.createVerticalStrut(20));

        // Genre distribution chart
        JPanel genrePanel = Theme.card();
        genrePanel.setBorder(Theme.sectionBorder("Genre Distribution"));
        genrePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        genrePanel.add(new GenreBarChart(data.getGenreDistribution()), BorderLayout.CENTER);
        contentArea.add(genrePanel);
        contentArea.add(Box.createVerticalStrut(20));

        // Most popular books
        JPanel popularPanel = Theme.card();
        popularPanel.setBorder(Theme.sectionBorder("Most Popular Books"));
        List<Book> popular = data.getMostPopularBooks(5);
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBackground(Theme.BG_CARD);
        if (popular.isEmpty()) {
            list.add(Theme.subtle("No books in catalog yet."));
        } else {
            int rank = 1;
            for (Book b : popular) {
                long count = data.getBorrowCountForBook(b);
                JLabel row = new JLabel(String.format("#%d  %s — borrowed %d time%s",
                        rank++, b.getTitle(), count, count == 1 ? "" : "s"));
                row.setForeground(Theme.TEXT_PRIMARY);
                row.setFont(Theme.FONT_BODY);
                row.setBorder(new EmptyBorder(6, 4, 6, 4));
                list.add(row);
            }
        }
        popularPanel.add(list, BorderLayout.CENTER);
        contentArea.add(popularPanel);

        revalidate();
        repaint();
    }

    private JPanel summaryCard(String label, String value, Color accent) {
        Theme.RoundedPanel card = new Theme.RoundedPanel(14, Theme.BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(accent);
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(Theme.FONT_SMALL);
        labelLabel.setForeground(Theme.TEXT_MUTED);
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(labelLabel);
        return card;
    }

    /** A simple custom-drawn horizontal bar chart for genre distribution. */
    private static class GenreBarChart extends JPanel {
        private final Map<String, Long> distribution;

        GenreBarChart(Map<String, Long> distribution) {
            this.distribution = distribution;
            setBackground(Theme.BG_CARD);
            setPreferredSize(new Dimension(400, 220));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (distribution.isEmpty()) {
                g2.setColor(Theme.TEXT_MUTED);
                g2.setFont(Theme.FONT_BODY);
                g2.drawString("No data to display.", 20, 30);
                g2.dispose();
                return;
            }

            long max = distribution.values().stream().mapToLong(Long::longValue).max().orElse(1);
            int barHeight = 22;
            int gap = 14;
            int y = 16;
            int labelWidth = 140;
            int chartWidth = Math.max(100, getWidth() - labelWidth - 80);

            Color[] palette = { Theme.ACCENT, Theme.SUCCESS, Theme.WARNING, Theme.DANGER, Theme.ACCENT_DIM };
            int colorIndex = 0;

            for (Map.Entry<String, Long> entry : distribution.entrySet()) {
                String genre = entry.getKey();
                long count = entry.getValue();
                int barWidth = (int) ((double) count / max * chartWidth);

                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(Theme.FONT_BODY);
                g2.drawString(truncate(genre, 18), 10, y + barHeight - 6);

                g2.setColor(palette[colorIndex % palette.length]);
                g2.fillRoundRect(labelWidth, y, Math.max(4, barWidth), barHeight, 6, 6);

                g2.setColor(Theme.TEXT_MUTED);
                g2.drawString(String.valueOf(count), labelWidth + barWidth + 10, y + barHeight - 6);

                y += barHeight + gap;
                colorIndex++;
            }

            setPreferredSize(new Dimension(getWidth(), y + 10));
            g2.dispose();
        }

        private String truncate(String s, int max) {
            return s.length() <= max ? s : s.substring(0, max - 1) + "…";
        }
    }
}