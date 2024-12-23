package com.mediaplayer.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;

public class PlaylistManager {
    private List<File> mediaFiles;
    private int currentIndex = -1;
    private final DefaultListModel<String> playlistModel;
    private final com.mediaplayer.application.AdvancedMediaPlayer player;

    public PlaylistManager(DefaultListModel<String> model, com.mediaplayer.application.AdvancedMediaPlayer player) {
        this.mediaFiles = new ArrayList<>();
        this.playlistModel = model;
        this.player = player;
    }

    public void addFile(File file) {
        mediaFiles.add(file);
        playlistModel.addElement(file.getName());
        if (currentIndex == -1) {
            playSelected(0);
        }
    }

    public void playSelected(int index) {
        if (index >= 0 && index < mediaFiles.size()) {
            currentIndex = index;
            try {
                player.loadMedia(mediaFiles.get(currentIndex));
            } catch (Exception e) {
                // Handle error
            }
        }
    }

    public void playNext() {
        if (currentIndex < mediaFiles.size() - 1) {
            playSelected(currentIndex + 1);
        }
    }

    public void playPrevious() {
        if (currentIndex > 0) {
            playSelected(currentIndex - 1);
        }
    }
}
