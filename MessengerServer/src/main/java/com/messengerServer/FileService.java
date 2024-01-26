package com.messengerServer;

import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class FileService {

    public File chooseFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file");
        return fileChooser.showOpenDialog(null);
    }

    public byte[] readFileContent(File fileToSend) throws IOException {
        return Files.readAllBytes(fileToSend.toPath());
    }

    public void saveFile(String fileName, byte[] fileContent) throws IOException{
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            fileOutputStream.write(fileContent);
        }
    }
}
