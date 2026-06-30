package app;

import data.LibraryData;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ui.Theme;
import ui.panels.CatalogPanel;
import ui.panels.DashboardPanel;
import ui.panels.LoansPanel;
import ui.panels.MembersPanel;
import ui.panels.StatisticsPanel;

public class MainFrame extends JFrame {
    private final LibraryData data;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    private DashboardPanel dashboardPanel;
    private CatalogPanel catalogPanel;
    private MembersPanel membersPanel;
    private LoansPanel loansPanel;
    private StatisticsPanel statisticsPanel;

    private final java.util.List<JButton> navButtons = new java.util.ArrayList<>();
    private final javax.swing.Timer autoSaveTimer;

    public MainFrame() {
        super("NovaLib — Library Catalog System");
        this.data = LibraryData.loadOrCreate();

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1040, 640));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildCardArea(), BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmAndExit();
            }
        });

        // Auto-save every 60 seconds in case of unexpected shutdown
        autoSaveTimer = new javax.swing.Timer(60_000, e -> data.save());
        autoSaveTimer.start();

        setVisible(true);
        selectNav(0);
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(Theme.BG_DARKEST);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(24, 0, 16, 0));

        JLabel logo = new JLabel("  📚 NovaLib");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(Theme.TEXT_PRIMARY);
        logo.setBorder(new EmptyBorder(0, 8, 28, 8));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);

        String[] labels = { "Dashboard", "Catalog", "Members", "Loans & Returns", "Statistics" };
        String[] icons = { "🏠", "📖", "👥", "🔄", "📊" };

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            JButton navBtn = new JButton("  " + icons[i] + "   " + labels[i]);
            navBtn.setHorizontalAlignment(SwingConstants.LEFT);
            navBtn.setFont(Theme.FONT_SUBHEAD);
            navBtn.setForeground(Theme.TEXT_MUTED);
            navBtn.setBackground(Theme.BG_DARKEST);
            navBtn.setBorder(new EmptyBorder(12, 18, 12, 12));
            navBtn.setFocusPainted(false);
            navBtn.setBorderPainted(false);
            navBtn.setOpaque(true);
            navBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            navBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            navBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            navBtn.addActionListener(e -> selectNav(index));
            navButtons.add(navBtn);
            sidebar.add(navBtn);
        }

        sidebar.add(Box.createVerticalGlue());

        JButton saveBtn = Theme.secondaryButton("💾 Save Now");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(190, 40));
        saveBtn.setMargin(new Insets(2, 2, 2, 2));
        JPanel saveWrap = new JPanel();
        saveWrap.setBackground(Theme.BG_DARKEST);
        saveWrap.setBorder(new EmptyBorder(8, 18, 8, 18));
        saveWrap.setLayout(new BorderLayout());
        saveWrap.add(saveBtn, BorderLayout.CENTER);
        saveBtn.addActionListener(e -> {
            data.save();
            JOptionPane.showMessageDialog(this, "Library data saved successfully.", "Saved",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        sidebar.add(saveWrap);

        JLabel footer = Theme.subtle("  v1.0 • Local Storage");
        footer.setBorder(new EmptyBorder(8, 10, 0, 0));
        sidebar.add(footer);

        return sidebar;
    }

    private JPanel buildCardArea() {
        dashboardPanel = new DashboardPanel(data);
        catalogPanel = new CatalogPanel(data, this);
        membersPanel = new MembersPanel(data, this);
        loansPanel = new LoansPanel(data, this);
        statisticsPanel = new StatisticsPanel(data);

        cardPanel.setBackground(Theme.BG_DARK);
        cardPanel.add(dashboardPanel, "dashboard");
        cardPanel.add(catalogPanel, "catalog");
        cardPanel.add(membersPanel, "members");
        cardPanel.add(loansPanel, "loans");
        cardPanel.add(statisticsPanel, "statistics");

        return cardPanel;
    }

    private void selectNav(int index) {
        String[] cardNames = { "dashboard", "catalog", "members", "loans", "statistics" };
        cardLayout.show(cardPanel, cardNames[index]);
        for (int i = 0; i < navButtons.size(); i++) {
            JButton btn = navButtons.get(i);
            if (i == index) {
                btn.setBackground(Theme.BG_PANEL);
                btn.setForeground(Theme.ACCENT);
            } else {
                btn.setBackground(Theme.BG_DARKEST);
                btn.setForeground(Theme.TEXT_MUTED);
            }
        }
        if (index == 0)
            dashboardPanel.refresh();
        if (index == 4)
            statisticsPanel.refresh();
    }

    /**
     * Called by sub-panels after any data mutation to keep all views consistent.
     */
    public void refreshAll() {
        catalogPanel.refresh();
        membersPanel.refresh();
        loansPanel.refresh();
        dashboardPanel.refresh();
        statisticsPanel.refresh();
        data.save();
    }

    private void confirmAndExit() {
        data.save();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Your data has been saved. Exit NovaLib?",
                "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            autoSaveTimer.stop();
            dispose();
            System.exit(0);
        }
    }
}