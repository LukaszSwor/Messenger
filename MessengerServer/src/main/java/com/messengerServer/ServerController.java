package com.messengerServer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.ResourceBundle;

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
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;

    /**
     * @param url            The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeServer();

        vbox_messages.heightProperty().addListener((observableValue, number, t1) -> sp_main.setVvalue((Double) t1));
        button_send.setOnAction(this::handleSendMessage);
        button_send_file.setOnAction(this::handleSendFile);
        receiveMessageFromClient(vbox_messages);
    }

    private void initializeServer() {
        int serverPort = 1234;
        System.out.println("Server waiting for connection on port: " + serverPort);
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            this.socket = serverSocket.accept();
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.inputStream = new DataInputStream(socket.getInputStream());
            System.out.println("Client connected on port: " + serverPort);
        } catch (IOException e) {
            handleServerError("Error creating server", e);
        }
    }

    /**
     * @param actionEvent The event that triggered the send action.
     */
    private void handleSendMessage(ActionEvent actionEvent) {
        String messageToSend = tf_message.getText();
        if (!messageToSend.isEmpty()) {
            messageViewSetUp(messageToSend);
            sendMessageToClient(messageToSend);
            tf_message.clear();
        }
    }

    /**
     * @param actionEvent The event that triggered the send file action.
     */
    private void handleSendFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file");
        File fileToSend = fileChooser.showOpenDialog(null);

        if (fileToSend != null) {
            sendFileToClient(fileToSend);
        }
    }

    /**
     * @param errorMessage The error message to be logged to the console.
     * @param exception    The exception that triggered the error handling.
     */
    private void handleServerError(String errorMessage, Exception exception) {
        System.out.println(errorMessage);
        exception.printStackTrace();
        closeEverything(socket, outputStream, inputStream);
    }

    /**
     * @param vBox The VBox container where the messages from the client will be displayed.
     */
    private void receiveMessageFromClient(VBox vBox) {
        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    String messageFromClient = inputStream.readUTF();
                    if (messageFromClient.equals("FILE:")) {
                        receiveAndSaveFile();
                    } else {
                        addLabel(messageFromClient, vBox);
                    }
                }
            } catch (IOException e) {
                handleServerError("Error receiving message from Client", e);
            }
        }).start();
    }

    /**
     * @throws IOException If an I/O error occurs while receiving the file.
     */
    private void receiveAndSaveFile() throws IOException {
        int fileNameLength = inputStream.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        inputStream.readFully(fileNameBytes);
        String fileName = new String(fileNameBytes);

        int fileContentLength = inputStream.readInt();
        byte[] fileContentBytes = new byte[fileContentLength];
        inputStream.readFully(fileContentBytes);

        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
        fileOutputStream.write(fileContentBytes);
        fileOutputStream.close();

        System.out.println("Received and saved file: " + fileName);
        addLabel(fileName, vbox_messages);
    }

    /**
     * @param messageToSend The message to be sent to the client.
     */
    private void sendMessageToClient(String messageToSend) {
        try {
            outputStream.writeUTF(messageToSend);
            outputStream.flush();
            tf_message.clear();
        } catch (IOException e) {
            handleServerError("Error sending message to the client", e);
        }
    }

    /**
     * @param fileToSend The file that needs to be sent to the client.
     */
    private void sendFileToClient(File fileToSend) {
        try {
            File imageFile = new File(fileToSend.getAbsolutePath());
            String fileName = fileToSend.getName();
            byte[] fileNameBytes = fileName.getBytes();
            byte[] fileBytes = Files.readAllBytes(imageFile.toPath());
            outputStream.writeUTF("FILE:");
            outputStream.writeInt(fileName.length());
            outputStream.write(fileNameBytes);
            outputStream.writeInt(fileBytes.length);
            outputStream.write(fileBytes);
            outputStream.flush();
            System.out.println(fileName);
            messageViewSetUp(fileName);
        } catch (IOException e) {
            handleServerError("Error sending File to the client", e);
        }
    }

    /**
     * @param messageToSend The message text to be displayed in the server's chat UI.
     */
    private void messageViewSetUp(String messageToSend) {
        if (!messageToSend.isEmpty()) {
            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);

            hBox.setPadding(new Insets(5, 5, 5, 10));
            Text text = new Text(messageToSend);
            TextFlow textFlow = new TextFlow(text);
            textFlow.setStyle("-fx-color: rgb(239,242,255); " +
                    "-fx-background-color: rgb(15,125,242);" +
                    "-fx-background-radius: 20px;");
            textFlow.setPadding(new Insets(5, 10, 5, 10));
            text.setFill(Color.color(0.934, 0.945, 0.996));

            hBox.getChildren().add(textFlow);
            vbox_messages.getChildren().add(hBox);
        }
    }

    /**
     * @param messageFromClient The message text received from the client.
     * @param vbox The VBox container where the messages will be displayed.
     */
    private static void addLabel(String messageFromClient, VBox vbox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(messageFromClient);
        TextFlow textFlow = new TextFlow(text);
        textFlow.setStyle("-fx-background-color: rgb(233,233,235);" +
                "-fx-background-radius: 20px;");
        textFlow.setPadding(new Insets(5, 10, 5, 10));
        hBox.getChildren().add(textFlow);

        Platform.runLater(() -> vbox.getChildren().add(hBox));
    }

    /**
     * @param socket The socket to be closed.
     * @param outputStream The output stream to be closed.
     * @param inputStream The input stream to be closed.
     */
    private void closeEverything(Socket socket, DataOutputStream outputStream, DataInputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




