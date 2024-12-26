package com.booktracker.model;

import java.time.LocalDateTime;

public class ReadingProgress {
    private Long id;
    private Long bookId;
    private Long userId;
    private int currentPage;
    private double progressPercentage;
    private LocalDateTime lastReadDate;

    public ReadingProgress() {}

    public ReadingProgress(Long bookId, Long userId, int currentPage, int totalPages) {
        this.bookId = bookId;
        this.userId = userId;
        this.currentPage = currentPage;
        this.progressPercentage = (double) currentPage / totalPages * 100;
        this.lastReadDate = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public double getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
    public LocalDateTime getLastReadDate() { return lastReadDate; }
    public void setLastReadDate(LocalDateTime lastReadDate) { this.lastReadDate = lastReadDate; }
}
