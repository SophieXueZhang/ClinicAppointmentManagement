package com.mediaplayer.application;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.embed.swing.JFXPanel;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

import com.mediaplayer.service.PlaylistManager;
import com.mediaplayer.service.LyricsManager;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.border.*;

public class AdvancedMediaPlayer extends JFrame {
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private JFXPanel mediaPanel;
    private com.mediaplayer.service.PlaylistManager playlistManager;
    private com.mediaplayer.service.LyricsManager lyricsManager;
    private com.mediaplayer.ui.components.CustomControlPanel controlPanel;
    private Component videoComponent;
    private JPanel mainPanel, playlistPanel;
    private JSplitPane splitPane;
    private boolean isFullScreen = false;

    // UI Components
    private JList<String> playlist;
    private DefaultListModel<String> playlistModel;

    public AdvancedMediaPlayer() {
        setTitle("Advanced Media Player");
        initComponents();
        setupLayout();
        initPlaylistManager();
        initLyricsManager();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
    }

    private void initPlaylistManager() {
        playlistManager = new PlaylistManager(playlistModel, this);
    }

    private void initLyricsManager() {
        lyricsManager = new LyricsManager(mainPanel);
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout());
        playlistPanel = new JPanel(new BorderLayout());
        playlistModel = new DefaultListModel<>();
        playlist = new JList<>(playlistModel);
        controlPanel = new com.mediaplayer.ui.components.CustomControlPanel(this);
        mediaPanel = new JFXPanel();
        mainPanel.add(mediaPanel, BorderLayout.CENTER);

        // Setup playlist
        playlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                playlistManager.playSelected(playlist.getSelectedIndex());
            }
        });

        JScrollPane playlistScroll = new JScrollPane(playlist);
        playlistPanel.add(new JLabel(" Playlist", SwingConstants.CENTER),
                BorderLayout.NORTH);
        playlistPanel.add(playlistScroll, BorderLayout.CENTER);

        // Add drag & drop support
        new com.mediaplayer.util.FileDrop(playlist, files -> {
            for (File file : files) {
                playlistManager.addFile(file);
            }
        });
    }

    private void setupLayout() {
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainPanel,
                playlistPanel);
        splitPane.setResizeWeight(0.8);
        add(splitPane);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
    }

    // Utility methods
    public void loadMedia(File file) throws Exception {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView = new MediaView(mediaPlayer);

        // Configure MediaPlayer
        com.mediaplayer.config.PlayerConfig config = new com.mediaplayer.config.PlayerConfig();
        config.configureMediaPlayer(mediaPlayer);

        // Create and configure event adapter
        com.mediaplayer.event.MediaEventAdapter eventAdapter = new com.mediaplayer.event.MediaEventAdapter(mediaPlayer);
        
        eventAdapter.setOnPlaybackEnd(() -> {
            if (mediaPlayer.getCycleCount() != MediaPlayer.INDEFINITE) {
                playlistManager.playNext();
            }
        });
        
        eventAdapter.setOnMediaReady(() -> {
            mediaPlayer.play();
        });
        
        eventAdapter.setOnTimeUpdate(newTime -> {
            if (file.getName().toLowerCase().endsWith(".mp3")) {
                lyricsManager.updateLyrics(newTime);
            }
        });
        
        eventAdapter.setOnError(error -> {
            showError("Media playback error: " + error.getMessage());
        });

        // Setup media view in JFXPanel
        javafx.application.Platform.runLater(() -> {
            // Configure MediaView
            mediaView.setPreserveRatio(true);
            mediaView.setFitWidth(mediaPanel.getWidth());
            mediaView.setFitHeight(mediaPanel.getHeight());
            
            // Add resize listener to update MediaView size
            mediaPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    javafx.application.Platform.runLater(() -> {
                        mediaView.setFitWidth(mediaPanel.getWidth());
                        mediaView.setFitHeight(mediaPanel.getHeight());
                    });
                }
            });
            
            // Add to JFXPanel
            javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane();
            root.getChildren().add(mediaView);
            mediaPanel.setScene(new javafx.scene.Scene(root));
        });

        if (file.getName().toLowerCase().endsWith(".mp3")) {
            lyricsManager.loadLyrics(file);
        }
    }

    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.seek(Duration.ZERO);
        }
    }

    public void seek(int percentage) {
        if (mediaPlayer != null) {
            Duration duration = mediaPlayer.getTotalDuration();
            if (duration != null) {
                double seekTime = duration.toMillis() * percentage / 100.0;
                mediaPlayer.seek(new Duration(seekTime));
            }
        }
    }

    public void toggleMute() {
        if (mediaPlayer != null) {
            mediaPlayer.setMute(!mediaPlayer.isMute());
        }
    }

    public void toggleFullScreen() {
        if (mediaView != null) {
            if (!isFullScreen) {
                dispose();
                setUndecorated(true);
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                mainPanel.remove(controlPanel);
                setVisible(true);
            } else {
                dispose();
                setUndecorated(false);
                setExtendedState(JFrame.NORMAL);
                mainPanel.add(controlPanel, BorderLayout.SOUTH);
                setVisible(true);
            }
            isFullScreen = !isFullScreen;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // Getters for inner classes
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
