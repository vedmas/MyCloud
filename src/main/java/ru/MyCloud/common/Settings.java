package ru.MyCloud.common;

import org.apache.log4j.Logger;

public class Settings {
    private static final Logger log = Logger.getLogger(Settings.class);

    private static final int RECEIVED_FILE = 8007; //Команда на получение файла с сервера
    private static final int FILE_LIST_ORDER = 8008; //Команда на получение списка файлов на сервере
    private static final int ORDER_REMOVE_FILE = 8009; //Команда на удаление файлов на сервере
    private static final int RESPONSE_ORDER_REMOVE_FILE = 8010; // Ответ клиенту на успешное удаление файла на сервере
    private static final int RESPONSE_SEND_FILE = 8011; //Ответ на успешный прием файла на сервере
    private static final int AUTHORIZATION_PASSED = 8001; //Ответ сервера об успешной авторизации
    private static final int AUTHORIZATION_FAILED = 8002; //Ответ сервера о неудачной авторизации
    private static final int PACKAGE_SIZE = 5 * 1024 * 1024; //размер пакета передачи данных

    private static final int PORT = 8188;
    private static final String HOST = "localhost";

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

    public int getAUTHORIZATION_PASSED() {
        return AUTHORIZATION_PASSED;
    }

    public int getAUTHORIZATION_FAILED() {
        return AUTHORIZATION_FAILED;
    }

    public int getPACKAGE_SIZE() {
        return PACKAGE_SIZE;
    }

    public String getHOST() {
        return HOST;
    }

}
