module com.messangernbclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.messengerClient to javafx.fxml;
    exports com.messengerClient;
}