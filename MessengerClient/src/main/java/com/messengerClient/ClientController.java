package com.messengerClient;

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
 * The ClientController class is responsible for handling the client-side functionality
 * of the messenger application. It initializes the connection to the server,
 * handles sending and receiving messages, and manages file transfer operations.
 */
public class ClientController implements Initializable {

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
    private static final Logger logger = Logger.getLogger(ClientController.class.getName());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        connectToServer();
        messageDisplayService = new MessageDisplayService(vbox_messages);
        vbox_messages.heightProperty().addListener((observableValue, number, t1) -> sp_main.setVvalue((Double) t1));
        button_send.setOnAction(this::handleSendMessage);
        button_send_file.setOnAction(this::handleSendFile);
        receiveMessageFromServer(vbox_messages);
    }

    private void connectToServer() {
        String serverAddress = "localhost";
        int serverPort = 1234;
        try {
            socket = new Socket(serverAddress, serverPort);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());
            networkService = new NetworkService(outputStream, inputStream);

            System.out.println("Connected with server: " + serverAddress + ":" + serverPort);
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

    private void receiveMessageFromServer(VBox vBox) {
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


//import javafx.application.Platform;
//import javafx.fxml.FXML;
//import javafx.fxml.Initializable;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//import javafx.scene.control.Button;
//import javafx.scene.control.ScrollPane;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.scene.paint.Color;
//import javafx.scene.text.Text;
//import javafx.scene.text.TextFlow;
//import javafx.stage.FileChooser;
//
//import java.io.*;
//import java.net.*;
//import java.nio.file.Files;
//import java.util.ResourceBundle;
//
///**
// * The ClientController class is responsible for handling the client-side functionality
// * of the messenger application. It initializes the connection to the server,
// * handles sending and receiving messages, and manages file transfer operations.
// */
//public class ClientController implements Initializable {
//
//    @FXML
//    private Button button_send_file;
//    @FXML
//    private Button button_send;
//    @FXML
//    private TextField tf_message;
//    @FXML
//    private VBox vbox_messages;
//    @FXML
//    private ScrollPane sp_main;
//    private Socket socket;
//    private DataOutputStream outputStream;
//    private DataInputStream inputStream;
//
//    /**
//     * @param url The location used to resolve relative paths for the root object, or null if the location is not known.
//     * @param resourceBundle The resources used to localize the root object, or null if the root object was not localized.
//     */
//    @Override
//    public void initialize(URL url, ResourceBundle resourceBundle) {
//        connectToServer();
//        setupMessageArea();
//        configureSendButton();
//        configureSendFileButton();
//        receiveMessageFromServer(vbox_messages);
//    }
//
//    private void connectToServer() {
//        String serverAddress = "localhost";
//        int serverPort = 1234;
//        try {
//            socket = new Socket(serverAddress, serverPort);
//            outputStream = new DataOutputStream(socket.getOutputStream());
//            inputStream = new DataInputStream(socket.getInputStream());
//            System.out.println("Connected with server: " + serverAddress + ":" + serverPort);
//        } catch (IOException e) {
//            System.out.println("Error creating Client");
//            e.printStackTrace();
//        }
//    }
//
//    private void setupMessageArea() {
//        vbox_messages.heightProperty().addListener((observableValue, oldVal, newVal) ->
//                sp_main.setVvalue(newVal.doubleValue()));
//    }
//
//    private void configureSendButton() {
//        button_send.setOnAction(actionEvent -> {
//            String messageToSend = tf_message.getText();
//            if (!messageToSend.isEmpty()) {
//                sendMessageAndClear(messageToSend);
//            }
//        });
//    }
//
//    /**
//     * @param messageToSend The message text to be sent to the server.
//     */
//    private void sendMessageAndClear(String messageToSend) {
//        messageViewSetUp(messageToSend);
//        sendMessageToServer(messageToSend);
//        tf_message.clear();
//    }
//
//    private void configureSendFileButton() {
//        button_send_file.setOnAction(actionEvent -> {
//            File fileToSend = chooseFileToSend();
//            if (fileToSend != null) {
//                sendFileToServer(fileToSend);
//            }
//        });
//    }
//
//    /**
//     * @return The file selected by the user or null if no file was selected.
//     */
//    private File chooseFileToSend() {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Choose file");
//        return fileChooser.showOpenDialog(null);
//    }
//
//    /**
//     * @param vBox The VBox component where incoming messages will be displayed.
//     */
//    public void receiveMessageFromServer(VBox vBox) {
//        new Thread(() -> {
//            try {
//                while (socket.isConnected()) {
//                    String messageFromServer = inputStream.readUTF();
//                    processServerMessage(messageFromServer, vBox);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
//
//    /**
//     * @param messageFromServer The message received from the server.
//     * @param vBox The VBox component where the message will be displayed.
//     * @throws IOException if there is an error during input/output operations.
//     */
//    private void processServerMessage(String messageFromServer, VBox vBox) throws IOException {
//        if (messageFromServer.equals("FILE:")) {
//            saveFileReceivedFromServer();
//        } else {
//            Platform.runLater(() -> addLabel(messageFromServer, vBox));
//        }
//    }
//
//    private void saveFileReceivedFromServer() throws IOException {
//        String fileName = receiveFileName();
//        byte[] fileContentBytes = receiveFileContent();
//        writeFileToDisk(fileName, fileContentBytes);
//        System.out.println("Received and saved file: " + fileName);
//    }
//
//    private String receiveFileName() throws IOException {
//        int fileNameLength = inputStream.readInt();
//        byte[] fileNameBytes = new byte[fileNameLength];
//        inputStream.readFully(fileNameBytes);
//        return new String(fileNameBytes);
//    }
//
//    private byte[] receiveFileContent() throws IOException {
//        int fileContentLength = inputStream.readInt();
//        byte[] fileContentBytes = new byte[fileContentLength];
//        inputStream.readFully(fileContentBytes);
//        return fileContentBytes;
//    }
//
//    /**
//     * @param fileName         The name of the file to write to.
//     * @param fileContentBytes The byte array containing the file's content.
//     * @throws IOException If an I/O error occurs while writing the file.
//     */
//    private void writeFileToDisk(String fileName, byte[] fileContentBytes) throws IOException {
//        FileOutputStream fileOutputStream = new FileOutputStream(fileName);
//        try {
//            fileOutputStream.write(fileContentBytes);
//        } finally {
//            fileOutputStream.close();
//        }
//    }
//
//    /**
//     * @param messageFromServer The message to be added.
//     * @param vbox              The VBox to which the message label will be added.
//     */
//    public static void addLabel(String messageFromServer, VBox vbox) {
//        HBox hBox = createMessageHBox(messageFromServer);
//        Platform.runLater(() -> vbox.getChildren().add(hBox));
//    }
//
//
//    /**
//     * @param message The message to be displayed in the HBox.
//     * @return A new HBox containing the message.
//     */
//    private static HBox createMessageHBox(String message) {
//        HBox hBox = new HBox();
//        hBox.setAlignment(Pos.CENTER_LEFT);
//        hBox.setPadding(new Insets(5, 5, 5, 10));
//        TextFlow textFlow = createTextFlow(message);
//        hBox.getChildren().add(textFlow);
//        return hBox;
//    }
//
//    /**
//     * @param message The message to be displayed in the TextFlow.
//     * @return A new TextFlow containing the message.
//     */
//    private static TextFlow createTextFlow(String message) {
//        Text text = new Text(message);
//        TextFlow textFlow = new TextFlow(text);
//        textFlow.setStyle("-fx-background-color: rgb(233,233,235);" +
//                "-fx-background-radius: 20px;");
//        textFlow.setPadding(new Insets(5, 10, 5, 10));
//        return textFlow;
//    }
//
//    /**
//     * @param fileToSend The file to be sent to the server.
//     */
//    public void sendFileToServer(File fileToSend) {
//        try {
//            byte[] fileNameBytes = fileToSend.getName().getBytes();
//            byte[] fileBytes = Files.readAllBytes(fileToSend.toPath());
//            sendFileMetadata(fileNameBytes, fileBytes.length);
//            sendFileBytes(fileBytes);
//            messageViewSetUp(fileToSend.getName());
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("Error sending File to Client");
//            closeEverything(socket, outputStream, inputStream);
//        }
//    }
//
//    /**
//     * Sends file metadata, including the file name and file length, to the server.
//     *
//     * @param fileNameBytes The byte array containing the file name.
//     * @param fileLength    The length of the file content in bytes.
//     * @throws IOException If an I/O error occurs while sending metadata.
//     */
//    private void sendFileMetadata(byte[] fileNameBytes, int fileLength) throws IOException {
//        outputStream.writeUTF("FILE:");
//        outputStream.writeInt(fileNameBytes.length);
//        outputStream.write(fileNameBytes);
//        outputStream.writeInt(fileLength);
//    }
//
//    /**
//     * @param fileBytes The byte array containing the file's content.
//     * @throws IOException If an I/O error occurs while sending file bytes.
//     */
//    private void sendFileBytes(byte[] fileBytes) throws IOException {
//        outputStream.write(fileBytes);
//        outputStream.flush();
//    }
//
//    /**
//     * @param messageToSend The message string to be sent.
//     */
//    public void sendMessageToServer(String messageToSend) {
//        try {
//            outputStream.writeUTF(messageToSend);
//            outputStream.flush();
//            tf_message.clear();
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("Error sending message to the client");
//            closeEverything(socket, outputStream, inputStream);
//        }
//    }
//
//    /**
//     * Updates the message view to display the sent message.
//     *
//     * @param messageToSend The message string that was sent.
//     */
//    public void messageViewSetUp(String messageToSend) {
//        if (!messageToSend.isEmpty()) {
//            HBox messageHBox = createOutgoingMessageHBox(messageToSend);
//            Platform.runLater(() -> vbox_messages.getChildren().add(messageHBox));
//        }
//    }
//
//    /**
//     * @param message The message to be displayed in the outgoing TextFlow.
//     * @return A new TextFlow formatted for outgoing messages.
//     */
//    private HBox createOutgoingMessageHBox(String message) {
//        HBox hBox = new HBox();
//        hBox.setAlignment(Pos.CENTER_RIGHT);
//        hBox.setPadding(new Insets(5, 5, 5, 10));
//        TextFlow textFlow = createOutgoingTextFlow(message);
//        hBox.getChildren().add(textFlow);
//        return hBox;
//    }
//
//    /**
//     * @param message The message to be displayed in the outgoing TextFlow.
//     * @return A new TextFlow formatted for outgoing messages.
//     */
//    private TextFlow createOutgoingTextFlow(String message) {
//        Text text = new Text(message);
//        TextFlow textFlow = new TextFlow(text);
//        textFlow.setStyle("-fx-color: rgb(239,242,255); " +
//                "-fx-background-color: rgb(15,125,242);" +
//                "-fx-background-radius: 20px;");
//        textFlow.setPadding(new Insets(5, 10, 5, 10));
//        text.setFill(Color.color(0.934, 0.945, 0.996));
//        return textFlow;
//    }
//
//    /**
//     * @param socket        The socket to be closed.
//     * @param outputStream The output stream to be closed.
//     * @param inputStream  The input stream to be closed.
//     */
//    public void closeEverything(Socket socket, DataOutputStream outputStream, DataInputStream inputStream) {
//        try {
//            if (outputStream != null) {
//                outputStream.close();
//            }
//            if (outputStream != null) {
//                outputStream.close();
//            }
//            if (socket != null) {
//                socket.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
