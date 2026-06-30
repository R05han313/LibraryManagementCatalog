package ui.dialogs;

import data.LibraryData;
import models.Book;
import ui.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Year;

/**
 * Dialog for adding a new book or editing an existing one.
 * Performs thorough input validation before allowing submission.
 */
public class BookDialog extends JDialog {
    private final JTextField titleField = Theme.textField();
    private final JTextField authorField = Theme.textField();
    private final JTextField isbnField = Theme.textField();
    private final JTextField genreField = Theme.textField();
    private final JTextField publisherField = Theme.textField();
    private final JTextField yearField = Theme.textField();
    private final JTextField copiesField = Theme.textField();
    private final JTextField priceField = Theme.textField();
    private final JTextField shelfField = Theme.textField();
    private final JTextArea descriptionArea = new JTextArea(4, 20);

    private boolean confirmed = false;
    private final Book editingBook; // null if adding new
    private final LibraryData data;

    public BookDialog(Window owner, LibraryData data, Book bookToEdit) {
        super(owner, bookToEdit == null ? "Add New Book" : "Edit Book", ModalityType.APPLICATION_MODAL);
        this.data = data;
        this.editingBook = bookToEdit;

        getContentPane().setBackground(Theme.BG_PANEL);
        setLayout(new BorderLayout());
        setSize(560, 640);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel formPanel = new JPanel();
        formPanel.setBackground(Theme.BG_PANEL);
        formPanel.setBorder(new EmptyBorder(24, 28, 10, 28));
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;
        gbc.weightx = 1;

        int gridY = 0;
        gridY = addField(formPanel, gbc, gridY, "Title *", titleField);
        gridY = addField(formPanel, gbc, gridY, "Author *", authorField);
        gridY = addField(formPanel, gbc, gridY, "ISBN *", isbnField);
        gridY = addField(formPanel, gbc, gridY, "Genre *", genreField);
        gridY = addField(formPanel, gbc, gridY, "Publisher", publisherField);
        gridY = addField(formPanel, gbc, gridY, "Publication Year *", yearField);
        gridY = addField(formPanel, gbc, gridY, "Total Copies *", copiesField);
        gridY = addField(formPanel, gbc, gridY, "Price (₹) *", priceField);
        gridY = addField(formPanel, gbc, gridY, "Shelf Location *", shelfField);

        JLabel descLabel = Theme.subtle("Description");
        gbc.gridy = gridY++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(descLabel, gbc);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(Theme.BG_CARD);
        descriptionArea.setForeground(Theme.TEXT_PRIMARY);
        descriptionArea.setCaretColor(Theme.ACCENT);
        descriptionArea.setFont(Theme.FONT_BODY);
        descriptionArea.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        gbc.gridy = gridY++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(descScroll, gbc);

        if (bookToEdit != null) {
            titleField.setText(bookToEdit.getTitle());
            authorField.setText(bookToEdit.getAuthor());
            isbnField.setText(bookToEdit.getIsbn());
            genreField.setText(bookToEdit.getGenre());
            publisherField.setText(bookToEdit.getPublisher());
            yearField.setText(String.valueOf(bookToEdit.getPublicationYear()));
            copiesField.setText(String.valueOf(bookToEdit.getTotalCopies()));
            priceField.setText(String.valueOf(bookToEdit.getPrice()));
            shelfField.setText(bookToEdit.getShelfLocation());
            descriptionArea.setText(bookToEdit.getDescription());
        }

        JLabel header = Theme.heading(bookToEdit == null ? "Add New Book" : "Edit Book Details");
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BG_PANEL);
        headerPanel.setBorder(new EmptyBorder(20, 28, 0, 28));
        headerPanel.add(header, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Theme.BG_PANEL);
        buttonPanel.setBorder(new EmptyBorder(0, 28, 18, 28));
        JButton cancelBtn = Theme.secondaryButton("Cancel");
        JButton saveBtn = Theme.primaryButton(bookToEdit == null ? "Add Book" : "Save Changes");
        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> attemptSave());
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        add(headerPanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveBtn);
    }

    private int addField(JPanel panel, GridBagConstraints gbc, int startRow, String label, JTextField field) {
        JLabel l = Theme.subtle(label);
        gbc.gridy = startRow;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(l, cloneAt(gbc));
        gbc.gridy = startRow + 1;
        panel.add(field, cloneAt(gbc));
        return startRow + 2;
    }

    private GridBagConstraints cloneAt(GridBagConstraints source) {
        GridBagConstraints c = (GridBagConstraints) source.clone();
        return c;
    }

    private void attemptSave() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String isbn = isbnField.getText().trim();
        String genre = genreField.getText().trim();
        String publisher = publisherField.getText().trim();
        String yearText = yearField.getText().trim();
        String copiesText = copiesField.getText().trim();
        String priceText = priceField.getText().trim();
        String shelf = shelfField.getText().trim();
        String description = descriptionArea.getText().trim();

        StringBuilder errors = new StringBuilder();

        if (title.isEmpty())
            errors.append("• Title is required.\n");
        if (author.isEmpty())
            errors.append("• Author is required.\n");
        if (genre.isEmpty())
            errors.append("• Genre is required.\n");
        if (shelf.isEmpty())
            errors.append("• Shelf location is required.\n");

        if (isbn.isEmpty()) {
            errors.append("• ISBN is required.\n");
        } else if (!isbn.matches("[0-9Xx-]{8,20}")) {
            errors.append("• ISBN format looks invalid (digits/hyphens only, 8-20 characters).\n");
        } else if (data.isDuplicateIsbn(isbn, editingBook == null ? "" : editingBook.getId())) {
            errors.append("• A book with this ISBN already exists.\n");
        }

        int year = 0;
        try {
            year = Integer.parseInt(yearText);
            if (year < 1000 || year > Year.now().getValue() + 1) {
                errors.append("• Publication year must be between 1000 and " + (Year.now().getValue() + 1) + ".\n");
            }
        } catch (NumberFormatException e) {
            errors.append("• Publication year must be a valid number.\n");
        }

        int copies = 0;
        try {
            copies = Integer.parseInt(copiesText);
            if (copies < 0)
                errors.append("• Total copies cannot be negative.\n");
            if (copies > 9999)
                errors.append("• Total copies seems unreasonably high (max 9999).\n");
            if (editingBook != null) {
                int borrowed = editingBook.getTotalCopies() - editingBook.getAvailableCopies();
                if (copies < borrowed) {
                    errors.append(
                            "• Total copies cannot be less than the " + borrowed + " copies currently borrowed.\n");
                }
            }
        } catch (NumberFormatException e) {
            errors.append("• Total copies must be a valid whole number.\n");
        }

        double price = 0;
        try {
            price = Double.parseDouble(priceText);
            if (price < 0)
                errors.append("• Price cannot be negative.\n");
        } catch (NumberFormatException e) {
            errors.append("• Price must be a valid number.\n");
        }

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this,
                    "Please correct the following:\n\n" + errors,
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (editingBook == null) {
            Book book = new Book(title, author, isbn, genre, publisher, year, copies, price, shelf, description);
            data.addBook(book);
        } else {
            int previousBorrowed = editingBook.getTotalCopies() - editingBook.getAvailableCopies();
            editingBook.setTitle(title);
            editingBook.setAuthor(author);
            editingBook.setIsbn(isbn);
            editingBook.setGenre(genre);
            editingBook.setPublisher(publisher);
            editingBook.setPublicationYear(year);
            editingBook.setTotalCopies(copies);
            editingBook.setAvailableCopies(Math.max(0, copies - previousBorrowed));
            editingBook.setPrice(price);
            editingBook.setShelfLocation(shelf);
            editingBook.setDescription(description);
            editingBook.setStatus(editingBook.getAvailableCopies() > 0 ? Book.Status.AVAILABLE : Book.Status.BORROWED);
            data.logActivity("Edited book: \"" + title + "\"");
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}