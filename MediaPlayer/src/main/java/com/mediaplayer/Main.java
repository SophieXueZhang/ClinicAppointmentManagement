package com.mediaplayer;

import javafx.application.Platform;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Initialize JavaFX Platform
        Platform.startup(() -> {});

        SwingUtilities.invokeLater(() -> {
            new com.mediaplayer.application.AdvancedMediaPlayer().setVisible(true);
        });
    }
}
