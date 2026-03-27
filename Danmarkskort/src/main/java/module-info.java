module org.example.danmarkskort {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.danmarkskort to javafx.fxml;
    exports org.example.danmarkskort;
}