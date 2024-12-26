package com.booktracker;

import javax.swing.SwingUtilities;
import com.booktracker.ui.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 图书阅读进度跟踪应用程序的主入口类
 * 
 * 该类负责初始化和启动整个应用程序，包括：
 * - 设置系统外观和感觉
 * - 创建并显示主窗口
 * - 处理启动过程中的异常
 * 
 * @author Devin AI
 * @version 1.0
 */
public class BookTrackerApplication {
    private static final Logger logger = LoggerFactory.getLogger(BookTrackerApplication.class);
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName()
                );
                new MainWindow().setVisible(true);
            } catch (Exception e) {
                logger.error("应用程序启动失败", e);
            }
        });
    }
}
