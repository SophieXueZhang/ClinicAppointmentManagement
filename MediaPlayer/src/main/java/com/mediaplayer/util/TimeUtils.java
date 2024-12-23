package com.mediaplayer.util;

import javafx.util.Duration;

public class TimeUtils {
    public static String formatDuration(Duration duration) {
        if (duration == null) {
            return "00:00";
        }
        long totalSeconds = (long) duration.toSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static long parseLyricsTime(String timeStr) {
        String[] parts = timeStr.split("[:.]");
        return Long.parseLong(parts[0]) * 60000 +
               Long.parseLong(parts[1]) * 1000 +
               Long.parseLong(parts[2]) * 10;
    }
}
