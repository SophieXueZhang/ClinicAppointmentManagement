package com.mediaplayer.config;

import javafx.scene.media.MediaPlayer;
import java.util.Properties;
import java.io.*;

public class PlayerConfig {
    private static final String CONFIG_FILE = "config/default-settings.properties";
    private Properties properties;

    public PlayerConfig() {
        properties = new Properties();
        loadDefaults();
        loadFromFile();
    }

    private void loadDefaults() {
        properties.setProperty("volume", "0.8");
        properties.setProperty("mute", "false");
        properties.setProperty("autoplay", "true");
        properties.setProperty("repeat", "false");
        properties.setProperty("showLyrics", "true");
    }

    private void loadFromFile() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void configureMediaPlayer(MediaPlayer mediaPlayer) {
        mediaPlayer.setVolume(Double.parseDouble(properties.getProperty("volume", "0.8")));
        mediaPlayer.setMute(Boolean.parseBoolean(properties.getProperty("mute", "false")));
        mediaPlayer.setAutoPlay(Boolean.parseBoolean(properties.getProperty("autoplay", "true")));

        String repeat = properties.getProperty("repeat", "false");
        if (Boolean.parseBoolean(repeat)) {
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        }
    }

    public void save() {
        File configDir = new File("config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        try (OutputStream output = new FileOutputStream("config/default-settings.properties")) {
            properties.store(output, "Media Player Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
