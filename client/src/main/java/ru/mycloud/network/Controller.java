package ru.mycloud.network;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;
import ru.mycloud.FileActions;
import ru.mycloud.PackageFile;
import ru.mycloud.Settings;
import ru.mycloud.message.AuthMessage;
import ru.mycloud.message.CommandMessage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static final Logger log = Logger.getLogger(Controller.class);

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

    void setAuthorized() {
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FileActions.createDirectory(Settings.CLIENT_DIRECTORY);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Network.getInstance().start(Controller.this);
            }
        }).start();
        starterRefreshFilesLists();
        setUIListeners();
    }

    //Updating the file list.
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

    //Context menu options
    private void setUIListeners() {
        //Client
        ContextMenu clientContextMenu = new ContextMenu();

        MenuItem refreshClientFiles = new MenuItem("Refresh");
        refreshClientFiles.setOnAction(event -> {
            refreshLocalFilesList();
        });
        refreshClientFiles.setUserData(Boolean.TRUE);
        clientContextMenu.getItems().add(refreshClientFiles);

        MenuItem uploadToCloud = new MenuItem("Upload to Cloud");
        uploadToCloud.setOnAction(event -> {
            sendingPacketsFile(filesList.getSelectionModel().getSelectedItem());
        });
        clientContextMenu.getItems().add(uploadToCloud);

        MenuItem deleteInTheClient = new MenuItem("Delete");
        deleteInTheClient.setOnAction(event -> {
            FileActions.fileDeletion(Settings.CLIENT_DIRECTORY ,filesList.getSelectionModel().getSelectedItem());
            refreshLocalFilesList();
        });
        clientContextMenu.getItems().add(deleteInTheClient);

        //Cloud
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

        //Click on the file
        filesList.setOnMouseClicked(event -> {
            String fs = filesList.getSelectionModel().getSelectedItem();
            //Hide the context menu
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

        //Handling clicks in the cloud
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
    private void hideContextMenus(ContextMenu ... contextMenus) {
        for (ContextMenu cm:contextMenus) {
            if (cm.isShowing())
                cm.hide();
        }
    }

    public void pressBtnToAuth(ActionEvent actionEvent) {
        if(!authLoginTF.getText().isEmpty() && !authPasswordPF.getText().isEmpty()) {
            AuthMessage authMessage = new AuthMessage(authLoginTF.getText(), authPasswordPF.getText());
            Network.getInstance().getCurrentChannel().writeAndFlush(authMessage);
        }
    }

    public void pressOnSearchFileClient(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0 && filePresence(tfFileName.getText(), filesList)) {
            sortList(tfFileName.getText(), filesList);
            tfFileName.clear();
        }
        tfFileName.clear();
    }

    public boolean pressOnSearchFileServer(ActionEvent actionEvent) {
        if(tfFileNameServer.getLength() > 0 && filePresence(tfFileNameServer.getText(), filesListServer)) {
            sortList(tfFileNameServer.getText(), filesListServer);
            tfFileNameServer.clear();
            return true;
        }
        tfFileNameServer.clear();
        return false;
    }

    private void downloadObject(String fileName) {
        CommandMessage order = new CommandMessage(Settings.RECEIVED_FILE, fileName);
        Network.getInstance().getCurrentChannel().writeAndFlush(order);
    }

    private void sendingPacketsFile(String fileName) {
        for (PackageFile packageFile : FileActions.createListPackage(Paths.get(Settings.CLIENT_DIRECTORY + fileName))) {
            Network.getInstance().getCurrentChannel().writeAndFlush(packageFile);
        }
    }

    private void removeFileFromServer(String fileName) {
        CommandMessage order = new CommandMessage(Settings.ORDER_REMOVE_FILE, fileName);
        Network.getInstance().getCurrentChannel().writeAndFlush(order);
    }

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

    private void sendRefreshListFilesToServer() {
            CommandMessage order = new CommandMessage(Settings.FILE_LIST_ORDER, null);
            Network.getInstance().getCurrentChannel().writeAndFlush(order);
    }

    void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                filesList.getItems().clear();
                Files.list(Paths.get(Settings.CLIENT_DIRECTORY)).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    filesList.getItems().clear();
                    Files.list(Paths.get(Settings.CLIENT_DIRECTORY)).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    void refreshCloudFileList(List<String> list) {
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

    void setMessage() {
        Platform.runLater(() -> authMsg.setText("Login or password incorrect"));
    }
}