package app;

import javax.swing.*;
import ui.Theme;

public class Main {
    public static void main(String[] args) {
        Theme.applyGlobalDefaults();
        SwingUtilities.invokeLater(MainFrame::new);
    }
}