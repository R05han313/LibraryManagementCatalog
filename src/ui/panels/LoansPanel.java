package ui.panels;

import app.MainFrame;
import data.LibraryData;
import models.Loan;
import ui.DarkTableCellRenderer;
import ui.Theme;
import ui.tables.LoanTableModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LoansPanel extends JPanel {
    private final LibraryData data;
    private final MainFrame mainFrame;
    private final LoanTableModel tableModel;
    private final JTable table;
    private final JComboBox<Object> statusFilter;
    private final JTextField searchField = Theme.textField();
    private final JLabel resultCountLabel = Theme.subtle("");

    public LoansPanel(LibraryData data, MainFrame mainFrame) {
        this.data = data;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel title = Theme.heading("Loans & Returns");
        title.setFont(Theme.FONT_TITLE);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(Theme.BG_DARK);
        searchField.setPreferredSize(new Dimension(280, 36));
        statusFilter = Theme.comboBox(new Object[] { "All", "Active", "Overdue", "Returned" });
        statusFilter.setPreferredSize(new Dimension(140, 36));
        filterBar.add(new JLabel(""));
        filterBar.add(searchField);
        filterBar.add(statusFilter);
        filterBar.add(resultCountLabel);
        searchField.addCaretListener(e -> applyFilters());
        statusFilter.addActionListener(e -> applyFilters());

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(Theme.BG_DARK);
        topSection.add(title, BorderLayout.NORTH);
        topSection.add(filterBar, BorderLayout.SOUTH);

        tableModel = new LoanTableModel(new ArrayList<>());
        table = new JTable(tableModel);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setBackground(Theme.BG_PANEL);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setFont(Theme.FONT_BODY);
        table.setSelectionBackground(Theme.ACCENT_SOFT);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);
        table.getTableHeader().setBackground(Theme.BG_CARD);
        table.getTableHeader().setForeground(Theme.TEXT_MUTED);
        table.getTableHeader().setFont(Theme.FONT_SUBHEAD);
        table.setDefaultRenderer(Object.class, new DarkTableCellRenderer(5));
        table.setRowSorter(new TableRowSorter<>(tableModel));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        scrollPane.getViewport().setBackground(Theme.BG_PANEL);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        actionBar.setBackground(Theme.BG_DARK);
        JButton returnBtn = Theme.successButton("Mark as Returned");
        JButton renewBtn = Theme.secondaryButton("Renew Loan (+7 days)");
        returnBtn.addActionListener(e -> markSelectedReturned());
        renewBtn.addActionListener(e -> renewSelectedLoan());
        actionBar.add(returnBtn);
        actionBar.add(renewBtn);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG_DARK);
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(actionBar, BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        applyFilters();
    }

    private void applyFilters() {
        String query = searchField.getText().trim().toLowerCase();
        Object statusSel = statusFilter.getSelectedItem();

        List<Loan> filtered = data.getLoans().stream()
                .filter(l -> {
                    boolean matchesQuery = query.isEmpty()
                            || l.getBookTitleSnapshot().toLowerCase().contains(query)
                            || l.getMemberNameSnapshot().toLowerCase().contains(query);
                    boolean matchesStatus;
                    if (statusSel == null || statusSel.equals("All"))
                        matchesStatus = true;
                    else if (statusSel.equals("Returned"))
                        matchesStatus = l.isReturned();
                    else if (statusSel.equals("Overdue"))
                        matchesStatus = !l.isReturned() && l.isOverdue();
                    else
                        matchesStatus = !l.isReturned() && !l.isOverdue(); // Active
                    return matchesQuery && matchesStatus;
                })
                .sorted(Comparator.comparing(Loan::getIssueDate).reversed())
                .collect(Collectors.toList());

        tableModel.setLoans(filtered);
        resultCountLabel.setText(filtered.size() + " of " + data.getLoans().size() + " loans");
    }

    public void refresh() {
        applyFilters();
    }

    private Loan getSelectedLoan() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0)
            return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getLoanAt(modelRow);
    }

    private void markSelectedReturned() {
        Loan loan = getSelectedLoan();
        if (loan == null) {
            JOptionPane.showMessageDialog(this, "Please select a loan first.", "No Loan Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (loan.isReturned()) {
            JOptionPane.showMessageDialog(this, "This loan has already been returned.", "Already Returned",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LibraryData.ReturnResult result = data.returnBook(loan);
        JOptionPane.showMessageDialog(this, result.message(),
                result.success() ? "Return Successful" : "Error",
                result.success() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        if (result.success())
            mainFrame.refreshAll();
    }

    private void renewSelectedLoan() {
        Loan loan = getSelectedLoan();
        if (loan == null) {
            JOptionPane.showMessageDialog(this, "Please select a loan first.", "No Loan Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Loan.RenewResult result = loan.renew(7, 2);
        JOptionPane.showMessageDialog(this, result.message(),
                result.success() ? "Loan Renewed" : "Cannot Renew",
                result.success() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        if (result.success()) {
            data.logActivity("Renewed loan: \"" + loan.getBookTitleSnapshot() + "\" for " + loan.getMemberNameSnapshot()
                    + " (new due date " + loan.getDueDate() + ")");
            mainFrame.refreshAll();
        }
    }
}