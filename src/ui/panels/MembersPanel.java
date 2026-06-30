package ui.panels;

import app.MainFrame;
import data.LibraryData;
import models.Loan;
import models.Member;
import ui.DarkTableCellRenderer;
import ui.Theme;
import ui.dialogs.MemberDialog;
import ui.tables.MemberTableModel;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MembersPanel extends JPanel {
    private final LibraryData data;
    private final MainFrame mainFrame;
    private final MemberTableModel tableModel;
    private final JTable table;
    private final JTextField searchField = Theme.textField();
    private final JLabel resultCountLabel = Theme.subtle("");

    public MembersPanel(LibraryData data, MainFrame mainFrame) {
        this.data = data;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG_DARK);
        JLabel title = Theme.heading("Members");
        title.setFont(Theme.FONT_TITLE);
        header.add(title, BorderLayout.WEST);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerButtons.setBackground(Theme.BG_DARK);
        JButton addBtn = Theme.primaryButton("+ Register Member");
        addBtn.addActionListener(e -> openAddDialog());
        headerButtons.add(addBtn);
        header.add(headerButtons, BorderLayout.EAST);

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(Theme.BG_DARK);
        searchField.setPreferredSize(new Dimension(280, 36));
        filterBar.add(new JLabel(""));
        filterBar.add(searchField);
        filterBar.add(resultCountLabel);
        searchField.addCaretListener(e -> applyFilters());

        JPanel topSection = new JPanel(new BorderLayout());
        topSection.setBackground(Theme.BG_DARK);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(filterBar, BorderLayout.SOUTH);

        tableModel = new MemberTableModel(new ArrayList<>(data.getMembers()), data);
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
        table.setDefaultRenderer(Object.class, new DarkTableCellRenderer(7));
        table.setDefaultRenderer(Integer.class, new DarkTableCellRenderer(-1));
        table.setRowSorter(new TableRowSorter<>(tableModel));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        scrollPane.getViewport().setBackground(Theme.BG_PANEL);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        actionBar.setBackground(Theme.BG_DARK);
        JButton editBtn = Theme.secondaryButton("Edit");
        JButton historyBtn = Theme.secondaryButton("View Loan History");
        JButton payFineBtn = Theme.successButton("Pay Fine");
        JButton toggleActiveBtn = Theme.secondaryButton("Toggle Active");
        JButton deleteBtn = Theme.dangerButton("Delete");
        editBtn.addActionListener(e -> editSelectedMember());
        historyBtn.addActionListener(e -> viewLoanHistory());
        payFineBtn.addActionListener(e -> payFine());
        toggleActiveBtn.addActionListener(e -> toggleActive());
        deleteBtn.addActionListener(e -> deleteSelectedMember());
        actionBar.add(editBtn);
        actionBar.add(historyBtn);
        actionBar.add(payFineBtn);
        actionBar.add(toggleActiveBtn);
        actionBar.add(deleteBtn);

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
        List<Member> filtered = data.getMembers().stream()
                .filter(m -> query.isEmpty()
                        || m.getName().toLowerCase().contains(query)
                        || m.getEmail().toLowerCase().contains(query)
                        || m.getPhone().contains(query))
                .collect(Collectors.toList());
        tableModel.setMembers(filtered);
        resultCountLabel.setText(filtered.size() + " of " + data.getMembers().size() + " members");
    }

    public void refresh() {
        applyFilters();
    }

    private Member getSelectedMember() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0)
            return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return tableModel.getMemberAt(modelRow);
    }

    private void openAddDialog() {
        MemberDialog dialog = new MemberDialog(SwingUtilities.getWindowAncestor(this), data, null);
        dialog.setVisible(true);
        if (dialog.isConfirmed())
            mainFrame.refreshAll();
    }

    private void editSelectedMember() {
        Member m = getSelectedMember();
        if (m == null) {
            warnNoSelection("edit");
            return;
        }
        MemberDialog dialog = new MemberDialog(SwingUtilities.getWindowAncestor(this), data, m);
        dialog.setVisible(true);
        if (dialog.isConfirmed())
            mainFrame.refreshAll();
    }

    private void viewLoanHistory() {
        Member m = getSelectedMember();
        if (m == null) {
            warnNoSelection("view history for");
            return;
        }
        List<Loan> history = data.getLoans().stream()
                .filter(l -> l.getMemberId().equals(m.getId()))
                .collect(Collectors.toList());
        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(this, m.getName() + " has no loan history yet.",
                    "Loan History", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Loan l : history) {
            sb.append(String.format("%-30s issued %s, due %s — %s\n",
                    l.getBookTitleSnapshot(), l.getIssueDate(), l.getDueDate(), l.getStatusLabel()));
        }
        JTextArea area = new JTextArea(sb.toString(), 15, 50);
        area.setEditable(false);
        area.setFont(Theme.FONT_MONO);
        area.setBackground(Theme.BG_CARD);
        area.setForeground(Theme.TEXT_PRIMARY);
        JScrollPane scroll = new JScrollPane(area);
        JOptionPane.showMessageDialog(this, scroll, "Loan History — " + m.getName(), JOptionPane.PLAIN_MESSAGE);
    }

    private void payFine() {
        Member m = getSelectedMember();
        if (m == null) {
            warnNoSelection("pay fine for");
            return;
        }
        if (m.getOutstandingFine() <= 0) {
            JOptionPane.showMessageDialog(this, m.getName() + " has no outstanding fines.",
                    "No Fine Due", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String input = JOptionPane.showInputDialog(this,
                String.format("Outstanding fine: ₹%.2f\nEnter amount to pay:", m.getOutstandingFine()),
                String.format("%.2f", m.getOutstandingFine()));
        if (input == null)
            return;
        try {
            double amount = Double.parseDouble(input.trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Payment amount must be positive.", "Invalid Amount",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (amount > m.getOutstandingFine() + 0.001) {
                JOptionPane.showMessageDialog(this, "Payment cannot exceed the outstanding fine amount.",
                        "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                return;
            }
            m.payPartial(amount);
            data.logActivity(String.format("Payment of ₹%.2f received from %s", amount, m.getName()));
            mainFrame.refreshAll();
            JOptionPane.showMessageDialog(this, "Payment recorded successfully.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.", "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void toggleActive() {
        Member m = getSelectedMember();
        if (m == null) {
            warnNoSelection("toggle status for");
            return;
        }
        m.setActive(!m.isActive());
        data.logActivity((m.isActive() ? "Reactivated" : "Deactivated") + " member: " + m.getName());
        mainFrame.refreshAll();
    }

    private void deleteSelectedMember() {
        Member m = getSelectedMember();
        if (m == null) {
            warnNoSelection("delete");
            return;
        }
        if (m.getOutstandingFine() > 0) {
            int proceed = JOptionPane.showConfirmDialog(this,
                    String.format("%s has an outstanding fine of ₹%.2f. Delete anyway?", m.getName(),
                            m.getOutstandingFine()),
                    "Outstanding Fine", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (proceed != JOptionPane.YES_OPTION)
                return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete member \"" + m.getName() + "\"?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        boolean removed = data.removeMember(m);
        if (!removed) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete this member — they currently have active loans.\nPlease wait until all their books are returned.",
                    "Cannot Delete", JOptionPane.WARNING_MESSAGE);
        } else {
            mainFrame.refreshAll();
        }
    }

    private void warnNoSelection(String action) {
        JOptionPane.showMessageDialog(this, "Please select a member first to " + action + " them.",
                "No Member Selected", JOptionPane.WARNING_MESSAGE);
    }
}