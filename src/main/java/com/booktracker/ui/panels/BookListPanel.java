package com.booktracker.ui.panels;

import com.booktracker.model.Book;
import com.booktracker.model.ReadingProgress;
import com.booktracker.service.BookService;
import com.booktracker.service.ReadingService;
import com.booktracker.ui.dialogs.BookDialog;
import com.booktracker.ui.dialogs.ReadingSessionDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class BookListPanel extends JPanel {
    private void showAddBookDialog() {
        BookDialog dialog = new BookDialog((JFrame) SwingUtilities.getWindowAncestor(this), "添加新书");
        dialog.setVisible(true);
        if (dialog.isApproved()) {
            try {
                bookService.addBook(
                    dialog.getBookTitle(),
                    dialog.getAuthor(),
                    dialog.getTotalPages()
                );
                refreshBookList();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "添加图书失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showEditBookDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一本书", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Book book = (Book) bookTable.getValueAt(selectedRow, 5);
            BookDialog dialog = new BookDialog((JFrame) SwingUtilities.getWindowAncestor(this), "编辑图书");
            dialog.setBookTitle(book.getTitle());
            dialog.setAuthor(book.getAuthor());
            dialog.setTotalPages(book.getTotalPages());
            dialog.setVisible(true);

            if (dialog.isApproved()) {
                bookService.updateBook(
                    book.getId(),
                    dialog.getBookTitle(),
                    dialog.getAuthor(),
                    dialog.getTotalPages()
                );
                refreshBookList();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "编辑图书失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一本书", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
            "确定要删除这本书吗？", "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            try {
                Book book = (Book) bookTable.getValueAt(selectedRow, 5);
                bookService.deleteBook(book.getId());
                refreshBookList();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "删除图书失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showReadingSessionDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一本书", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            Book book = (Book) bookTable.getValueAt(selectedRow, 5);
            ReadingProgress progress = readingService.getReadingProgress(book.getId());
            int currentPage = progress != null ? progress.getCurrentPage() : 0;

            ReadingSessionDialog dialog = new ReadingSessionDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                book.getTitle(),
                book.getTotalPages(),
                currentPage
            );
            dialog.setVisible(true);

            if (dialog.isApproved()) {
                readingService.recordReadingSession(
                    book.getId(),
                    dialog.getStartTime(),
                    dialog.getEndTime(),
                    currentPage,
                    dialog.getCurrentPage()
                );
                refreshBookList();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "更新阅读进度失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshBookList() {
        try {
            BookService bookService = new BookService();
            ReadingService readingService = new ReadingService();
            readingService.setCurrentUser(1L); // TODO: Get from current user session

            List<Book> books = bookService.getAllBooks();
            DefaultTableModel model = (DefaultTableModel) bookTable.getModel();
            model.setRowCount(0);

            for (Book book : books) {
                ReadingProgress progress = readingService.getReadingProgress(book.getId());
                int currentPage = progress != null ? progress.getCurrentPage() : 0;
                double progressPercent = progress != null ? progress.getProgressPercentage() : 0.0;
                
                model.addRow(new Object[]{
                    book.getTitle(),
                    book.getAuthor(),
                    book.getTotalPages(),
                    currentPage,
                    String.format("%.1f%%", progressPercent)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "刷新图书列表失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    private JTable bookTable;
    private JScrollPane scrollPane;
    private final BookService bookService;
    private final ReadingService readingService;
    private Long currentUserId = 1L; // TODO: Get from user session
    private final String[] columnNames = {"书名", "作者", "总页数", "当前页数", "阅读进度", "图书对象"};
    private final Class<?>[] columnTypes = {
        String.class, String.class, Integer.class, Integer.class, String.class, Book.class
    };

    public BookListPanel() {
        setLayout(new BorderLayout());
        this.bookService = new BookService();
        this.readingService = new ReadingService();
        readingService.setCurrentUser(currentUserId);
        initializeComponents();
        refreshBookList();
    }

    public void setCurrentUser(Long userId) {
        this.currentUserId = userId;
        readingService.setCurrentUser(userId);
        refreshBookList();
    }

    private void initializeComponents() {
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        bookTable = new JTable(model);
        // Hide the Book object column
        bookTable.getColumnModel().getColumn(5).setMinWidth(0);
        bookTable.getColumnModel().getColumn(5).setMaxWidth(0);
        bookTable.getColumnModel().getColumn(5).setWidth(0);
        scrollPane = new JScrollPane(bookTable);
        
        // Add toolbar with buttons
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton addButton = new JButton("添加图书");
        JButton editButton = new JButton("编辑");
        JButton deleteButton = new JButton("删除");
        JButton updateProgressButton = new JButton("更新进度");
        
        addButton.addActionListener(e -> showAddBookDialog());
        editButton.addActionListener(e -> showEditBookDialog());
        deleteButton.addActionListener(e -> deleteSelectedBook());
        updateProgressButton.addActionListener(e -> showReadingSessionDialog());
        
        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(updateProgressButton);
        
        // Add components to panel
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
}
