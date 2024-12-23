package com.mediaplayer.ui.components;

import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class CustomControlPanel extends JPanel {
    private JSlider progressSlider, volumeSlider;
    private JButton playButton, stopButton, prevButton, nextButton;
    private JToggleButton muteButton, fullscreenButton;
    private JLabel timeLabel;
    private final com.mediaplayer.application.AdvancedMediaPlayer parent;
    private Timer updateTimer;

    public CustomControlPanel(com.mediaplayer.application.AdvancedMediaPlayer parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(5, 5, 5, 5));
        initComponents();
        setupLayout();
        setupControlListeners();
        setupUpdateTimer();
    }

    private void initComponents() {
        progressSlider = new JSlider(0, 100, 0);
        volumeSlider = new JSlider(0, 100, 80);
        playButton = new JButton("Play");
        stopButton = new JButton("Stop");
        prevButton = new JButton("Prev");
        nextButton = new JButton("Next");
        muteButton = new JToggleButton("Mute");
        fullscreenButton = new JToggleButton("Full Screen");
        timeLabel = new JLabel("00:00 / 00:00");
    }

    private void setupLayout() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        buttonPanel.add(prevButton);
        buttonPanel.add(playButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(muteButton);
        buttonPanel.add(volumeSlider);
        buttonPanel.add(fullscreenButton);

        add(progressSlider, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(timeLabel, BorderLayout.EAST);
    }

    private void setupControlListeners() {
        playButton.addActionListener(e -> parent.togglePlayPause());
        stopButton.addActionListener(e -> parent.stop());
        prevButton.addActionListener(e -> {}); // Will be connected to PlaylistManager
        nextButton.addActionListener(e -> {}); // Will be connected to PlaylistManager
        muteButton.addActionListener(e -> parent.toggleMute());
        fullscreenButton.addActionListener(e -> parent.toggleFullScreen());

        volumeSlider.addChangeListener(e -> {
            MediaPlayer mediaPlayer = parent.getMediaPlayer();
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
            }
        });

        progressSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int width = progressSlider.getWidth();
                int value = (int)((double)e.getX() / width * 100);
                parent.seek(value);
            }
        });
    }

    private void setupUpdateTimer() {
        updateTimer = new Timer(100, e -> updateProgress());
        updateTimer.start();
    }

    private void updateProgress() {
        MediaPlayer mediaPlayer = parent.getMediaPlayer();
        if (mediaPlayer != null) {
            Duration current = mediaPlayer.getCurrentTime();
            Duration total = mediaPlayer.getTotalDuration();
            
            if (current != null && total != null) {
                double progress = (current.toMillis() / total.toMillis()) * 100;
                progressSlider.setValue((int) progress);
                
                String timeStr = String.format("%02d:%02d / %02d:%02d",
                    (int) current.toMinutes(),
                    (int) current.toSeconds() % 60,
                    (int) total.toMinutes(),
                    (int) total.toSeconds() % 60);
                timeLabel.setText(timeStr);
            }
        }
    }

    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
}
