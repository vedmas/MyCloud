package ru.mycloud;

import java.io.File;

public class Settings {
    public static final int RECEIVED_FILE = 8007;
    public static final int FILE_LIST_ORDER = 8008;
    public static final int ORDER_REMOVE_FILE = 8009;
    public static final int RESPONSE_ORDER_REMOVE_FILE = 8010;
    public static final int RESPONSE_SEND_FILE = 8011;
    public static final int AUTHORIZATION_PASSED = 8001;
    public static final int AUTHORIZATION_FAILED = 8002;
    public static final int PACKAGE_SIZE = 5 * 1024 * 1024;
    public static final int OBJECT_SIZE_FOR_DECODER = 6 * 1024 * 1024;
    public static final String USER_DIRECTORY_SERVER = "server_storage";
    public static final String USER_DIRECTORY_CLIENT = "client_storage" + File.separator;
    public static String serverDirectory;
    public static String clientDirectory;

    public static final int PORT = 8188;
    public static final String HOST = "localhost";


}
