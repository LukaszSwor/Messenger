package com.messengerServer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.io.*;
import java.net.*;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ServerController class is responsible for handling the server-side functionality
 * of the messenger application. It initializes the server, handles incoming and outgoing
 * messages, and deals with file transfer operations.
 */
public class ServerController implements Initializable {

    @FXML
    private Button button_send_file;
    @FXML
    private Button button_send;
    @FXML
    private TextField tf_message;
    @FXML
    private VBox vbox_messages;
    @FXML
    private ScrollPane sp_main;
    private MessageDisplayService messageDisplayService;
    private NetworkService networkService;
    private ConnectionManager connectionManager;
    private FileService fileService;
    private static final Logger logger = Logger.getLogger(ServerController.class.getName());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connectionManager = new ConnectionManager();
        try {
            connectionManager.initializeServer();
            networkService = connectionManager.getNetworkService();
            messageDisplayService = new MessageDisplayService(vbox_messages);
            vbox_messages.heightProperty().addListener((observableValue, number, t1) -> sp_main.setVvalue((Double) t1));
            button_send.setOnAction(this::handleSendMessage);
            button_send_file.setOnAction(this::handleSendFile);
            this.fileService = new FileService();
            receiveMessageFromClient();
        } catch (IOException e) {
            handleServerError("Error creating server", e);
        }
    }

    private void handleSendMessage(ActionEvent actionEvent) {
        String messageToSend = tf_message.getText();
        if (!messageToSend.isEmpty()) {
            processAndSendMessage(messageToSend);
        }
    }

    private void processAndSendMessage(String message) {
        messageDisplayService.addMessageToDisplay(message, true);
        sendMessageToClient(message);
        tf_message.clear();
    }

    private void handleSendFile(ActionEvent actionEvent) {
        File fileToSend = fileService.chooseFile();
        if (fileToSend != null) {
            sendFileToClient(fileToSend);
        }
    }

    private void sendMessageToClient(String messageToSend) {
        try {
            networkService.sendMessage(messageToSend);
            tf_message.clear();
        } catch (IOException e) {
            handleServerError("Error sending message to the client", e);
        }
    }

    private void sendFileToClient(File fileToSend) {
        try {
            byte[] fileNameBytes = fileToSend.getName().getBytes();
            byte[] fileContentBytes = fileService.readFileContent(fileToSend);

            sendMessageToClient("FILE:");
            networkService.sendFileMetadata(fileNameBytes, fileContentBytes);
            networkService.sendFileContent(fileContentBytes);

            Platform.runLater(() -> messageDisplayService.addMessageToDisplay(fileToSend.getName(), true));
        } catch (IOException e) {
            handleServerError("Error sending file to the client", e);
        }
    }

    private void receiveMessageFromClient() {
        new Thread(() -> {
            try {
                while (true) {
                    if (connectionManager.isConnected()) {
                        String messageFromClient = networkService.readMessage();
                        processReceivedMessage(messageFromClient);
                    }
                }
            } catch (SocketException e) {
                logger.log(Level.SEVERE, "Problem with socket", e);
            } catch (IOException e) {
                logger.log(Level.SEVERE,"Input/output device error", e);
            }
        }).start();
    }

    private void processReceivedMessage(String message) throws IOException {
        if (message.equals("FILE:")) {
            receiveAndSaveFile();
        } else {
            Platform.runLater(() -> messageDisplayService.addMessageToDisplay(message, false));
        }
    }

    private void receiveAndSaveFile() {
        try {
            String fileName = networkService.readFileNameFromClient();
            byte[] fileContent = networkService.readFileContentFromClient();
            fileService.saveFile(fileName, fileContent);
            Platform.runLater(() -> messageDisplayService.addMessageToDisplay(fileName, false));
        } catch (IOException e) {
            handleServerError("Error receiving and saving file from client", e);
        }
    }

    private void handleServerError(String errorMessage, Exception exception) {
        System.out.println(errorMessage);
        logger.log(Level.SEVERE, errorMessage, exception);
        connectionManager.closeConnection();
    }
}