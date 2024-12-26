package com.booktracker.model;

import java.time.LocalDateTime;

/**
 * 图书实体类，表示系统中的一本图书
 * 
 * 该类存储图书的基本信息，包括：
 * - 书名
 * - 作者
 * - 总页数
 * - 添加日期
 * 
 * 每本书都有唯一的ID，用于在系统中识别和追踪
 * 
 * @author Devin AI
 * @version 1.0
 * @see ReadingProgress
 * @see ReadingSession
 */
public class Book {
    private Long id;
    private String title;
    private String author;
    private int totalPages;
    private LocalDateTime dateAdded;

    public Book() {}

    public Book(String title, String author, int totalPages) {
        this.title = title;
        this.author = author;
        this.totalPages = totalPages;
        this.dateAdded = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public LocalDateTime getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDateTime dateAdded) { this.dateAdded = dateAdded; }
}
