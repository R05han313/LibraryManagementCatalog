package ui.tables;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import models.Loan;

public class LoanTableModel extends AbstractTableModel {
    private final String[] columns = { "Book", "Member", "Issue Date", "Due Date", "Return Date", "Status", "Fine" };
    private List<Loan> loans;

    public LoanTableModel(List<Loan> loans) {
        this.loans = loans;
    }

    public void setLoans(List<Loan> loans) {
        this.loans = loans;
        fireTableDataChanged();
    }

    public Loan getLoanAt(int row) {
        if (row < 0 || row >= loans.size())
            return null;
        return loans.get(row);
    }

    @Override
    public int getRowCount() {
        return loans.size();
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
        Loan l = loans.get(row);
        return switch (col) {
            case 0 -> l.getBookTitleSnapshot();
            case 1 -> l.getMemberNameSnapshot();
            case 2 -> l.getIssueDate().toString();
            case 3 -> l.getDueDate().toString();
            case 4 -> l.getReturnDate() == null ? "—" : l.getReturnDate().toString();
            case 5 -> l.getStatusLabel();
            case 6 -> l.isReturned()
                ? String.format("%.2f", l.getFineCharged())
                : (l.isOverdue() ? String.format("%.2f (accruing)", l.calculateFine()) : "0.00");
            default -> "";
        };
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}