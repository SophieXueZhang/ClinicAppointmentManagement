package com.mediaplayer.event;

import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.util.function.Consumer;

public class MediaEventAdapter {
    private final MediaPlayer mediaPlayer;
    private Consumer<Duration> onTimeUpdate;
    private Runnable onPlaybackEnd;
    private Runnable onMediaReady;
    private Consumer<Exception> onError;

    public MediaEventAdapter(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        setupDefaultHandlers();
    }

    private void setupDefaultHandlers() {
        mediaPlayer.setOnReady(() -> {
            if (onMediaReady != null) {
                onMediaReady.run();
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            if (onPlaybackEnd != null) {
                onPlaybackEnd.run();
            }
        });

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (onTimeUpdate != null) {
                onTimeUpdate.accept(newValue);
            }
        });

        mediaPlayer.setOnError(() -> {
            if (onError != null && mediaPlayer.getError() != null) {
                onError.accept(mediaPlayer.getError());
            }
        });
    }

    public void setOnTimeUpdate(Consumer<Duration> handler) {
        this.onTimeUpdate = handler;
    }

    public void setOnPlaybackEnd(Runnable handler) {
        this.onPlaybackEnd = handler;
    }

    public void setOnMediaReady(Runnable handler) {
        this.onMediaReady = handler;
    }

    public void setOnError(Consumer<Exception> handler) {
        this.onError = handler;
    }
}
