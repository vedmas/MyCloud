package ru.MyCloud.network;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;
import ru.MyCloud.FileActions;
import ru.MyCloud.PackageFile;
import ru.MyCloud.Settings;
import ru.MyCloud.message.AuthMessage;
import ru.MyCloud.message.OrderMessage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static final Logger log = Logger.getLogger(Controller.class);

    private final String CLIENT_DIRECTORY = "client_storage/";
    private Settings settings = new Settings();
    private FileActions fileActions = new FileActions();
    private boolean isAuthorized;

    @FXML
    TextField tfFileName, tfFileNameServer, authLoginTF;

    @FXML
    PasswordField authPasswordPF;

    @FXML
    ListView<String> filesList, filesListServer;

    @FXML
    HBox upperPanel, bottomPanel, bottomPane2;

    @FXML
    Label authMsg;



    public String getCLIENT_DIRECTORY() {
        return CLIENT_DIRECTORY;
    }

    void setAuthorized(Boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        if(!isAuthorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            bottomPane2.setVisible(false);
            bottomPane2.setManaged(false);
            filesList.setVisible(false);
            filesList.setManaged(false);
            filesListServer.setVisible(false);
            filesListServer.setManaged(false);
        }
        else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            bottomPane2.setVisible(true);
            bottomPane2.setManaged(true);
            filesList.setVisible(true);
            filesList.setManaged(true);
            filesListServer.setVisible(true);
            filesListServer.setManaged(true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileActions.createDirectory(CLIENT_DIRECTORY);
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

        //Кнопка отправки файла в облако
        MenuItem uploadToCloud = new MenuItem("Upload to Cloud");
        uploadToCloud.setOnAction(event -> {
            sendingPacketsFile(filesList.getSelectionModel().getSelectedItem());
        });
        clientContextMenu.getItems().add(uploadToCloud);

        //Кнопка удаления файла
        MenuItem deleteInTheClient = new MenuItem("Delete");
        deleteInTheClient.setOnAction(event -> {
            fileActions.fileDeletion(CLIENT_DIRECTORY ,filesList.getSelectionModel().getSelectedItem());
            refreshLocalFilesList();
        });
        clientContextMenu.getItems().add(deleteInTheClient);

        //Контекстное меню в разделе облака
        ContextMenu serverContextMenu = new ContextMenu();

        //Кнопка обновления списка файлов
        MenuItem refreshServerFiles = new MenuItem("Refresh");
        refreshServerFiles.setOnAction(event -> {
            sendRefreshListFilesToServer();
        });
        refreshServerFiles.setUserData(Boolean.TRUE);
        serverContextMenu.getItems().add(refreshServerFiles);

        //Кнопка скачивания файла в дерикторию клиента
        MenuItem downloadToClient = new MenuItem("Download");
        downloadToClient.setOnAction(event -> {
            downloadObject(filesListServer.getSelectionModel().getSelectedItem());
        });
        serverContextMenu.getItems().add(downloadToClient);

        //Кнопка удаления файла в облаке
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

    //Кнопка авторизации
    public void pressBtnToAuth(ActionEvent actionEvent) {
        if(!authLoginTF.getText().isEmpty() && !authPasswordPF.getText().isEmpty()) {
            AuthMessage authMessage = new AuthMessage(authLoginTF.getText(), authPasswordPF.getText());
            Network.getInstance().getCurrentChannel().writeAndFlush(authMessage);
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
        OrderMessage order = new OrderMessage(settings.getRECEIVED_FILE(), fileName);
        Network.getInstance().getCurrentChannel().writeAndFlush(order);
    }

    //Отправка файла пакетами
    private void sendingPacketsFile(String fileName) {
        for (PackageFile packageFile : fileActions.createListPackage(Paths.get(CLIENT_DIRECTORY + fileName))) {
            Network.getInstance().getCurrentChannel().writeAndFlush(packageFile);
        }
    }

    //Запрос на удаление файла из облака
    private void removeFileFromServer(String fileName) {
        OrderMessage order = new OrderMessage(settings.getORDER_REMOVE_FILE(), fileName);
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
            OrderMessage order = new OrderMessage(settings.getFILE_LIST_ORDER(), null);
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

    public void setMessage(String text) {
        Platform.runLater(() -> authMsg.setText(text));
    }
}