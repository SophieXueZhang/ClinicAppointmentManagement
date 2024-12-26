package com.booktracker.dao.impl;

import com.booktracker.dao.BookDao;
import com.booktracker.dao.DatabaseManager;
import com.booktracker.exception.BookTrackerException;
import com.booktracker.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BookDao接口的实现类
 */
public class BookDaoImpl implements BookDao {
    private final DatabaseManager dbManager;
    private String username = "default";

    public BookDaoImpl() {
        this.dbManager = DatabaseManager.getInstance();
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void create(Book book) throws BookTrackerException {
        String sql = "INSERT INTO books (title, author, total_pages, date_added) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setInt(3, book.getTotalPages());
            stmt.setTimestamp(4, Timestamp.valueOf(book.getDateAdded()));
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    book.setId(rs.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new BookTrackerException("创建图书记录失败", e);
        }
    }

    @Override
    public Book findById(Long id) throws BookTrackerException {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        } catch (SQLException e) {
            throw new BookTrackerException("查询图书失败", e);
        }
        return null;
    }

    @Override
    public List<Book> findAll() throws BookTrackerException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY date_added DESC";
        try (Connection conn = dbManager.getConnection(username);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            throw new BookTrackerException("查询所有图书失败", e);
        }
        return books;
    }

    @Override
    public void update(Book book) throws BookTrackerException {
        String sql = "UPDATE books SET title = ?, author = ?, total_pages = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setInt(3, book.getTotalPages());
            stmt.setLong(4, book.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new BookTrackerException("更新图书信息失败", e);
        }
    }

    @Override
    public void delete(Long id) throws BookTrackerException {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new BookTrackerException("删除图书失败", e);
        }
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setTotalPages(rs.getInt("total_pages"));
        book.setDateAdded(rs.getTimestamp("date_added").toLocalDateTime());
        return book;
    }
}
