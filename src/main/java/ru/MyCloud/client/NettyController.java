package ru.MyCloud.client;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
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
import ru.MyCloud.common.FileListMassage;
import ru.MyCloud.common.FileMessage;
import ru.MyCloud.common.OrderMessage;
import ru.MyCloud.common.OrdersNumbers;

public class NettyController implements Initializable {

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NettyNetwork.getInstance().start();
            }
        }).start();

        refreshLocalFilesList();
        refreshServerFilesList();
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0 && filePresence(tfFileName.getText(), filesListServer)) {
            downloadObject(tfFileName.getText());
            refreshLocalFilesList();
            tfFileName.clear();
        }
    }

    public boolean pressOnSendBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0 && filePresence(tfFileName.getText(), filesList)) {
            sendObject();
            refreshListFilesToServer();
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
            refreshListFilesToServer();
            refreshServerFilesList(); // локально
            tfFileNameServer.clear();
            return true;
        }
        System.out.println("File not found");
        return false;
    }

    //Скачивание файла с сервера
    private void downloadObject(String fileName) {
        ObjectEncoderOutputStream oeos = null;
        try (Socket socket = new Socket(ordersNumbers.getHOST(), ordersNumbers.getPORT())) {
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            OrderMessage order = new OrderMessage(ordersNumbers.getRECEIVED_FILE(), fileName);
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

    //Передача файла на сервер
    private void sendObject() {
        if (tfFileName.getLength() > 0 && filePresence(tfFileName.getText(), filesList)) {
            ObjectEncoderOutputStream oeos = null;
            try (Socket socket = new Socket(ordersNumbers.getHOST(), ordersNumbers.getPORT())) {
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

    //Запрос на удаление файла с сервера
    private void removeFileFromServer(String fileName) {
        ObjectEncoderOutputStream oeos = null;
        try (Socket socket = new Socket(ordersNumbers.getHOST(), ordersNumbers.getPORT())) {
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            OrderMessage order = new OrderMessage(ordersNumbers.getORDER_REMOVE_FILE(), fileName);
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
    //поиск файла в списке
    private boolean filePresence (String fileName, ListView list) {
        for (Object value : list.getItems()) {
            if(value.equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    //Отправка запроса на сервер для обновления списка файлов в каталоге на сервре
    private void refreshListFilesToServer() {
        ObjectEncoderOutputStream oeos = null;
        ObjectDecoderInputStream odis = null;
        try (Socket socket = new Socket(ordersNumbers.getHOST(), ordersNumbers.getPORT())) {
            oeos = new ObjectEncoderOutputStream(socket.getOutputStream());
            OrderMessage order = new OrderMessage(ordersNumbers.getFILE_LIST_ORDER(), null);
            oeos.writeObject(order);
            oeos.flush();
            odis = new ObjectDecoderInputStream(socket.getInputStream());
            if(odis.readObject() instanceof FileListMassage) {
                System.out.println(true);
                FileListMassage flm = (FileListMassage) odis.readObject();
                System.out.println("Принят список файлов на сервере");
            }
            else System.out.println(false);
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
    }

    //Обновление списка файлов на клиенте
    private void refreshLocalFilesList() {
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
