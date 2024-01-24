module com.messangernbclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;


    opens com.messengerClient to javafx.fxml;
    exports com.messengerClient;
}