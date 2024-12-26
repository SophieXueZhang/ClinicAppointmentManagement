package com.booktracker.dao;

import com.booktracker.model.ReadingProgress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReadingProgressDao {
    private final DatabaseManager dbManager;
    private String username = "default";

    public ReadingProgressDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void create(ReadingProgress progress) throws SQLException {
        String sql = "INSERT INTO reading_progress (book_id, user_id, current_page, progress_percentage, last_read_date) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, progress.getBookId());
            stmt.setLong(2, progress.getUserId());
            stmt.setInt(3, progress.getCurrentPage());
            stmt.setDouble(4, progress.getProgressPercentage());
            stmt.setTimestamp(5, Timestamp.valueOf(progress.getLastReadDate()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    progress.setId(rs.getLong(1));
                }
            }
        }
    }

    public ReadingProgress findByBookAndUser(Long bookId, Long userId) throws SQLException {
        String sql = "SELECT * FROM reading_progress WHERE book_id = ? AND user_id = ?";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProgress(rs);
                }
            }
        }
        return null;
    }

    public List<ReadingProgress> findByUser(Long userId) throws SQLException {
        List<ReadingProgress> progressList = new ArrayList<>();
        String sql = "SELECT * FROM reading_progress WHERE user_id = ? ORDER BY last_read_date DESC";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    progressList.add(mapResultSetToProgress(rs));
                }
            }
        }
        return progressList;
    }

    public void update(ReadingProgress progress) throws SQLException {
        String sql = "UPDATE reading_progress SET current_page = ?, progress_percentage = ?, " +
                    "last_read_date = ? WHERE book_id = ? AND user_id = ?";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, progress.getCurrentPage());
            stmt.setDouble(2, progress.getProgressPercentage());
            stmt.setTimestamp(3, Timestamp.valueOf(progress.getLastReadDate()));
            stmt.setLong(4, progress.getBookId());
            stmt.setLong(5, progress.getUserId());
            stmt.executeUpdate();
        }
    }

    private ReadingProgress mapResultSetToProgress(ResultSet rs) throws SQLException {
        ReadingProgress progress = new ReadingProgress();
        progress.setId(rs.getLong("id"));
        progress.setBookId(rs.getLong("book_id"));
        progress.setUserId(rs.getLong("user_id"));
        progress.setCurrentPage(rs.getInt("current_page"));
        progress.setProgressPercentage(rs.getDouble("progress_percentage"));
        progress.setLastReadDate(rs.getTimestamp("last_read_date").toLocalDateTime());
        return progress;
    }
}
