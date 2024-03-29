package com.messengerServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NetworkService {
    private final DataOutputStream outputStream;
    private final DataInputStream inputStream;

    public NetworkService(DataOutputStream outputStream, DataInputStream inputStream){
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    public void sendMessage(String messageToSend) throws IOException {
        outputStream.writeUTF(messageToSend);
        outputStream.flush();
    }

    public void sendFileMetadata(byte[] fileNameBytes, byte[] fileContentBytes) throws IOException {
        outputStream.writeInt(fileNameBytes.length);
        outputStream.write(fileNameBytes);
        outputStream.writeInt(fileContentBytes.length);
    }

    public void sendFileContent(byte[] fileContentBytes) throws IOException{
        outputStream.write(fileContentBytes);
        outputStream.flush();
    }

    private byte[] readDataFromClient() throws IOException {
        int length = inputStream.readInt();
        byte[] data = new byte[length];
        inputStream.readFully(data);
        return data;
    }

    public String readFileNameFromClient() throws IOException {
        byte[] fileNameBytes = readDataFromClient();
        return new String(fileNameBytes, StandardCharsets.UTF_8);
    }

    public byte[] readFileContentFromClient() throws IOException {
        return readDataFromClient();
    }

    public String readMessage() throws IOException {
        if (inputStream != null) {
            return inputStream.readUTF();
        } else {
            throw new IOException("InputStream is null.");
        }
    }
}