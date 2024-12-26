package com.booktracker.model;

/**
 * 用户实体类，代表系统中的一个用户
 * 
 * 存储用户的基本信息：
 * - 用户名
 * - 数据库路径（每个用户独立的数据存储位置）
 * 
 * 数据库路径根据用户名自动生成，确保每个用户
 * 的数据互相隔离且易于定位
 * 
 * @author Devin AI
 * @version 1.0
 * @see ReadingProgress
 * @see ReadingSession
 */
public class User {
    private Long id;
    private String username;
    private String databasePath;

    public User() {}

    public User(String username) {
        this.username = username;
        this.databasePath = "data/" + username.toLowerCase().replaceAll("\\s+", "_");
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDatabasePath() { return databasePath; }
    public void setDatabasePath(String databasePath) { this.databasePath = databasePath; }
}
