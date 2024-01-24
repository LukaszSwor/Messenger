module com.messangernbserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;


    opens com.messengerServer to javafx.fxml;
    exports com.messengerServer;
}