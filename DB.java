package com.mycompany.healthinformationsystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    private static final String URL = "jdbc:mysql://localhost:3316/healthinfosystem";
    private static final String USER = "root";
    private static final String PASSWORD = "Isufst_123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}