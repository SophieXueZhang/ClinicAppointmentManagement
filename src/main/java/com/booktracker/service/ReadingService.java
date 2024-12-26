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

    public void updateReadingProgress(Long bookId, int currentPage, int totalPages) {
        try {
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
        } catch (SQLException e) {
            throw new BookTrackerException("更新阅读进度失败: 图书ID " + bookId, e);
        }
    }

    public void recordReadingSession(Long bookId, LocalDateTime startTime, LocalDateTime endTime, 
                                   int startPage, int endPage) {
        try {
            ReadingSession session = new ReadingSession(
                bookId,
                currentUserId,
                startTime,
                endTime,
                endPage - startPage
            );
            sessionDao.create(session);
            updateReadingProgress(bookId, endPage, getBookTotalPages(bookId));
        } catch (SQLException e) {
            throw new BookTrackerException("记录阅读会话失败: 图书ID " + bookId, e);
        }
    }

    public List<ReadingSession> getReadingSessions(Long bookId) {
        try {
            return sessionDao.findByBookAndUser(bookId, currentUserId);
        } catch (SQLException e) {
            throw new BookTrackerException("获取阅读会话记录失败: 图书ID " + bookId, e);
        }
    }

    public List<ReadingSession> getReadingSessionsInTimeRange(LocalDateTime start, LocalDateTime end) {
        try {
            return sessionDao.findByUserInTimeRange(currentUserId, start, end);
        } catch (SQLException e) {
            throw new BookTrackerException("获取时间范围内的阅读会话记录失败", e);
        }
    }

    public ReadingProgress getReadingProgress(Long bookId) {
        try {
            return progressDao.findByBookAndUser(bookId, currentUserId);
        } catch (SQLException e) {
            throw new BookTrackerException("获取阅读进度失败: 图书ID " + bookId, e);
        }
    }

    private int getBookTotalPages(Long bookId) {
        try {
            Book book = bookDao.findById(bookId);
            return book != null ? book.getTotalPages() : 0;
        } catch (SQLException e) {
            throw new BookTrackerException("获取图书总页数失败: 图书ID " + bookId, e);
        }
    }

    public double calculateAverageReadingSpeed(Long bookId) {
        try {
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
        } catch (Exception e) {
            throw new BookTrackerException("计算平均阅读速度失败: 图书ID " + bookId, e);
        }
    }

    public double calculateCompletionRate(Long bookId) {
        try {
            ReadingProgress progress = getReadingProgress(bookId);
            return progress != null ? progress.getProgressPercentage() : 0.0;
        } catch (Exception e) {
            throw new BookTrackerException("计算完成率失败: 图书ID " + bookId, e);
        }
    }

    public int getTotalPagesRead(LocalDateTime start, LocalDateTime end) {
        try {
            List<ReadingSession> sessions = getReadingSessionsInTimeRange(start, end);
            return sessions.stream().mapToInt(ReadingSession::getPagesRead).sum();
        } catch (Exception e) {
            throw new BookTrackerException("获取总阅读页数失败", e);
        }
    }

    public double getTotalReadingHours(LocalDateTime start, LocalDateTime end) {
        try {
            List<ReadingSession> sessions = getReadingSessionsInTimeRange(start, end);
            return sessions.stream()
                .mapToDouble(session -> session.getDuration().toMinutes() / 60.0)
                .sum();
        } catch (Exception e) {
            throw new BookTrackerException("获取总阅读时间失败", e);
        }
    }
}
