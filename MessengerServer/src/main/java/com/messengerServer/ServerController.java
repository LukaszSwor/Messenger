package com.messengerServer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ServerController class is responsible for handling the server-side functionality
 * of the messenger application. It initializes the server, handles incoming and outgoing
 * messages, and deals with file transfer operations.
 */
public class ServerController implements Initializable {

    private static final int SERVER_PORT = 1234;
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
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    private MessageDisplayService messageDisplayService;
    private NetworkService networkService;
    private static final Logger logger = Logger.getLogger(ServerController.class.getName());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeServer();
        messageDisplayService = new MessageDisplayService(vbox_messages);
        vbox_messages.heightProperty().addListener((observableValue, number, t1) -> sp_main.setVvalue((Double) t1));
        button_send.setOnAction(this::handleSendMessage);
        button_send_file.setOnAction(this::handleSendFile);
        receiveMessageFromClient(vbox_messages);
    }

    private void initializeServer() {
        try {
            ServerSocket serverSocket = createServerSocket();
            acceptClientConnection(serverSocket);
            networkService = new NetworkService(outputStream, inputStream);
        } catch (IOException e) {
            handleServerError("Error creating server", e);
        }
    }

    private ServerSocket createServerSocket() throws IOException {
        logger.info("Server waiting for connection on port: " + ServerController.SERVER_PORT);
        return new ServerSocket(ServerController.SERVER_PORT);
    }

    private void acceptClientConnection(ServerSocket serverSocket) throws IOException {
        this.socket = serverSocket.accept();
        logger.info("Client connected on port: " + serverSocket.getLocalPort());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
        this.inputStream = new DataInputStream(socket.getInputStream());
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
        File fileToSend = chooseFile();
        if (fileToSend != null) {
            sendFileToClient(fileToSend);
        }
    }

    private File chooseFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file");
        return fileChooser.showOpenDialog(null);
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
            byte[] fileContentBytes = Files.readAllBytes(fileToSend.toPath());

            sendMessageToClient("FILE:");
            networkService.sendFileMetadata(fileNameBytes, fileContentBytes);
            networkService.sendFileContent(fileContentBytes);

            Platform.runLater(() -> messageDisplayService.addMessageToDisplay(fileToSend.getName(), true));
        } catch (IOException e) {
            handleServerError("Error sending file to the client", e);
        }
    }

    private void receiveMessageFromClient(VBox vBox) {
        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    String messageFromClient = inputStream.readUTF();
                    processReceivedMessage(messageFromClient, vBox);
                }
            } catch (SocketException e) {
                logger.log(Level.SEVERE, "Problem z gniazdem", e);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Błąd we/wy", e);
            }
        }).start();
    }

    private void processReceivedMessage(String message, VBox vBox) throws IOException {
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
            saveFile(fileName, fileContent);
            Platform.runLater(() -> messageDisplayService.addMessageToDisplay(fileName, false));
        } catch (IOException e) {
            handleServerError("Error receiving and saving file from client", e);
        }
    }

    private void saveFile(String fileName, byte[] fileContent) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            fileOutputStream.write(fileContent);
        }
    }

    private void handleServerError(String errorMessage, Exception exception) {
        System.out.println(errorMessage);
        logger.log(Level.SEVERE, errorMessage, exception);
        networkService.closeEverything(socket, outputStream, inputStream);
    }
}