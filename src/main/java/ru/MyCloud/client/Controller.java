package ru.MyCloud.client;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import ru.MyCloud.common.FileMessage;
import ru.MyCloud.common.OrderMessage;
import ru.MyCloud.common.OrdersNumbers;
import org.apache.log4j.Logger;

public class Controller implements Initializable {
    private static final Logger log = Logger.getLogger(Controller.class);

    private final String CLIENT_DIRECTORY = "client_storage/";
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
        ordersNumbers.createDirectory(CLIENT_DIRECTORY);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Network.getInstance().start(Controller.this);
            }
        }).start();
        starterRefreshFilesLists();
        setUIListeners();
    }

    //Обновляем списки файлов в каталогах клиента и сервера, если клиент еще не запущен, то ждем запуска.
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

    //Параметры контекстных меню

    private void setUIListeners() {
        //Контекстное меню в разделе клиента
        ContextMenu clientContextMenu = new ContextMenu();

        //Кнопка обновления списка файлов клиента
        MenuItem refreshClientFiles = new MenuItem("Refresh");
        refreshClientFiles.setOnAction(event -> {
            refreshLocalFilesList();
        });
        refreshClientFiles.setUserData(Boolean.TRUE);
        clientContextMenu.getItems().add(refreshClientFiles);

        MenuItem uploadToCloud = new MenuItem("Upload to Cloud");
        uploadToCloud.setOnAction(event -> {
            sendObject(filesList.getSelectionModel().getSelectedItem());
        });
        clientContextMenu.getItems().add(uploadToCloud);

        MenuItem deleteInTheClient = new MenuItem("Delete");
        deleteInTheClient.setOnAction(event -> {
            ordersNumbers.fileDeletion(CLIENT_DIRECTORY ,filesList.getSelectionModel().getSelectedItem());
            refreshLocalFilesList();
        });
        clientContextMenu.getItems().add(deleteInTheClient);

        //Контекстное меню в разделе облака
        ContextMenu serverContextMenu = new ContextMenu();

        MenuItem refreshServerFiles = new MenuItem("Refresh");
        refreshServerFiles.setOnAction(event -> {
            sendRefreshListFilesToServer();
        });
        refreshServerFiles.setUserData(Boolean.TRUE);
        serverContextMenu.getItems().add(refreshServerFiles);

        MenuItem downloadToClient = new MenuItem("Download");
        downloadToClient.setOnAction(event -> {
            downloadObject(filesListServer.getSelectionModel().getSelectedItem());
        });
        serverContextMenu.getItems().add(downloadToClient);

        MenuItem deleteInTheCloud = new MenuItem("Delete");
        deleteInTheCloud.setOnAction(event -> {
            removeFileFromServer(filesListServer.getSelectionModel().getSelectedItem());
        });
        serverContextMenu.getItems().add(deleteInTheCloud);

        //Обработка клика по файлам в окне клиента
        filesList.setOnMouseClicked(event -> {
            // Захватываем имя файла по которому кликнули
            String fs = filesList.getSelectionModel().getSelectedItem();
            //Скрытие контекстного меню если оно открыто
            hideContextMenus(clientContextMenu, serverContextMenu);

            if (event.getButton().equals(MouseButton.SECONDARY) && fs != null) {
                for (MenuItem mi:clientContextMenu.getItems()) {
                    if (!mi.isVisible())
                        mi.setVisible(true);
                }
                clientContextMenu.show(filesList, event.getScreenX(), event.getScreenY());

            } else if(event.getButton().equals(MouseButton.SECONDARY)) {
                for (MenuItem mi:clientContextMenu.getItems()) {
                    if (mi.isVisible() && (mi.getUserData() == null))
                        mi.setVisible(false);
                }
                clientContextMenu.show(filesList, event.getScreenX(), event.getScreenY());
            }
        });

        //Обработка клика по файлам в окне сервера
        filesListServer.setOnMouseClicked(event -> {
            String fs = filesListServer.getSelectionModel().getSelectedItem();
            //Скрытие контекстного меню если оно открыто
            hideContextMenus(clientContextMenu, serverContextMenu);
            if (event.getButton().equals(MouseButton.SECONDARY) && fs != null) {
                for (MenuItem mi:serverContextMenu.getItems()) {
                    if (!mi.isVisible())
                        mi.setVisible(true);
                }
                serverContextMenu.show(filesListServer, event.getScreenX(), event.getScreenY());
            }
            else if(event.getButton().equals(MouseButton.SECONDARY)) {

                for (MenuItem mi:serverContextMenu.getItems()) {
                    if (mi.isVisible() && (mi.getUserData() == null))
                    mi.setVisible(false);
                }
                serverContextMenu.show(filesListServer, event.getScreenX(), event.getScreenY());
            }
        });

    }
    //Скрытие контекстного меню
    private void hideContextMenus(ContextMenu ... contextMenus) {
        for (ContextMenu cm:contextMenus) {
            if (cm.isShowing())
                cm.hide();
        }
    }

        //Кнопка поиска в окне клиента
    public void pressOnSearchFileClient(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0 && filePresence(tfFileName.getText(), filesList)) {
            sortList(tfFileName.getText(), filesList);
            tfFileName.clear();
        }
        tfFileName.clear();
    }

    //Кнопка поиска в окне облака
    public boolean pressOnSearchFileServer(ActionEvent actionEvent) {
        if(tfFileNameServer.getLength() > 0 && filePresence(tfFileNameServer.getText(), filesListServer)) {
            sortList(tfFileNameServer.getText(), filesListServer);
            tfFileNameServer.clear();
            return true;
        }
        tfFileNameServer.clear();
        return false;
    }

    //Скачивание файла с облака
    private void downloadObject(String fileName) {
        OrderMessage order = new OrderMessage(ordersNumbers.getRECEIVED_FILE(), fileName);
        Network.getInstance().getCurrentChannel().writeAndFlush(order);
    }

    //Передача файла в облако
    private void sendObject(String fileName) {
            try {
                FileMessage sendObject = new FileMessage(Paths.get(CLIENT_DIRECTORY + fileName));
                Network.getInstance().getCurrentChannel().writeAndFlush(sendObject);
                log.info("Файл отправлен на сервер");
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
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

    private void sortList(String fileName, ListView<String> list) {
        list.getItems().remove(fileName);
        list.getItems().add(0,fileName);
    }

    //Отправка запроса в облако для обновления списка файлов в каталоге в облаке
    private void sendRefreshListFilesToServer() {
            OrderMessage order = new OrderMessage(ordersNumbers.getFILE_LIST_ORDER(), null);
            Network.getInstance().getCurrentChannel().writeAndFlush(order);
    }

    //Обновление списка файлов на клиенте
    void refreshLocalFilesList() {
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

        //Обновление списка файлов в окне облака
    void refresh(List<String> list) {
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