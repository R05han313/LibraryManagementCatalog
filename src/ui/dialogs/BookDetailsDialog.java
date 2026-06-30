package ui.dialogs;

import data.LibraryData;
import models.Book;
import models.Loan;
import ui.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class BookDetailsDialog extends JDialog {
    public BookDetailsDialog(Window owner, LibraryData data, Book book) {
        super(owner, "Book Details", ModalityType.APPLICATION_MODAL);
        getContentPane().setBackground(Theme.BG_PANEL);
        setLayout(new BorderLayout());
        setSize(520, 560);
        setLocationRelativeTo(owner);

        JPanel content = new JPanel();
        content.setBackground(Theme.BG_PANEL);
        content.setBorder(new EmptyBorder(24, 28, 16, 28));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = Theme.heading(book.getTitle());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel author = Theme.subtle("by " + book.getAuthor());
        author.setFont(Theme.FONT_BODY);
        author.setForeground(Theme.TEXT_MUTED);
        author.setAlignmentX(Component.LEFT_ALIGNMENT);
        author.setBorder(new EmptyBorder(2, 0, 16, 0));

        content.add(title);
        content.add(author);

        content.add(infoRow("ISBN", book.getIsbn()));
        content.add(infoRow("Genre", book.getGenre()));
        content.add(infoRow("Publisher", book.getPublisher().isEmpty() ? "—" : book.getPublisher()));
        content.add(infoRow("Publication Year", String.valueOf(book.getPublicationYear())));
        content.add(infoRow("Shelf Location", book.getShelfLocation()));
        content.add(infoRow("Price", String.format("₹%.2f", book.getPrice())));
        content.add(infoRow("Copies", book.getAvailableCopies() + " available of " + book.getTotalCopies() + " total"));
        content.add(infoRow("Status", book.getStatus().toString()));
        content.add(infoRow("Rating", String.format("%.1f / 5.0", book.getRating())));
        content.add(infoRow("Total Times Borrowed", String.valueOf(data.getBorrowCountForBook(book))));
        content.add(infoRow("Date Added", book.getDateAdded().toString()));

        JLabel descLabel = Theme.subtle("DESCRIPTION");
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descLabel.setBorder(new EmptyBorder(16, 0, 6, 0));
        content.add(descLabel);

        JTextArea desc = new JTextArea(
                book.getDescription().isEmpty() ? "No description provided." : book.getDescription());
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setBackground(Theme.BG_PANEL);
        desc.setForeground(Theme.TEXT_PRIMARY);
        desc.setFont(Theme.FONT_BODY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(desc);

        List<Loan> activeLoans = data.getActiveLoansForBook(book);
        if (!activeLoans.isEmpty()) {
            JLabel loanLabel = Theme.subtle("CURRENTLY BORROWED BY");
            loanLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            loanLabel.setBorder(new EmptyBorder(16, 0, 6, 0));
            content.add(loanLabel);
            for (Loan l : activeLoans) {
                JLabel row = new JLabel(l.getMemberNameSnapshot() + " — due " + l.getDueDate()
                        + (l.isOverdue() ? "  (OVERDUE)" : ""));
                row.setFont(Theme.FONT_BODY);
                row.setForeground(l.isOverdue() ? Theme.DANGER : Theme.TEXT_PRIMARY);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                content.add(row);
            }
        }

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_PANEL);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Theme.BG_PANEL);
        buttonPanel.setBorder(new EmptyBorder(0, 28, 16, 28));
        JButton closeBtn = Theme.secondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Theme.BG_PANEL);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = new JLabel(label);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_MUTED);
        l.setPreferredSize(new Dimension(160, 18));
        JLabel v = new JLabel(value);
        v.setFont(Theme.FONT_SUBHEAD);
        v.setForeground(Theme.TEXT_PRIMARY);
        row.add(l, BorderLayout.WEST);
        row.add(v, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        return row;
    }
}