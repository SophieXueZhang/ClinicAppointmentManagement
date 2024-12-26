package com.booktracker.dao;

import com.booktracker.model.Book;
import java.util.List;
import com.booktracker.exception.BookTrackerException;

/**
 * 图书数据访问接口
 */
public interface BookDao {
    /**
     * 设置用户名
     * @param username 用户名
     */
    void setUsername(String username);

    /**
     * 创建新的图书记录
     * @param book 要创建的图书对象
     * @throws BookTrackerException 如果创建过程中发生错误
     */
    void create(Book book) throws BookTrackerException;
    /**
     * 根据ID查找图书
     * @param id 图书ID
     * @return 图书对象，如果未找到返回null
     * @throws BookTrackerException 如果查询过程中发生错误
     */
    Book findById(Long id) throws BookTrackerException;
    /**
     * 查找所有图书
     * @return 图书列表
     * @throws BookTrackerException 如果查询过程中发生错误
     */
    List<Book> findAll() throws BookTrackerException;
    /**
     * 更新图书信息
     * @param book 要更新的图书对象
     * @throws BookTrackerException 如果更新过程中发生错误
     */
    void update(Book book) throws BookTrackerException;
    /**
     * 删除图书
     * @param id 要删除的图书ID
     * @throws BookTrackerException 如果删除过程中发生错误
     */
    void delete(Long id) throws BookTrackerException;
}
