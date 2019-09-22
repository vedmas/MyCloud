package ru.MyCloud.client;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

import ru.MyCloud.common.AbstractMessage;
import ru.MyCloud.common.FileMessage;
import ru.MyCloud.common.FileRequest;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class MainController implements Initializable {
    @FXML
    TextField tfFileName;

    @FXML
    TextField tfFileNameServer;

    @FXML
    ListView<String> filesList;

    @FXML
    ListView<String> filesListServer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
        refreshServerFilesList();
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            Network.sendMsg(new FileRequest(tfFileName.getText()));
            tfFileName.clear();
        }
    }

    public boolean pressOnDeleteBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            fileDeletion("client_storage/", tfFileName.getText());
            filesList.getItems().remove(tfFileName.getText());
            tfFileName.clear();
            return true;
        }
     return false;
    }

    public boolean pressOnSendBtn(ActionEvent actionEvent) {
        System.out.println("Напиши реализацию и будем отправлять");
        return false;
    }

    public boolean pressOnDeleteBtnServ(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            fileDeletion("server_storage/", tfFileNameServer.getText());
            filesListServer.getItems().remove(tfFileNameServer.getText());
            tfFileNameServer.clear();
            return true;
        }
        return false;
    }

    public boolean pressOnSendBtnServ(ActionEvent actionEvent) {
        System.out.println("Напиши реализацию и будем отправлять");
        return false;
    }


    //Удаление файла по указанному пути
    private void fileDeletion(String pathDirectory, String fileName) {
        String pathFile = pathDirectory + fileName;
        Path path = Paths.get(pathFile);
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                filesList.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    filesList.getItems().clear();
                    Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    private void refreshServerFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                filesListServer.getItems().clear();
                Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesListServer.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    filesListServer.getItems().clear();
                    Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesListServer.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
