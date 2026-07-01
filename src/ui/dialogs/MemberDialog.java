package ui.dialogs;

import data.LibraryData;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import models.Member;
import ui.Theme;

public class MemberDialog extends JDialog {
    private final JTextField nameField = Theme.textField();
    private final JTextField emailField = Theme.textField();
    private final JTextField phoneField = Theme.textField();
    private final JComboBox<Object> typeBox = Theme.comboBox(Member.MembershipType.values());

    private boolean confirmed = false;
    private final Member editingMember;

    public MemberDialog(Window owner, LibraryData data, Member memberToEdit) {
        super(owner, memberToEdit == null ? "Register New Member" : "Edit Member", ModalityType.APPLICATION_MODAL);
        this.editingMember = memberToEdit;

        getContentPane().setBackground(Theme.BG_PANEL);
        setLayout(new BorderLayout());
        setSize(440, 400);
        setMinimumSize(new Dimension(380, 320));
        setLocationRelativeTo(owner);
        setResizable(true);

        JPanel formPanel = new JPanel();
        formPanel.setBackground(Theme.BG_PANEL);
        formPanel.setBorder(new EmptyBorder(24, 28, 10, 28));
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.gridx = 0;
        gbc.weightx = 1;

        int row = 0;
        gbc.gridy = row++;
        formPanel.add(Theme.subtle("Full Name *"), gbc);
        gbc.gridy = row++;
        formPanel.add(nameField, gbc);
        gbc.gridy = row++;
        formPanel.add(Theme.subtle("Email *"), gbc);
        gbc.gridy = row++;
        formPanel.add(emailField, gbc);
        gbc.gridy = row++;
        formPanel.add(Theme.subtle("Phone *"), gbc);
        gbc.gridy = row++;
        formPanel.add(phoneField, gbc);
        gbc.gridy = row++;
        formPanel.add(Theme.subtle("Membership Type *"), gbc);
        gbc.gridy = row++;
        formPanel.add(typeBox, gbc);

        if (memberToEdit != null) {
            nameField.setText(memberToEdit.getName());
            emailField.setText(memberToEdit.getEmail());
            phoneField.setText(memberToEdit.getPhone());
            typeBox.setSelectedItem(memberToEdit.getType());
        }

        JLabel header = Theme.heading(memberToEdit == null ? "Register New Member" : "Edit Member Details");
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BG_PANEL);
        headerPanel.setBorder(new EmptyBorder(20, 28, 0, 28));
        headerPanel.add(header, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Theme.BG_PANEL);
        buttonPanel.setBorder(new EmptyBorder(0, 28, 18, 28));
        JButton cancelBtn = Theme.secondaryButton("Cancel");
        JButton saveBtn = Theme.primaryButton(memberToEdit == null ? "Register" : "Save Changes");
        cancelBtn.addActionListener(e -> dispose());
        saveBtn.addActionListener(e -> attemptSave(data));
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setBorder(null);
        formScroll.getViewport().setBackground(Theme.BG_PANEL);
        formScroll.setBackground(Theme.BG_PANEL);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);
        formScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(headerPanel, BorderLayout.NORTH);
        add(formScroll, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(saveBtn);
    }

    private void attemptSave(LibraryData data) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        Member.MembershipType type = (Member.MembershipType) typeBox.getSelectedItem();

        StringBuilder errors = new StringBuilder();
        if (name.isEmpty())
            errors.append("• Full name is required.\n");
        if (name.length() > 80)
            errors.append("• Name is too long.\n");

        if (email.isEmpty()) {
            errors.append("• Email is required.\n");
        } else if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")) {
            errors.append("• Email format looks invalid.\n");
        }

        if (phone.isEmpty()) {
            errors.append("• Phone number is required.\n");
        } else if (!phone.matches("[0-9+\\-\\s()]{7,15}")) {
            errors.append("• Phone number format looks invalid.\n");
        }

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, "Please correct the following:\n\n" + errors,
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (editingMember == null) {
            Member member = new Member(name, email, phone, type);
            data.addMember(member);
        } else {
            editingMember.setName(name);
            editingMember.setEmail(email);
            editingMember.setPhone(phone);
            editingMember.setType(type);
            data.logActivity("Edited member: " + name);
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}