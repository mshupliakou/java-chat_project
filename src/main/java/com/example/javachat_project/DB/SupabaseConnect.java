package com.example.javachat_project.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class SupabaseConnect {
    private static Connection connection;


    public static Connection getConnection() {
        if (connection == null) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream("config.properties")) {
                props.load(in);
            } catch (IOException e) {
                System.out.println("config.properties is not founded");
                e.printStackTrace();
                return null;
            }

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            try {
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Success db connection with supabase");
            } catch (SQLException e) {
                System.out.println("something wrong db connection with supabase");
                e.printStackTrace();
                return null;
            }
        }
        return connection;
    }
}
