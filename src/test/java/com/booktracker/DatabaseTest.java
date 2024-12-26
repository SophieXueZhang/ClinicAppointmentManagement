package com.booktracker;

import com.booktracker.dao.DatabaseManager;
import java.sql.Connection;

public class DatabaseTest {
    public static void main(String[] args) {
        try {
            // This will trigger both database creation and logging
            DatabaseManager manager = DatabaseManager.getInstance();
            Connection conn = manager.getConnection("testuser");
            System.out.println("Successfully connected to database");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
