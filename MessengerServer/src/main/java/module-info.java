module com.messangernbserver {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.messengerServer to javafx.fxml;
    exports com.messengerServer;
}