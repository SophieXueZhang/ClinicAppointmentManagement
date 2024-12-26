package com.booktracker.ui;

import com.booktracker.ui.panels.BookListPanel;
import com.booktracker.ui.panels.StatisticsPanel;
import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private JMenuBar menuBar;
    private BookListPanel bookListPanel;
    private StatisticsPanel statisticsPanel;

    public MainWindow() {
        setTitle("图书阅读进度跟踪系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        
        initializeComponents();
        createMenuBar();
    }
    
    private void initializeComponents() {
        // Create main container with split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // Initialize panels
        bookListPanel = new BookListPanel();
        statisticsPanel = new StatisticsPanel();
        
        // Add panels to split pane
        splitPane.setLeftComponent(bookListPanel);
        splitPane.setRightComponent(statisticsPanel);
        
        // Set split pane properties
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(700);
        
        // Set as content pane
        setContentPane(splitPane);
    }
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("文件");
        fileMenu.add(new JMenuItem("切换用户"));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("退出"));
        
        // Library Menu
        JMenu libraryMenu = new JMenu("图书库");
        libraryMenu.add(new JMenuItem("添加新书"));
        libraryMenu.add(new JMenuItem("导入图书"));
        libraryMenu.add(new JMenuItem("导出图书清单"));
        
        // Progress Menu
        JMenu progressMenu = new JMenu("阅读进度");
        progressMenu.add(new JMenuItem("开始阅读会话"));
        progressMenu.add(new JMenuItem("更新阅读进度"));
        progressMenu.add(new JMenuItem("查看统计信息"));
        
        // Help Menu
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.add(new JMenuItem("使用说明"));
        helpMenu.add(new JMenuItem("关于"));
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(libraryMenu);
        menuBar.add(progressMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
}
