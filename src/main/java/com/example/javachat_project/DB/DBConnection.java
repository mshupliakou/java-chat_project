package com.example.javachat_project.DB;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    public static Connection dbConnection;
    public static Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Properties props = new Properties();
        try(InputStream in = Files.newInputStream(Paths.get("src/main/resources/database.properties"))){
            props.load(in);
            String url = props.getProperty("url");
            dbConnection = DriverManager.getConnection(url);
            System.out.println("DB connection is successful");
        }catch (IOException | SQLException e){e.printStackTrace();}


        return dbConnection;
    }

}
