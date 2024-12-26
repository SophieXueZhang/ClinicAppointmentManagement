package com.booktracker.model;

import java.time.LocalDateTime;

/**
 * 阅读进度实体类，记录用户对特定图书的阅读进度
 * 
 * 该类跟踪：
 * - 当前阅读页数
 * - 阅读进度百分比
 * - 最后阅读时间
 * 
 * 通过bookId和userId关联特定用户的特定图书
 * 进度百分比自动根据当前页数和总页数计算
 * 
 * @author Devin AI
 * @version 1.0
 * @see Book
 * @see User
 */
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
