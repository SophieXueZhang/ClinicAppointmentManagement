package com.booktracker.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.booktracker.exception.BookTrackerException;

/**
 * 数据库管理器类，负责处理所有数据库连接和初始化操作
 * 
 * 该类实现了单例模式，主要功能包括：
 * - 管理数据库连接池
 * - 为每个用户创建独立的数据库实例
 * - 处理数据库初始化和模式创建
 * - 管理数据源生命周期
 * 
 * 使用HikariCP连接池来优化数据库连接性能
 * 
 * @author Devin AI
 * @version 1.0
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;
    private final Map<String, DataSource> dataSources;
    private static final String BASE_PATH = "data/users";
    private static final String SCHEMA_FILE = "schema.sql";

    private DatabaseManager() {
        dataSources = new HashMap<>();
        createBaseDirectory();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void createBaseDirectory() {
        try {
            Files.createDirectories(Paths.get(BASE_PATH));
        } catch (Exception e) {
            logger.error("创建基础目录失败", e);
            throw new BookTrackerException("创建基础目录失败: 请确保应用程序具有写入权限", e);
        }
    }

    public Connection getConnection(String username) throws SQLException {
        DataSource ds = getOrCreateDataSource(username);
        return ds.getConnection();
    }

    private synchronized DataSource getOrCreateDataSource(String username) {
        return dataSources.computeIfAbsent(username, this::createDataSource);
    }

    private DataSource createDataSource(String username) {
        try {
            String dbPath = getDbPath(username);
            Files.createDirectories(Paths.get(dbPath).getParent());

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:h2:file:" + dbPath);
            config.setUsername("sa");
            config.setPassword("");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(20000);
            config.setAutoCommit(false);

            HikariDataSource ds = new HikariDataSource(config);
            initializeDatabase(ds);
            return ds;
        } catch (Exception e) {
            logger.error("为用户创建数据源失败: {}", username, e);
            throw new BookTrackerException("为用户'" + username + "'创建数据源失败: 请检查数据库配置和权限", e);
        }
    }

    private String getDbPath(String username) {
        Path path = Paths.get(System.getProperty("user.dir"), BASE_PATH, 
            username.toLowerCase().replaceAll("[^a-z0-9]", "_"), "booktracker");
        return path.toAbsolutePath().toString();
    }

    private void initializeDatabase(DataSource ds) {
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Execute schema creation in a single transaction
                String schemaContent = new String(
                    getClass().getClassLoader().getResourceAsStream(SCHEMA_FILE).readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
                );
                
                // Split into individual statements and execute each one
                for (String statement : schemaContent.split(";")) {
                    statement = statement.trim();
                    if (!statement.isEmpty()) {
                        try {
                            conn.createStatement().execute(statement);
                        } catch (SQLException e) {
                            // Ignore if objects already exist
                            if (!e.getMessage().contains("already exists")) {
                                throw e;
                            }
                        }
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            logger.error("初始化数据库失败", e);
            throw new BookTrackerException("初始化数据库失败: 请检查数据库架构文件和权限", e);
        }
    }

    public void closeDataSource(String username) {
        DataSource ds = dataSources.remove(username);
        if (ds instanceof HikariDataSource) {
            ((HikariDataSource) ds).close();
        }
    }
}
