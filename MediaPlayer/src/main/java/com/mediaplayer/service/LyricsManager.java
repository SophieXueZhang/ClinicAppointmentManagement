package com.mediaplayer.service;

import javafx.util.Duration;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class LyricsManager {
    private Map<Long, String> lyrics;
    private JTextArea lyricsArea;
    private javax.swing.Timer lyricsTimer;
    private long currentTime = 0;
    private final JPanel mainPanel;

    public LyricsManager(JPanel mainPanel) {
        this.mainPanel = mainPanel;
        this.lyrics = new TreeMap<>();
        initComponents();
    }

    private void initComponents() {
        lyricsArea = new JTextArea();
        lyricsArea.setEditable(false);
        lyricsArea.setFont(new Font("Arial", Font.PLAIN, 14));
        lyricsArea.setLineWrap(true);
        lyricsArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(lyricsArea);
        scrollPane.setPreferredSize(new Dimension(200, 0));
        mainPanel.add(scrollPane, BorderLayout.EAST);
    }

    public void loadLyrics(File mediaFile) {
        lyrics.clear();
        String lyricsPath = mediaFile.getPath().replaceAll("\\.[^.]+$", ".lrc");
        File lyricsFile = new File(lyricsPath);

        if (lyricsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(lyricsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    parseLyricsLine(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseLyricsLine(String line) {
        try {
            String[] parts = line.split("\\[|\\]");
            if (parts.length < 2) return;
            
            String text = parts[parts.length - 1].trim();
            if (text.isEmpty()) return;

            for (int i = 0; i < parts.length - 1; i++) {
                String timeStr = parts[i];
                if (timeStr.matches("\\d{2}:\\d{2}\\.\\d{2}")) {
                    long time = com.mediaplayer.util.TimeUtils.parseLyricsTime(timeStr);
                    lyrics.put(time, text);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing lyrics line: " + line);
            e.printStackTrace();
        }
    }

    private long parseLyricsTime(String timeStr) {
        String[] parts = timeStr.split("[:.]");
        return Long.parseLong(parts[0]) * 60000 +
               Long.parseLong(parts[1]) * 1000 +
               Long.parseLong(parts[2]) * 10;
    }

    public void updateLyrics(Duration currentTime) {
        if (currentTime == null || lyrics.isEmpty()) {
            return;
        }

        long time = (long) currentTime.toMillis();
        
        // Find the most recent lyric for the current time
        Map.Entry<Long, String> currentEntry = lyrics.entrySet().stream()
                .filter(entry -> entry.getKey() <= time)
                .reduce((first, second) -> second)
                .orElse(null);
                
        // Find the next lyric for highlighting
        Map.Entry<Long, String> nextEntry = lyrics.entrySet().stream()
                .filter(entry -> entry.getKey() > time)
                .findFirst()
                .orElse(null);

        StringBuilder displayText = new StringBuilder();
        if (currentEntry != null) {
            displayText.append(currentEntry.getValue());
            if (nextEntry != null) {
                displayText.append("\n\n>> ").append(nextEntry.getValue());
            }
        }

        final String textToDisplay = displayText.toString();
        SwingUtilities.invokeLater(() -> {
            lyricsArea.setText(textToDisplay);
            lyricsArea.setCaretPosition(0);
            
            // Highlight current lyric
            if (!textToDisplay.isEmpty()) {
                try {
                    int firstLineEnd = textToDisplay.indexOf("\n");
                    if (firstLineEnd == -1) firstLineEnd = textToDisplay.length();
                    lyricsArea.getHighlighter().addHighlight(0, firstLineEnd, 
                        new javax.swing.text.DefaultHighlighter.DefaultHighlightPainter(
                            new Color(255, 255, 0, 70)));
                } catch (javax.swing.text.BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
