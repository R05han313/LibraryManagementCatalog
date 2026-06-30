package ui.panels;

import app.MainFrame;
import data.LibraryData;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import models.Book;
import ui.DarkTableCellRenderer;
import ui.Theme;
import ui.dialogs.BookDetailsDialog;
import ui.dialogs.BookDialog;
import ui.dialogs.IssueBookDialog;
import ui.tables.BookTableModel;
import utils.CsvUtil;

public class CatalogPanel extends JPanel {
    private final LibraryData data;
    private final MainFrame mainFrame;
    private final BookTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<BookTableModel> sorter;
    private final JTextField searchField = Theme.textField();
    private final JComboBox<Object> genreFilter;
    private final JComboBox<Object> statusFilter;
    private final JLabel resultCountLabel = Theme.subtle("");

    public CatalogPanel(LibraryData data, MainFrame mainFrame) {
        this.data = data;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // ---- Header ----
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARK);
        JLabel title = Theme.heading("Book Catalog");
        title.setFont(Theme.FONT_TITLE);
        header.add(title, BorderLayout.WEST);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerButtons.setBackground(Theme.BG_DARK);
        JButton addBtn = Theme.primaryButton("+ Add Book");
        JButton importBtn = Theme.secondaryButton("Import CSV");
        JButton exportBtn = Theme.secondaryButton("Export CSV");
        addBtn.addActionListener(e -> openAddDialog());
        importBtn.addActionListener(e -> CsvUtil.importBooks(this, data, this::refresh));
        exportBtn.addActionListener(e -> CsvUtil.exportBooks(this, data));
        headerButtons.add(importBtn);
        headerButtons.add(exportBtn);
        headerButtons.add(addBtn);
        header.add(headerButtons, BorderLayout.EAST);

        // ---- Filter bar ----
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(Theme.BG_DARK);
        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.putClientProperty("JTextField.placeholderText", "Search by title, author, or ISBN...");
        genreFilter = Theme.comboBox(new Object[] { "All Genres" });
        statusFilter = Theme
                .comboBox(new Object[] { "All Statuses", "AVAILABLE", "BORROWED", "RESERVED", "LOST", "DAMAGED" });
        genreFilter.setPreferredSize(new Dimension(150, 36));
        statusFilter.setPreferredSize(new Dimension(150, 36));

        filterBar.add(new JLabel(""));
        filterBar.add(searchField);
        filterBar.add(genreFilter);
        filterBar.add(statusFilter);
        filterBar.add(resultCountLabel);

        searchField.addCaretListener(e -> applyFilters());
        genreFilter.addActionListener(e -> applyFilters());
        statusFilter.addActionListener(e -> applyFilters());

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(Theme.BG_DARK);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(filterBar, BorderLayout.SOUTH);

        // ---- Table ----
        tableModel = new BookTableModel(new ArrayList<>(data.getBooks()));
        table = new JTable(tableModel);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setBackground(Theme.BG_PANEL);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_BODY);
        table.setSelectionBackground(Theme.ACCENT_SOFT);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setBackground(Theme.BG_CARD);
        table.getTableHeader().setForeground(Theme.TEXT_MUTED);
        table.getTableHeader().setFont(Theme.FONT_SUBHEAD);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));
        table.setDefaultRenderer(Object.class, new DarkTableCellRenderer(7));
        table.setDefaultRenderer(Integer.class, new DarkTableCellRenderer(-1));
        table.setAutoCreateRowSorter(false);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedBookDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        scrollPane.getViewport().setBackground(Theme.BG_PANEL);

        // ---- Action buttons below table ----
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        actionBar.setBackground(Theme.BG_DARK);
        JButton viewBtn = Theme.secondaryButton("View Details");
        JButton editBtn = Theme.secondaryButton("Edit");
        JButton issueBtn = Theme.successButton("Issue to Member");
        JButton deleteBtn = Theme.dangerButton("Delete");
        viewBtn.addActionListener(e -> viewSelectedBookDetails());
        editBtn.addActionListener(e -> editSelectedBook());
        issueBtn.addActionListener(e -> issueSelectedBook());
        deleteBtn.addActionListener(e -> deleteSelectedBook());
        actionBar.add(viewBtn);
        actionBar.add(editBtn);
        actionBar.add(issueBtn);
        actionBar.add(deleteBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG_DARK);
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(actionBar, BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        refreshGenreFilterOptions();
        applyFilters();
    }

    private void refreshGenreFilterOptions() {
        Set<String> genres = data.getBooks().stream().map(Book::getGenre)
                .collect(Collectors.toCollection(TreeSet::new));
        Object currentSelection = genreFilter.getSelectedItem();
        genreFilter.removeAllItems();
        genreFilter.addItem("All Genres");
        for (String g : genres)
            genreFilter.addItem(g);
        if (currentSelection != null)
            genreFilter.setSelectedItem(currentSelection);
    }

    private void applyFilters() {
        String query = searchField.getText().trim().toLowerCase();
        Object genreSel = genreFilter.getSelectedItem();
        Object statusSel = statusFilter.getSelectedItem();

        List<Book> filtered = data.getBooks().stream().filter(b -> {
            boolean matchesQuery = query.isEmpty()
                    || b.getTitle().toLowerCase().contains(query)
                    || b.getAuthor().toLowerCase().contains(query)
                    || b.getIsbn().toLowerCase().contains(query);
            boolean matchesGenre = genreSel == null || genreSel.equals("All Genres") || b.getGenre().equals(genreSel);
            boolean matchesStatus = statusSel == null || statusSel.equals("All Statuses")
                    || b.getStatus().toString().equals(statusSel);
            return matchesQuery && matchesGenre && matchesStatus;
        }).sorted().collect(Collectors.toList());

        tableModel.setBooks(filtered);
        resultCountLabel.setText(filtered.size() + " of " + data.getBooks().size() + " books");
    }

    public void refresh() {
        refreshGenreFilterOptions();
        applyFilters();
    }

    private Book getSelectedBook() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0)
            return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getBookAt(modelRow);
    }

    private void openAddDialog() {
        BookDialog dialog = new BookDialog(SwingUtilities.getWindowAncestor(this), data, null);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            mainFrame.refreshAll();
        }
    }

    private void editSelectedBook() {
        Book book = getSelectedBook();
        if (book == null) {
            showNoSelectionWarning("edit");
            return;
        }
        BookDialog dialog = new BookDialog(SwingUtilities.getWindowAncestor(this), data, book);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            mainFrame.refreshAll();
        }
    }

    private void viewSelectedBookDetails() {
        Book book = getSelectedBook();
        if (book == null) {
            showNoSelectionWarning("view");
            return;
        }
        new BookDetailsDialog(SwingUtilities.getWindowAncestor(this), data, book).setVisible(true);
    }

    private void issueSelectedBook() {
        Book book = getSelectedBook();
        if (book == null) {
            showNoSelectionWarning("issue");
            return;
        }
        if (data.getMembers().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No members are registered yet. Please add a member first.",
                    "No Members", JOptionPane.WARNING_MESSAGE);
            return;
        }
        IssueBookDialog dialog = new IssueBookDialog(SwingUtilities.getWindowAncestor(this), data, book);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            mainFrame.refreshAll();
        }
    }

    private void deleteSelectedBook() {
        Book book = getSelectedBook();
        if (book == null) {
            showNoSelectionWarning("delete");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete \"" + book.getTitle() + "\"?\nThis cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        boolean removed = data.removeBook(book);
        if (!removed) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete this book — it currently has active loans outstanding.\nPlease wait until all copies are returned.",
                    "Cannot Delete", JOptionPane.WARNING_MESSAGE);
        } else {
            mainFrame.refreshAll();
        }
    }

    private void showNoSelectionWarning(String action) {
        JOptionPane.showMessageDialog(this, "Please select a book first to " + action + " it.",
                "No Book Selected", JOptionPane.WARNING_MESSAGE);
    }
}