package ru.MyCloud.client.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ru.MyCloud.client.Network;
import ru.MyCloud.common.FileRequest;

public class NettyController implements Initializable {
    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> filesList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NettyNetwork.getInstance().start();
            }
        }).start();

        refreshLocalFilesList();
    }

    public void pressOnSendData(ActionEvent actionEvent) {
        NettyNetwork.getInstance().sendData(new FileRequest(tfFileName.getText()));
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {

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
        if (tfFileName.getLength() > 0) {
            ObjectEncoderOutputStream oeos = null;
            ObjectDecoderInputStream odis = null;
            try (Socket socket = new Socket("localhost", 8189)) {
                oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
                FileRequest textMessage = new FileRequest(tfFileName.getText());
                oeos.writeObject(textMessage);
                oeos.flush();
                odis = new ObjectDecoderInputStream(socket.getInputStream());
                FileRequest msgFromServer = (FileRequest) odis.readObject();
                System.out.println("Answer from server: " + msgFromServer.getFilename());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    oeos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    odis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            tfFileName.clear();
        }
        return false;
    }

    //На сервере
    public boolean pressOnDeleteBtnServ(ActionEvent actionEvent) {
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

    public void refreshLocalFilesList() {
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
}
