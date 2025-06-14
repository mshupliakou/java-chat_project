module com.example.javachat_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens com.example.javachat_project to javafx.fxml;
    exports com.example.javachat_project;
}
