package com.booktracker.dao;

import com.booktracker.model.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDao {
    private final DatabaseManager dbManager;
    private String username = "default";

    public BookDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void create(Book book) throws SQLException {
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
        }
    }

    public Book findById(Long id) throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        }
        return null;
    }

    public List<Book> findAll() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM books ORDER BY date_added DESC";
        try (Connection conn = dbManager.getConnection(username);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        }
        return books;
    }

    public void update(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, total_pages = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setInt(3, book.getTotalPages());
            stmt.setLong(4, book.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = dbManager.getConnection(username);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
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
