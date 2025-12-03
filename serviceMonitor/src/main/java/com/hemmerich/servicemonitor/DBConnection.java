package com.hemmerich.servicemonitor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database Config
    private static final String URL = "jdbc:mysql://localhost:3306/homelab_monitor?useSSL=false&serverTimezone=UTC";
    private static final String USER = "monitor_user";
    private static final String PASS = "HomelabSecure123!";

    public static Connection getConnection() throws SQLException {
        try {
            // Load the driver explicitly to ensure Tomcat finds it
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }
}