package com.booktracker.dao;

import com.booktracker.model.ReadingProgress;
import java.util.List;
import com.booktracker.exception.BookTrackerException;

/**
 * 阅读进度数据访问接口
 */
public interface ReadingProgressDao {
    /**
     * 设置用户名
     * @param username 用户名
     */
    void setUsername(String username);

    /**
     * 创建新的阅读进度记录
     * @param progress 要创建的阅读进度对象
     * @throws BookTrackerException 如果创建过程中发生错误
     */
    void create(ReadingProgress progress) throws BookTrackerException;

    /**
     * 根据图书ID和用户ID查找阅读进度
     * @param bookId 图书ID
     * @param userId 用户ID
     * @return 阅读进度对象，如果未找到返回null
     * @throws BookTrackerException 如果查询过程中发生错误
     */
    ReadingProgress findByBookAndUser(Long bookId, Long userId) throws BookTrackerException;
    /**
     * 查找用户的所有阅读进度
     * @param userId 用户ID
     * @return 阅读进度列表
     * @throws BookTrackerException 如果查询过程中发生错误
     */
    List<ReadingProgress> findByUser(Long userId) throws BookTrackerException;
    /**
     * 更新阅读进度
     * @param progress 要更新的阅读进度对象
     * @throws BookTrackerException 如果更新过程中发生错误
     */
    void update(ReadingProgress progress) throws BookTrackerException;
}
