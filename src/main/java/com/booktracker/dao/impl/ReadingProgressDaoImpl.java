package com.booktracker.dao.impl;

import com.booktracker.dao.ReadingProgressDao;
import com.booktracker.exception.BookTrackerException;
import com.booktracker.model.ReadingProgress;
import com.booktracker.dao.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ReadingProgressDao接口的实现类
 * 负责处理阅读进度相关的数据库操作
 */
public class ReadingProgressDaoImpl implements ReadingProgressDao {
    private String username;
    private final DatabaseManager dbManager;

    public ReadingProgressDaoImpl() {
        this.dbManager = new DatabaseManager();
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void create(ReadingProgress progress) throws BookTrackerException {
        String sql = "INSERT INTO reading_progress (book_id, user_id, current_page, progress_percentage, last_read_date) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, progress.getBookId());
            stmt.setLong(2, progress.getUserId());
            stmt.setInt(3, progress.getCurrentPage());
            stmt.setDouble(4, progress.getProgressPercentage());
            stmt.setTimestamp(5, Timestamp.valueOf(progress.getLastReadDate()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new BookTrackerException("创建阅读进度记录失败", e);
        }
    }

    @Override
    public ReadingProgress findByBookAndUser(Long bookId, Long userId) throws BookTrackerException {
        String sql = "SELECT * FROM reading_progress WHERE book_id = ? AND user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToReadingProgress(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new BookTrackerException("查询阅读进度失败", e);
        }
    }

    @Override
    public List<ReadingProgress> findAllByUser(Long userId) throws BookTrackerException {
        String sql = "SELECT * FROM reading_progress WHERE user_id = ?";
        List<ReadingProgress> progressList = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                progressList.add(mapResultSetToReadingProgress(rs));
            }
            return progressList;
        } catch (SQLException e) {
            throw new BookTrackerException("查询用户阅读进度列表失败", e);
        }
    }

    @Override
    public void update(ReadingProgress progress) throws BookTrackerException {
        String sql = "UPDATE reading_progress SET current_page = ?, progress_percentage = ?, " +
                    "last_read_date = ? WHERE book_id = ? AND user_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, progress.getCurrentPage());
            stmt.setDouble(2, progress.getProgressPercentage());
            stmt.setTimestamp(3, Timestamp.valueOf(progress.getLastReadDate()));
            stmt.setLong(4, progress.getBookId());
            stmt.setLong(5, progress.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new BookTrackerException("更新阅读进度失败", e);
        }
    }

    private ReadingProgress mapResultSetToReadingProgress(ResultSet rs) throws SQLException {
        ReadingProgress progress = new ReadingProgress(
            rs.getLong("book_id"),
            rs.getLong("user_id"),
            rs.getInt("current_page"),
            0 // Total pages will be set from book data
        );
        progress.setProgressPercentage(rs.getDouble("progress_percentage"));
        progress.setLastReadDate(rs.getTimestamp("last_read_date").toLocalDateTime());
        return progress;
    }
}
