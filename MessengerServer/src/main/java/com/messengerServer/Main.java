package com.messengerServer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The main class for the server application which launches the JavaFX application.
 */
public class Main extends Application {

    /**
     * @param stage The primary stage for this application, onto which the application scene can be set.
     *              The primary stage will be embedded in the browser if the application was launched as an applet.
     *              Applications may create other stages if needed.
     * @throws IOException If the fxml file fails to load.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 400);
        stage.setTitle("Server");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}