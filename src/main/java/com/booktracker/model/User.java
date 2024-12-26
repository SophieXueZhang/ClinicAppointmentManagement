package com.booktracker.model;

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
