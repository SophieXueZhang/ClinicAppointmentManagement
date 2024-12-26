package com.booktracker.dao;

import com.booktracker.model.ReadingSession;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReadingSessionDao {
    private final DatabaseManager dbManager;
    private String username = "default";

    public ReadingSessionDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void create(ReadingSession session) throws SQLException {
        String sql = "INSERT INTO reading_sessions (book_id, user_id, start_time, end_time, pages_read) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, session.getBookId());
            stmt.setLong(2, session.getUserId());
            stmt.setTimestamp(3, Timestamp.valueOf(session.getStartTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(session.getEndTime()));
            stmt.setInt(5, session.getPagesRead());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    session.setId(rs.getLong(1));
                }
            }
        }
    }

    public List<ReadingSession> findByBookAndUser(Long bookId, Long userId) throws SQLException {
        List<ReadingSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM reading_sessions WHERE book_id = ? AND user_id = ? ORDER BY start_time DESC";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookId);
            stmt.setLong(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToSession(rs));
                }
            }
        }
        return sessions;
    }

    public List<ReadingSession> findByUserInTimeRange(Long userId, LocalDateTime start, LocalDateTime end) 
            throws SQLException {
        List<ReadingSession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM reading_sessions WHERE user_id = ? AND start_time >= ? AND end_time <= ? " +
                    "ORDER BY start_time DESC";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(start));
            stmt.setTimestamp(3, Timestamp.valueOf(end));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToSession(rs));
                }
            }
        }
        return sessions;
    }

    private ReadingSession mapResultSetToSession(ResultSet rs) throws SQLException {
        ReadingSession session = new ReadingSession();
        session.setId(rs.getLong("id"));
        session.setBookId(rs.getLong("book_id"));
        session.setUserId(rs.getLong("user_id"));
        session.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        session.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        session.setPagesRead(rs.getInt("pages_read"));
        return session;
    }
}
