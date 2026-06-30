package app;

import ui.Theme;
import javax.swing.*;

/**
 * Entry point for the NovaLib Library Catalog application.
 */
public class Main {
    public static void main(String[] args) {
        Theme.applyGlobalDefaults();
        SwingUtilities.invokeLater(MainFrame::new);
    }
}