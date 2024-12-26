package com.booktracker.service;

import com.booktracker.dao.BookDao;
import com.booktracker.dao.ReadingProgressDao;
import com.booktracker.dao.ReadingSessionDao;
import com.booktracker.exception.BookTrackerException;
import com.booktracker.model.Book;
import com.booktracker.model.ReadingProgress;
import com.booktracker.model.ReadingSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 阅读服务测试类
 */
public class ReadingServiceTest {
    @Mock
    private BookDao bookDao;
    @Mock
    private ReadingProgressDao progressDao;
    @Mock
    private ReadingSessionDao sessionDao;

    private ReadingService readingService;
    private final Long TEST_USER_ID = 1L;
    private final Long TEST_BOOK_ID = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        readingService = new ReadingService();
        readingService.setCurrentUser(TEST_USER_ID);
    }

    @Test
    void testUpdateReadingProgress_Success() throws SQLException {
        ReadingProgress progress = new ReadingProgress(TEST_BOOK_ID, TEST_USER_ID, 50, 100);
        when(progressDao.findByBookAndUser(TEST_BOOK_ID, TEST_USER_ID)).thenReturn(progress);

        readingService.updateReadingProgress(TEST_BOOK_ID, 75, 100);

        verify(progressDao).update(any(ReadingProgress.class));
    }

    @Test
    void testUpdateReadingProgress_DatabaseError() throws SQLException {
        when(progressDao.findByBookAndUser(TEST_BOOK_ID, TEST_USER_ID))
            .thenThrow(new SQLException("数据库连接失败"));

        BookTrackerException exception = assertThrows(BookTrackerException.class, () ->
            readingService.updateReadingProgress(TEST_BOOK_ID, 50, 100));
        assertTrue(exception.getMessage().contains("更新阅读进度失败"));
    }

    @Test
    void testRecordReadingSession_Success() throws SQLException {
        Book book = new Book();
        book.setId(TEST_BOOK_ID);
        book.setTotalPages(200);
        
        when(bookDao.findById(TEST_BOOK_ID)).thenReturn(book);
        
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now();
        
        readingService.recordReadingSession(TEST_BOOK_ID, startTime, endTime, 50, 100);
        
        verify(sessionDao).create(any(ReadingSession.class));
    }

    @Test
    void testRecordReadingSession_DatabaseError() throws SQLException {
        doThrow(new SQLException("数据库连接失败"))
            .when(sessionDao).create(any(ReadingSession.class));

        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now();

        BookTrackerException exception = assertThrows(BookTrackerException.class, () ->
            readingService.recordReadingSession(TEST_BOOK_ID, startTime, endTime, 50, 100));
        assertTrue(exception.getMessage().contains("记录阅读会话失败"));
    }

    @Test
    void testCalculateAverageReadingSpeed_Success() throws SQLException {
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);
        LocalDateTime endTime = LocalDateTime.now().minusHours(1);
        
        ReadingSession session = new ReadingSession(TEST_BOOK_ID, TEST_USER_ID, startTime, endTime, 60);
        when(sessionDao.findByBookAndUser(TEST_BOOK_ID, TEST_USER_ID))
            .thenReturn(Arrays.asList(session));

        double speed = readingService.calculateAverageReadingSpeed(TEST_BOOK_ID);
        assertEquals(60.0, speed, 0.1); // 60 pages per hour
    }

    @Test
    void testCalculateAverageReadingSpeed_NoSessions() throws SQLException {
        when(sessionDao.findByBookAndUser(TEST_BOOK_ID, TEST_USER_ID))
            .thenReturn(Collections.emptyList());

        double speed = readingService.calculateAverageReadingSpeed(TEST_BOOK_ID);
        assertEquals(0.0, speed, 0.1);
    }

    @Test
    void testGetTotalPagesRead_Success() throws SQLException {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        
        ReadingSession session1 = new ReadingSession(TEST_BOOK_ID, TEST_USER_ID, 
            start.plusDays(1), start.plusDays(2), 50);
        ReadingSession session2 = new ReadingSession(TEST_BOOK_ID, TEST_USER_ID,
            start.plusDays(3), start.plusDays(4), 30);
            
        when(sessionDao.findByUserInTimeRange(eq(TEST_USER_ID), any(), any()))
            .thenReturn(Arrays.asList(session1, session2));

        int totalPages = readingService.getTotalPagesRead(start, end);
        assertEquals(80, totalPages);
    }

    @Test
    void testGetTotalReadingHours_Success() throws SQLException {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        
        ReadingSession session1 = new ReadingSession(TEST_BOOK_ID, TEST_USER_ID,
            start.plusHours(1), start.plusHours(3), 50); // 2 hours
        ReadingSession session2 = new ReadingSession(TEST_BOOK_ID, TEST_USER_ID,
            start.plusHours(5), start.plusHours(6), 30); // 1 hour
            
        when(sessionDao.findByUserInTimeRange(eq(TEST_USER_ID), any(), any()))
            .thenReturn(Arrays.asList(session1, session2));

        double totalHours = readingService.getTotalReadingHours(start, end);
        assertEquals(3.0, totalHours, 0.1);
    }

    @Test
    void testCalculateCompletionRate_Success() throws SQLException {
        ReadingProgress progress = new ReadingProgress(TEST_BOOK_ID, TEST_USER_ID, 75, 100);
        when(progressDao.findByBookAndUser(TEST_BOOK_ID, TEST_USER_ID)).thenReturn(progress);

        double completionRate = readingService.calculateCompletionRate(TEST_BOOK_ID);
        assertEquals(75.0, completionRate, 0.1);
    }

    @Test
    void testCalculateCompletionRate_NoProgress() throws SQLException {
        when(progressDao.findByBookAndUser(TEST_BOOK_ID, TEST_USER_ID)).thenReturn(null);

        double completionRate = readingService.calculateCompletionRate(TEST_BOOK_ID);
        assertEquals(0.0, completionRate, 0.1);
    }

    @Test
    void testCalculateCompletionRate_DatabaseError() throws SQLException {
        when(progressDao.findByBookAndUser(TEST_BOOK_ID, TEST_USER_ID))
            .thenThrow(new SQLException("数据库连接失败"));

        BookTrackerException exception = assertThrows(BookTrackerException.class, () ->
            readingService.calculateCompletionRate(TEST_BOOK_ID));
        assertTrue(exception.getMessage().contains("计算完成率失败"));
    }

    @Test
    void testGetTotalPagesRead_EmptyPeriod() throws SQLException {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        
        when(sessionDao.findByUserInTimeRange(eq(TEST_USER_ID), any(), any()))
            .thenReturn(Collections.emptyList());

        int totalPages = readingService.getTotalPagesRead(start, end);
        assertEquals(0, totalPages);
    }

    @Test
    void testGetTotalReadingHours_DatabaseError() throws SQLException {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        
        when(sessionDao.findByUserInTimeRange(eq(TEST_USER_ID), any(), any()))
            .thenThrow(new SQLException("数据库连接失败"));

        BookTrackerException exception = assertThrows(BookTrackerException.class, () ->
            readingService.getTotalReadingHours(start, end));
        assertTrue(exception.getMessage().contains("获取总阅读时间失败"));
    }
}
