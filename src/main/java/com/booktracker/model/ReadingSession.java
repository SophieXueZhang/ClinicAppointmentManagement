package com.booktracker.model;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * 阅读会话实体类，记录单次阅读活动的详细信息
 * 
 * 记录每次阅读的：
 * - 开始和结束时间
 * - 阅读页数
 * - 阅读时长（通过计算得出）
 * - 阅读速度（页数/小时）
 * 
 * 通过bookId和userId关联到特定用户的特定图书
 * 提供计算阅读时长和阅读速度的方法
 * 
 * @author Devin AI
 * @version 1.0
 * @see Book
 * @see User
 */
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

    /**
     * 计算本次阅读会话的持续时间
     * 
     * @return 返回开始时间到结束时间的时间间隔
     */
    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    /**
     * 计算本次阅读会话的阅读速度
     * 
     * @return 返回每小时阅读页数，如果阅读时间为0则返回0
     */
    public double getPagesPerHour() {
        double hours = getDuration().toMinutes() / 60.0;
        return hours > 0 ? pagesRead / hours : 0;
    }
}
