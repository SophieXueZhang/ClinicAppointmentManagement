package com.booktracker.dao.impl;

import com.booktracker.dao.ReadingSessionDao;
import com.booktracker.exception.BookTrackerException;
import com.booktracker.model.ReadingSession;
import com.booktracker.dao.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ReadingSessionDao接口的实现类
 * 负责处理阅读会话相关的数据库操作
 */
public class ReadingSessionDaoImpl implements ReadingSessionDao {
    private String username;
    private final DatabaseManager dbManager;

    public ReadingSessionDaoImpl() {
        this.dbManager = new DatabaseManager();
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void create(ReadingSession session) throws BookTrackerException {
        String sql = "INSERT INTO reading_sessions (book_id, user_id, start_time, end_time, pages_read) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, session.getBookId());
            stmt.setLong(2, session.getUserId());
            stmt.setTimestamp(3, Timestamp.valueOf(session.getStartTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(session.getEndTime()));
            stmt.setInt(5, session.getPagesRead());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new BookTrackerException("创建阅读会话记录失败", e);
        }
    }

    @Override
    public List<ReadingSession> findByBookAndUser(Long bookId, Long userId) throws BookTrackerException {
        String sql = "SELECT * FROM reading_sessions WHERE book_id = ? AND user_id = ?";
        List<ReadingSession> sessions = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sessions.add(mapResultSetToReadingSession(rs));
            }
            return sessions;
        } catch (SQLException e) {
            throw new BookTrackerException("查询阅读会话记录失败", e);
        }
    }

    @Override
    public List<ReadingSession> findByUserInTimeRange(Long userId, LocalDateTime start, LocalDateTime end) 
            throws BookTrackerException {
        String sql = "SELECT * FROM reading_sessions WHERE user_id = ? AND start_time >= ? AND end_time <= ?";
        List<ReadingSession> sessions = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(start));
            stmt.setTimestamp(3, Timestamp.valueOf(end));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sessions.add(mapResultSetToReadingSession(rs));
            }
            return sessions;
        } catch (SQLException e) {
            throw new BookTrackerException("查询时间范围内的阅读会话记录失败", e);
        }
    }

    private ReadingSession mapResultSetToReadingSession(ResultSet rs) throws SQLException {
        return new ReadingSession(
            rs.getLong("book_id"),
            rs.getLong("user_id"),
            rs.getTimestamp("start_time").toLocalDateTime(),
            rs.getTimestamp("end_time").toLocalDateTime(),
            rs.getInt("pages_read")
        );
    }
}
