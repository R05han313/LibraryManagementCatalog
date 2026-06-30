package ui.tables;

import data.LibraryData;
import models.Member;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class MemberTableModel extends AbstractTableModel {
    private final String[] columns = { "Name", "Email", "Phone", "Type", "Joined", "Active Loans", "Fine Due",
            "Status" };
    private List<Member> members;
    private final LibraryData data;

    public MemberTableModel(List<Member> members, LibraryData data) {
        this.members = members;
        this.data = data;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
        fireTableDataChanged();
    }

    public Member getMemberAt(int row) {
        if (row < 0 || row >= members.size())
            return null;
        return members.get(row);
    }

    @Override
    public int getRowCount() {
        return members.size();
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
        Member m = members.get(row);
        switch (col) {
            case 0:
                return m.getName();
            case 1:
                return m.getEmail();
            case 2:
                return m.getPhone();
            case 3:
                return m.getType().toString();
            case 4:
                return m.getJoinDate().toString();
            case 5:
                return data.getActiveLoansForMember(m).size();
            case 6:
                return String.format("%.2f", m.getOutstandingFine());
            case 7:
                return m.isActive() ? "Active" : "Inactive";
            default:
                return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == 5)
            return Integer.class;
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}