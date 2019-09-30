package ru.MyCloud.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OrdersNumbers {

    private final int RECEIVED_FILE = 8007;
    private final int FILE_LIST_ORDER = 8008;
    private final int ORDER_REMOVE_FILE = 8009;

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

    public int getORDER_REMOVE_FILE() {
        return ORDER_REMOVE_FILE;
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
        }
    }
}
