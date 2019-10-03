package ru.MyCloud.client;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ru.MyCloud.common.FileMessage;
import ru.MyCloud.common.OrderMessage;
import ru.MyCloud.common.OrdersNumbers;
import org.apache.log4j.Logger;

public class Controller implements Initializable {
    private static final Logger log = Logger.getLogger(Controller.class);

    private final String CLIENT_DIRECTORY = "client_storage/";
    private final String SERVER_DIRECTORY = "server_storage/";
    private OrdersNumbers ordersNumbers = new OrdersNumbers();

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Network.getInstance().start(Controller.this);
            }
        }).start();
        starterRefreshFilesLists();
    }

    private void starterRefreshFilesLists() {
        if(Network.getInstance().isConnectionOpened()) {
            log.info("Client started!");
            refreshLocalFilesList();
            sendRefreshListFilesToServer();
        }
        else {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
            starterRefreshFilesLists();
        }
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0 && filePresence(tfFileName.getText(), filesListServer)) {
            downloadObject(tfFileName.getText());
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

    //Кнопка удалить файл в облаке
    public boolean pressOnDeleteBtnServ(ActionEvent actionEvent) {
        if(tfFileNameServer.getLength() > 0 && filePresence(tfFileNameServer.getText(), filesListServer)) {
            removeFileFromServer(tfFileNameServer.getText());
            tfFileNameServer.clear();
            return true;
        }
        log.error("There is no such file on the server");
        return false;
    }
    //Кнопка обновить список файлов в облаке
    public void pressOnRefreshBtnServ(ActionEvent actionEvent) {
        sendRefreshListFilesToServer();
    }

    //Скачивание файла с сервера
    private void downloadObject(String fileName) {
        OrderMessage order = new OrderMessage(ordersNumbers.getRECEIVED_FILE(), fileName);
        Network.getInstance().getCurrentChannel().writeAndFlush(order);
    }

    //Передача файла в облако
    private void sendObject() {
        if (tfFileName.getLength() > 0 && filePresence(tfFileName.getText(), filesList)) {
            try {
                FileMessage sendObject = new FileMessage(Paths.get(CLIENT_DIRECTORY + tfFileName.getText()));
                Network.getInstance().getCurrentChannel().writeAndFlush(sendObject);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Запрос на удаление файла из облака
    private void removeFileFromServer(String fileName) {
        OrderMessage order = new OrderMessage(ordersNumbers.getORDER_REMOVE_FILE(), fileName);
        Network.getInstance().getCurrentChannel().writeAndFlush(order);
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

    //Отправка запроса в облако для обновления списка файлов в каталоге в облаке
    private void sendRefreshListFilesToServer() {
            OrderMessage order = new OrderMessage(ordersNumbers.getFILE_LIST_ORDER(), null);
            Network.getInstance().getCurrentChannel().writeAndFlush(order);
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
}
