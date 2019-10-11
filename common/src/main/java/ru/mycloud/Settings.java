package ru.mycloud;

import org.apache.log4j.Logger;

import java.io.File;

public class Settings {
    private static final Logger log = Logger.getLogger(Settings.class);

    public static final int RECEIVED_FILE = 8007;
    public static final int FILE_LIST_ORDER = 8008;
    public static final int ORDER_REMOVE_FILE = 8009;
    public static final int RESPONSE_ORDER_REMOVE_FILE = 8010;
    public static final int RESPONSE_SEND_FILE = 8011;
    public static final int AUTHORIZATION_PASSED = 8001;
    public static final int AUTHORIZATION_FAILED = 8002;
    public static final int PACKAGE_SIZE = 5 * 1024 * 1024;
    public static final int OBJECT_SIZE_FOR_DECODER = 6 * 1024 *1024;
    public static final String SERVER_DIRECTORY = "server_storage";
    public static final String CLIENT_DIRECTORY = "client_storage" + File.separator;

    public static final int PORT = 8188;
    public static final String HOST = "localhost";


}
