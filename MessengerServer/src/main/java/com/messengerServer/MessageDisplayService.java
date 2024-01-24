package com.messengerServer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.application.Platform;

public class MessageDisplayService {

    private final VBox messageContainer;

    public MessageDisplayService(VBox messageContainer) {
        this.messageContainer = messageContainer;
    }

    public void addMessageToDisplay(String message, boolean isSender) {
        HBox hBox = new HBox();
        hBox.setAlignment(isSender ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text);

        if (isSender) {
            textFlow.setStyle("-fx-background-color: rgb(15,125,242);" +
                    "-fx-background-radius: 20px;");
            text.setFill(Color.color(0.934, 0.945, 0.996));
        } else {
            textFlow.setStyle("-fx-background-color: rgb(233,233,235);" +
                    "-fx-background-radius: 20px;");
        }

        textFlow.setPadding(new Insets(5, 10, 5, 10));
        hBox.getChildren().add(textFlow);

        Platform.runLater(() -> messageContainer.getChildren().add(hBox));
    }
}
