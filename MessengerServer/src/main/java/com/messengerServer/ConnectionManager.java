package com.messengerServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionManager {
    private static final int SERVER_PORT = 1234;
    private static final Logger logger = Logger.getLogger(ConnectionManager.class.getName());
    private ServerSocket serverSocket;
    private Socket socket;
    private NetworkService networkService;

    public ConnectionManager() {
    }

    public void initializeServer() throws IOException {

        serverSocket = new ServerSocket(SERVER_PORT);
        logger.info("Server waiting for connection on port: " + SERVER_PORT);
        acceptClientConnection(serverSocket);
    }

    private void acceptClientConnection(ServerSocket serverSocket) throws IOException {
        this.socket = serverSocket.accept();
        logger.info("Client connected on port: " + serverSocket.getLocalPort());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        this.networkService = new NetworkService(outputStream, inputStream);
    }

    public NetworkService getNetworkService() {
        return networkService;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Problem with closing the connection", e);
        }
    }
}