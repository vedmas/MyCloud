package ru.MyCloud.client;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ru.MyCloud.common.FileMessage;
import ru.MyCloud.common.OrderMessage;
import ru.MyCloud.common.OrdersNumbers;
import org.apache.log4j.Logger;

public class NettyController implements Initializable {

    private final String CLIENT_DIRECTORY = "client_storage/";
    private final String SERVER_DIRECTORY = "server_storage/";
    private OrdersNumbers ordersNumbers = new OrdersNumbers();
    private NettyController nettyController;

    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> filesList;

    @FXML
    TextField tfFileNameServer;

    @FXML
    ListView<String> filesListServer;


    public String getCLIENT_DIRECTORY() {
        return CLIENT_DIRECTORY;
    }

    public String getSERVER_DIRECTORY() {
        return SERVER_DIRECTORY;
    }

    private static final Logger log = Logger.getLogger(NettyController.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<String> list = new ArrayList<>();
        list.add("Hallo");
        list.add(" Miguel");

        new Thread(new Runnable() {
            @Override
            public void run() {
                NettyNetwork.getInstance().start();
            }
        }).start();

        refreshLocalFilesList();
        refreshServerFilesList(); // локально
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0 && filePresence(tfFileName.getText(), filesListServer)) {
            downloadObject(tfFileName.getText());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            refreshLocalFilesList();
            tfFileName.clear();
        }
    }

    public boolean pressOnSendBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0 && filePresence(tfFileName.getText(), filesList)) {
            sendObject();
            tfFileName.clear();
        }
        return false;
    }

    public boolean pressOnDeleteBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0 && filePresence(tfFileName.getText(), filesList)) {
            ordersNumbers.fileDeletion(CLIENT_DIRECTORY ,tfFileName.getText());
            refreshLocalFilesList();
            tfFileName.clear();
            return true;
        }
        return false;
    }

    //Кнопка на сервере
    public boolean pressOnDeleteBtnServ(ActionEvent actionEvent) {
        if(tfFileNameServer.getLength() > 0 && filePresence(tfFileNameServer.getText(), filesListServer)) {
            removeFileFromServer(tfFileNameServer.getText());
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            refreshServerFilesList(); // локально
            tfFileNameServer.clear();
            return true;
        }
        log.info("There is no such file on the server");
        return false;
    }

    //Скачивание файла с сервера
    private void downloadObject(String fileName) {
        OrderMessage order = new OrderMessage(ordersNumbers.getRECEIVED_FILE(), fileName);
        NettyNetwork.getInstance().getCurrentChannel().writeAndFlush(order);
    }

    //Передача файла на сервер
    private void sendObject() {
        if (tfFileName.getLength() > 0 && filePresence(tfFileName.getText(), filesList)) {
            try {
                FileMessage sendObject = new FileMessage(Paths.get(CLIENT_DIRECTORY + tfFileName.getText()));
                NettyNetwork.getInstance().getCurrentChannel().writeAndFlush(sendObject);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Запрос на удаление файла с сервера
    private void removeFileFromServer(String fileName) {
        OrderMessage order = new OrderMessage(ordersNumbers.getORDER_REMOVE_FILE(), fileName);
        NettyNetwork.getInstance().getCurrentChannel().writeAndFlush(order);
    }
    //поиск файла в списке
    private boolean filePresence (String fileName, ListView list) {
        for (Object value : list.getItems()) {
            if(value.equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    //Отправка запроса на сервер для обновления списка файлов в каталоге на сервере
    private void sendRefreshListFilesToServer() {
        OrderMessage order = new OrderMessage(ordersNumbers.getFILE_LIST_ORDER(), null);
        NettyNetwork.getInstance().getCurrentChannel().writeAndFlush(order);
    }

    //Обновление списка файлов на клиенте
    public void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                filesList.getItems().clear();
                Files.list(Paths.get(CLIENT_DIRECTORY)).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    filesList.getItems().clear();
                    Files.list(Paths.get(CLIENT_DIRECTORY)).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void refresh(List<String> list) {
        if (Platform.isFxApplicationThread()) {
                filesListServer.getItems().clear();
                filesListServer.getItems().addAll(list);
        } else {
            Platform.runLater(() -> {
                filesListServer.getItems().clear();
                filesListServer.getItems().addAll(list);
            });
        }
    }

    //Обновление списка файлов на сервере локально, для проверки метода удаления.
    private void refreshServerFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                filesListServer.getItems().clear();
                Files.list(Paths.get(SERVER_DIRECTORY)).map(p -> p.getFileName().toString()).forEach(o -> filesListServer.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    filesListServer.getItems().clear();
                    Files.list(Paths.get(SERVER_DIRECTORY)).map(p -> p.getFileName().toString()).forEach(o -> filesListServer.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
