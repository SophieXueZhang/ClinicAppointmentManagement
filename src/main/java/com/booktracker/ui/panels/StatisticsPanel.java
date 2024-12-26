package com.booktracker.ui.panels;

import com.booktracker.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.booktracker.service.BookService;
import com.booktracker.service.ReadingService;
import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计信息面板类，显示用户的阅读统计数据
 * 
 * 该面板提供以下功能：
 * - 显示总阅读时间
 * - 统计已读页数
 * - 计算已完成图书数量
 * - 分析平均阅读速度
 * - 计算图书完成率
 * - 统计每日平均阅读时长
 * 
 * 支持按不同时间范围（日、周、月、全部）查看统计数据
 * 所有数据都可以实时刷新
 * 
 * @author Devin AI
 * @version 1.0
 * @see BookService
 * @see ReadingService
 */
public class StatisticsPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsPanel.class);
    private JPanel statsContainer;
    private final ReadingService readingService;
    private final BookService bookService;
    private Long currentUserId = 1L; // TODO: Get from user session
    private final Map<String, JLabel> statLabels;
    private JComboBox<String> timeRangeCombo;
    
    /**
     * 创建并初始化统计信息面板
     * 
     * 初始化过程包括：
     * - 设置面板布局为BorderLayout
     * - 创建服务层实例
     * - 初始化统计标签映射
     * - 设置当前用户
     * - 初始化UI组件
     * - 刷新统计数据
     */
    public StatisticsPanel() {
        setLayout(new BorderLayout());
        this.readingService = new ReadingService();
        this.bookService = new BookService();
        this.statLabels = new HashMap<>();
        readingService.setCurrentUser(currentUserId);
        initializeComponents();
        refreshStatistics();
    }
    
    public void setCurrentUser(Long userId) {
        this.currentUserId = userId;
        readingService.setCurrentUser(userId);
        refreshStatistics();
    }
    
    private void initializeComponents() {
        // Create statistics container
        statsContainer = new JPanel(new GridLayout(6, 1, 10, 10));
        statsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add statistics components
        addStatisticComponent("总阅读时间", "0小时");
        addStatisticComponent("已读页数", "0页");
        addStatisticComponent("已完成图书", "0本");
        addStatisticComponent("平均阅读速度", "0页/小时");
        addStatisticComponent("完成率", "0%");
        addStatisticComponent("平均每日阅读时长", "0小时");
        
        // Add to main panel with scroll support
        JScrollPane scrollPane = new JScrollPane(statsContainer);
        add(scrollPane, BorderLayout.CENTER);
        
        // Add time range selection
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] timeRanges = {"本日", "本周", "本月", "全部"};
        timeRangeCombo = new JComboBox<>(timeRanges);
        timeRangeCombo.addActionListener(e -> refreshStatistics());
        controlPanel.add(new JLabel("时间范围："));
        controlPanel.add(timeRangeCombo);
        
        // Add refresh button
        JButton refreshButton = new JButton("刷新统计");
        refreshButton.addActionListener(e -> refreshStatistics());
        controlPanel.add(refreshButton);
        
        add(controlPanel, BorderLayout.SOUTH);
    }
    
    private void addStatisticComponent(String label, String value) {
        JPanel statPanel = new JPanel(new BorderLayout());
        statPanel.setBorder(BorderFactory.createTitledBorder(label));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font(valueLabel.getFont().getName(), Font.BOLD, 16));
        
        statPanel.add(valueLabel, BorderLayout.CENTER);
        statsContainer.add(statPanel);
        statLabels.put(label, valueLabel);
    }
    
    private void refreshStatistics() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime;
            
            switch (timeRangeCombo.getSelectedIndex()) {
                case 0: // 本日
                    startTime = now.withHour(0).withMinute(0).withSecond(0);
                    break;
                case 1: // 本周
                    startTime = now.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                                 .withHour(0).withMinute(0).withSecond(0);
                    break;
                case 2: // 本月
                    startTime = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
                    break;
                default: // 全部
                    startTime = LocalDateTime.of(2000, 1, 1, 0, 0);
            }
            
            // Calculate statistics
            double totalHours = readingService.getTotalReadingHours(startTime, now);
            int totalPages = readingService.getTotalPagesRead(startTime, now);
            
            List<Book> books = bookService.getAllBooks();
            int completedBooks = 0;
            double totalSpeed = 0;
            int booksWithSpeed = 0;
            
            for (Book book : books) {
                double completion = readingService.calculateCompletionRate(book.getId());
                if (completion >= 100) {
                    completedBooks++;
                }
                
                double speed = readingService.calculateAverageReadingSpeed(book.getId());
                if (speed > 0) {
                    totalSpeed += speed;
                    booksWithSpeed++;
                }
            }
            
            double averageSpeed = booksWithSpeed > 0 ? totalSpeed / booksWithSpeed : 0;
            
            // Update statistics display
            updateStatLabel("总阅读时间", String.format("%.1f小时", totalHours));
            updateStatLabel("已读页数", totalPages + "页");
            updateStatLabel("已完成图书", completedBooks + "本");
            updateStatLabel("平均阅读速度", String.format("%.1f页/小时", averageSpeed));
            updateStatLabel("完成率", String.format("%.1f%%", 
                books.isEmpty() ? 0 : (completedBooks * 100.0 / books.size())));
            updateStatLabel("平均每日阅读时长", String.format("%.1f小时", 
                totalHours / Math.max(1, java.time.Duration.between(startTime, now).toDays())));
            
        } catch (SQLException e) {
            logger.error("获取统计数据失败", e);
            JOptionPane.showMessageDialog(this,
                "获取统计数据失败: " + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStatLabel(String label, String value) {
        JLabel valueLabel = statLabels.get(label);
        if (valueLabel != null) {
            valueLabel.setText(value);
        }
    }
}
