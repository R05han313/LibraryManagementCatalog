package ui.tables;

import models.Book;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class BookTableModel extends AbstractTableModel {
    private final String[] columns = { "Title", "Author", "ISBN", "Genre", "Year", "Available", "Total", "Status",
            "Rating", "Shelf" };
    private List<Book> books;

    public BookTableModel(List<Book> books) {
        this.books = books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
        fireTableDataChanged();
    }

    public Book getBookAt(int row) {
        if (row < 0 || row >= books.size())
            return null;
        return books.get(row);
    }

    @Override
    public int getRowCount() {
        return books.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int col) {
        return columns[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        Book b = books.get(row);
        switch (col) {
            case 0:
                return b.getTitle();
            case 1:
                return b.getAuthor();
            case 2:
                return b.getIsbn();
            case 3:
                return b.getGenre();
            case 4:
                return b.getPublicationYear();
            case 5:
                return b.getAvailableCopies();
            case 6:
                return b.getTotalCopies();
            case 7:
                return b.getStatus().toString();
            case 8:
                return String.format("%.1f", b.getRating());
            case 9:
                return b.getShelfLocation();
            default:
                return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == 4 || col == 5 || col == 6)
            return Integer.class;
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}