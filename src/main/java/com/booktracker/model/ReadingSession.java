package com.booktracker.model;

import java.time.LocalDateTime;
import java.time.Duration;

public class ReadingSession {
    private Long id;
    private Long bookId;
    private Long userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int pagesRead;

    public ReadingSession() {}

    public ReadingSession(Long bookId, Long userId, LocalDateTime startTime, LocalDateTime endTime, int pagesRead) {
        this.bookId = bookId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.pagesRead = pagesRead;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public int getPagesRead() { return pagesRead; }
    public void setPagesRead(int pagesRead) { this.pagesRead = pagesRead; }

    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    public double getPagesPerHour() {
        double hours = getDuration().toMinutes() / 60.0;
        return hours > 0 ? pagesRead / hours : 0;
    }
}
