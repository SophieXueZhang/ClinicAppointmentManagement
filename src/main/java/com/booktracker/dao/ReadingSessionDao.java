package com.booktracker.dao;

import com.booktracker.model.ReadingSession;
import java.time.LocalDateTime;
import java.util.List;
import com.booktracker.exception.BookTrackerException;

/**
 * 阅读会话数据访问接口
 */
public interface ReadingSessionDao {
    /**
     * 设置用户名
     * @param username 用户名
     */
    void setUsername(String username);

    /**
     * 创建新的阅读会话记录
     * @param session 要创建的阅读会话对象
     * @throws BookTrackerException 如果创建过程中发生错误
     */
    void create(ReadingSession session) throws BookTrackerException;

    /**
     * 查找指定图书和用户的所有阅读会话
     * @param bookId 图书ID
     * @param userId 用户ID
     * @return 阅读会话列表
     * @throws BookTrackerException 如果查询过程中发生错误
     */
    List<ReadingSession> findByBookAndUser(Long bookId, Long userId) throws BookTrackerException;
    /**
     * 查找指定用户在时间范围内的所有阅读会话
     * @param userId 用户ID
     * @param start 开始时间
     * @param end 结束时间
     * @return 阅读会话列表
     * @throws BookTrackerException 如果查询过程中发生错误
     */
    List<ReadingSession> findByUserInTimeRange(Long userId, LocalDateTime start, LocalDateTime end) 
            throws BookTrackerException;
}
