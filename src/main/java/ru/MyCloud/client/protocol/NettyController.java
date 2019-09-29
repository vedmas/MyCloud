package ru.MyCloud.client.protocol;

import java.io.File;
import java.io.FileInputStream;
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
import ru.MyCloud.common.FileMessage;
import ru.MyCloud.common.FileRequest;
import ru.MyCloud.common.OrderMessage;
import ru.MyCloud.common.OrdersNumbers;

public class NettyController implements Initializable {

    private final String CLIENT_DIRECTORY = "client_storage/";
    private final int CONNECTION_PORT = 8189;
    OrdersNumbers ordersNumbers;

    @FXML
    TextField tfFileName;

    public ListView<String> getFilesList() {
        return filesList;
    }

    public void setFilesList(ListView<String> filesList) {
        this.filesList = filesList;
    }

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

    public void pressOnDownloadBtn(ActionEvent actionEvent) {

    }

    public boolean pressOnDeleteBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            fileDeletion(tfFileName.getText());
            filesList.getItems().remove(tfFileName.getText());
            tfFileName.clear();
            return true;
        }
        return false;
    }

    public boolean pressOnSendBtn(ActionEvent actionEvent) {
//        sendObject();
        refreshListFilesToServer();
        tfFileName.clear();
        return false;
    }

    private void sendObject() {
        if (tfFileName.getLength() > 0) {
            ObjectEncoderOutputStream oeos = null;
            try (Socket socket = new Socket("localhost", CONNECTION_PORT)) {
                oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
                FileMessage sendObject = new FileMessage(Paths.get(CLIENT_DIRECTORY + tfFileName.getText()));
                oeos.writeObject(sendObject);
                oeos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    assert oeos != null;
                    oeos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //Отправка запроса на сервер для обновления списка файлов в каталоге на сервре
    private void refreshListFilesToServer() {
        ObjectEncoderOutputStream oeos = null;
        try (Socket socket = new Socket("localhost", CONNECTION_PORT)) {
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            OrderMessage order = new OrderMessage(8008);
            oeos.writeObject(order);
            oeos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert oeos != null;
                oeos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
    private void fileDeletion(String fileName) {
        Path path = Paths.get(CLIENT_DIRECTORY + fileName);
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
}
