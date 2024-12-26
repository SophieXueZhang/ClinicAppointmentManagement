package com.booktracker.service;

import com.booktracker.dao.BookDao;
import com.booktracker.dao.ReadingProgressDao;
import com.booktracker.dao.ReadingSessionDao;
import com.booktracker.model.Book;
import com.booktracker.model.ReadingProgress;
import com.booktracker.model.ReadingSession;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ReadingService {
    private final BookDao bookDao;
    private final ReadingProgressDao progressDao;
    private final ReadingSessionDao sessionDao;
    private Long currentUserId;

    public ReadingService() {
        this.bookDao = new BookDao();
        this.progressDao = new ReadingProgressDao();
        this.sessionDao = new ReadingSessionDao();
    }

    public void setCurrentUser(Long userId) {
        this.currentUserId = userId;
    }

    public void updateReadingProgress(Long bookId, int currentPage, int totalPages) throws SQLException {
        ReadingProgress progress = progressDao.findByBookAndUser(bookId, currentUserId);
        if (progress == null) {
            progress = new ReadingProgress(bookId, currentUserId, currentPage, totalPages);
            progressDao.create(progress);
        } else {
            progress.setCurrentPage(currentPage);
            progress.setProgressPercentage((double) currentPage / totalPages * 100);
            progress.setLastReadDate(LocalDateTime.now());
            progressDao.update(progress);
        }
    }

    public void recordReadingSession(Long bookId, LocalDateTime startTime, LocalDateTime endTime, 
                                   int startPage, int endPage) throws SQLException {
        ReadingSession session = new ReadingSession(
            bookId,
            currentUserId,
            startTime,
            endTime,
            endPage - startPage
        );
        sessionDao.create(session);
        updateReadingProgress(bookId, endPage, getBookTotalPages(bookId));
    }

    public List<ReadingSession> getReadingSessions(Long bookId) throws SQLException {
        return sessionDao.findByBookAndUser(bookId, currentUserId);
    }

    public List<ReadingSession> getReadingSessionsInTimeRange(LocalDateTime start, LocalDateTime end) 
            throws SQLException {
        return sessionDao.findByUserInTimeRange(currentUserId, start, end);
    }

    public ReadingProgress getReadingProgress(Long bookId) throws SQLException {
        return progressDao.findByBookAndUser(bookId, currentUserId);
    }

    private int getBookTotalPages(Long bookId) throws SQLException {
        Book book = bookDao.findById(bookId);
        return book != null ? book.getTotalPages() : 0;
    }

    public double calculateAverageReadingSpeed(Long bookId) throws SQLException {
        List<ReadingSession> sessions = getReadingSessions(bookId);
        if (sessions.isEmpty()) {
            return 0.0;
        }

        int totalPagesRead = 0;
        double totalHours = 0.0;

        for (ReadingSession session : sessions) {
            totalPagesRead += session.getPagesRead();
            totalHours += session.getDuration().toMinutes() / 60.0;
        }

        return totalHours > 0 ? totalPagesRead / totalHours : 0.0;
    }

    public double calculateCompletionRate(Long bookId) throws SQLException {
        ReadingProgress progress = getReadingProgress(bookId);
        return progress != null ? progress.getProgressPercentage() : 0.0;
    }

    public int getTotalPagesRead(LocalDateTime start, LocalDateTime end) throws SQLException {
        List<ReadingSession> sessions = getReadingSessionsInTimeRange(start, end);
        return sessions.stream().mapToInt(ReadingSession::getPagesRead).sum();
    }

    public double getTotalReadingHours(LocalDateTime start, LocalDateTime end) throws SQLException {
        List<ReadingSession> sessions = getReadingSessionsInTimeRange(start, end);
        return sessions.stream()
            .mapToDouble(session -> session.getDuration().toMinutes() / 60.0)
            .sum();
    }
}
