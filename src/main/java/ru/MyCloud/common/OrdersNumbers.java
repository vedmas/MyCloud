package ru.MyCloud.common;

import org.apache.log4j.Logger;
import ru.MyCloud.client.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OrdersNumbers {
    private static final org.apache.log4j.Logger log = Logger.getLogger(OrdersNumbers.class);

    private final int RECEIVED_FILE = 8007; //Команда на получение файла с сервера
    private final int FILE_LIST_ORDER = 8008; //Команда на получение списка файлов на сервере
    private final int ORDER_REMOVE_FILE = 8009; //Команда на удаление файлов на сервере
    private final int RESPONSE_ORDER_REMOVE_FILE = 8010; // Ответ клиенту на успешное удаление файла на сервере
    private final int RESPONSE_SEND_FILE = 8011; //Ответ на успешный прием файла на сервере
    private final int PACKAGE_SIZE = 5 * 1024 * 1024; //размер пакета передачи данных

    private final int PORT = 8188;
    private final String HOST = "localhost";

    public int getRECEIVED_FILE() {
        return RECEIVED_FILE;
    }

    public int getPORT() {
        return PORT;
    }

    public int getFILE_LIST_ORDER() {
        return FILE_LIST_ORDER;
    }

    public int getRESPONSE_ORDER_REMOVE_FILE() {
        return RESPONSE_ORDER_REMOVE_FILE;
    }

    public int getRESPONSE_SEND_FILE() {
        return RESPONSE_SEND_FILE;
    }

    public int getORDER_REMOVE_FILE() {
        return ORDER_REMOVE_FILE;
    }

    public int getPACKAGE_SIZE() {
        return PACKAGE_SIZE;
    }

    public String getHOST() {
        return HOST;
    }

    //Удаление файла по указанному пути
    public void fileDeletion(String catalog, String fileName) {
        Path path = Paths.get(catalog  + "/" + fileName);
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    //Создание каталога для хранения файлов
    public void createDirectory(String path) {
        if(Files.notExists(Paths.get("./" + path))) {
            try {
                Files.createDirectory(Paths.get("./" + path));
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }

}
