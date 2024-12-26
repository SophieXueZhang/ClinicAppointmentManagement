package com.booktracker.ui;

import com.booktracker.ui.panels.BookListPanel;
import com.booktracker.ui.panels.StatisticsPanel;
import javax.swing.*;
import java.awt.*;

/**
 * 图书阅读进度跟踪系统的主窗口类
 * 
 * 该类负责创建和管理应用程序的主要图形界面，包括：
 * - 分割面板布局，左侧显示图书列表，右侧显示统计信息
 * - 主菜单栏，提供文件、图书库、阅读进度和帮助等功能
 * - 用户界面组件的初始化和布局管理
 * 
 * 窗口布局采用JSplitPane进行左右分割，默认比例为7:3
 * 
 * @author Devin AI
 * @version 1.0
 * @see BookListPanel
 * @see StatisticsPanel
 */
public class MainWindow extends JFrame {
    private JMenuBar menuBar;
    private BookListPanel bookListPanel;
    private StatisticsPanel statisticsPanel;

    /**
     * 创建并初始化主窗口
     * 
     * 设置窗口的基本属性，包括标题、关闭操作、大小和位置
     * 初始化所有UI组件并创建菜单栏
     */
    public MainWindow() {
        setTitle("图书阅读进度跟踪系统");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setLocationRelativeTo(null);
        
        initializeComponents();
        createMenuBar();
    }
    
    /**
     * 初始化主窗口的组件
     * 
     * 创建分割面板并添加图书列表和统计面板
     * 设置面板的分割比例为7:3
     */
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
    
    /**
     * 创建并初始化菜单栏
     * 
     * 创建以下菜单项：
     * - 文件菜单：包含用户切换和退出功能
     * - 图书库菜单：包含图书管理相关功能
     * - 阅读进度菜单：包含阅读会话和统计功能
     * - 帮助菜单：包含使用说明和关于信息
     */
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
