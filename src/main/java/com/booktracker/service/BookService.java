package com.booktracker.service;

import com.booktracker.dao.BookDao;
import com.booktracker.model.Book;

import java.sql.SQLException;
import java.util.List;

public class BookService {
    private final BookDao bookDao;

    public BookService() {
        this.bookDao = new BookDao();
    }

    public void addBook(String title, String author, int totalPages) throws SQLException {
        Book book = new Book(title, author, totalPages);
        bookDao.create(book);
    }

    public void updateBook(Long id, String title, String author, int totalPages) throws SQLException {
        Book book = bookDao.findById(id);
        if (book != null) {
            book.setTitle(title);
            book.setAuthor(author);
            book.setTotalPages(totalPages);
            bookDao.update(book);
        }
    }

    public void deleteBook(Long id) throws SQLException {
        bookDao.delete(id);
    }

    public Book getBook(Long id) throws SQLException {
        return bookDao.findById(id);
    }

    public List<Book> getAllBooks() throws SQLException {
        return bookDao.findAll();
    }
}
