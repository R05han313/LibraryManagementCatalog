package ui.dialogs;

import data.LibraryData;
import java.awt.*;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import models.Book;
import models.Member;
import ui.Theme;

public class IssueBookDialog extends JDialog {
    private final JComboBox<Member> memberBox;
    private boolean confirmed = false;

    public IssueBookDialog(Window owner, LibraryData data, Book book) {
        super(owner, "Issue Book", ModalityType.APPLICATION_MODAL);
        getContentPane().setBackground(Theme.BG_PANEL);
        setLayout(new BorderLayout());
        setSize(440, 320);
        setMinimumSize(new Dimension(380, 280));
        setLocationRelativeTo(owner);
        setResizable(true);

        JPanel content = new JPanel();
        content.setBackground(Theme.BG_PANEL);
        content.setBorder(new EmptyBorder(24, 28, 10, 28));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel header = Theme.heading("Issue: " + book.getTitle());
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = Theme.subtle("Available copies: " + book.getAvailableCopies() + " of " + book.getTotalCopies());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setBorder(new EmptyBorder(2, 0, 18, 0));

        JLabel memberLabel = Theme.subtle("Select Member *");
        memberLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Member[] members = data.getMembers().stream()
                .filter(Member::isActive)
                .sorted(Comparator.comparing(Member::getName))
                .toArray(Member[]::new);
        memberBox = new JComboBox<>(members);
        memberBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        memberBox.setBackground(Theme.BG_CARD);
        memberBox.setForeground(Theme.TEXT_PRIMARY);
        memberBox.setFont(Theme.FONT_BODY);
        memberBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        memberBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                if (value instanceof Member m) {
                    label.setText(m.getName() + "  (" + m.getType() + ")");
                }
                label.setBackground(isSelected ? Theme.ACCENT_SOFT : Theme.BG_CARD);
                label.setForeground(Theme.TEXT_PRIMARY);
                label.setBorder(new EmptyBorder(6, 8, 6, 8));
                return label;
            }
        });

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBackground(Theme.BG_PANEL);
        infoArea.setForeground(Theme.TEXT_MUTED);
        infoArea.setFont(Theme.FONT_SMALL);
        infoArea.setBorder(new EmptyBorder(14, 0, 0, 0));
        infoArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (members.length > 0) {
            updateInfo(infoArea, members[0]);
        } else {
            infoArea.setText("No active members are registered. Please register a member first.");
        }
        memberBox.addActionListener(e -> {
            Member selected = (Member) memberBox.getSelectedItem();
            if (selected != null)
                updateInfo(infoArea, selected);
        });

        content.add(header);
        content.add(sub);
        content.add(memberLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(memberBox);
        content.add(infoArea);
        content.add(Box.createVerticalGlue());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Theme.BG_PANEL);
        buttonPanel.setBorder(new EmptyBorder(0, 28, 18, 28));
        JButton cancelBtn = Theme.secondaryButton("Cancel");
        JButton issueBtn = Theme.primaryButton("Issue Book");
        cancelBtn.addActionListener(e -> dispose());
        issueBtn.addActionListener(e -> {
            Member selected = (Member) memberBox.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "No member selected.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            LibraryData.IssueResult result = data.issueBook(book, selected);
            JOptionPane.showMessageDialog(this, result.message(),
                    result.success() ? "Success" : "Cannot Issue Book",
                    result.success() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            if (result.success()) {
                confirmed = true;
                dispose();
            }
        });
        if (members.length == 0)
            issueBtn.setEnabled(false);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(issueBtn);

        JScrollPane contentScroll = new JScrollPane(content);
        contentScroll.setBorder(null);
        contentScroll.getViewport().setBackground(Theme.BG_PANEL);
        contentScroll.setBackground(Theme.BG_PANEL);
        contentScroll.getVerticalScrollBar().setUnitIncrement(16);
        contentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(contentScroll, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateInfo(JTextArea area, Member m) {
        area.setText(String.format(
                "Borrow limit: %d books   •   Loan period: %d days\nOutstanding fine: ₹%.2f%s",
                m.getBorrowLimit(), m.getLoanPeriodDays(), m.getOutstandingFine(),
                m.getOutstandingFine() > 0 ? "  (must be cleared before issuing)" : ""));
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}