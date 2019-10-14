package ru.mycloud;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileActions {
    private static final Logger log = Logger.getLogger(FileActions.class);

    public static void fileDeletion(String catalog, String fileName) {
        Path path = Paths.get(catalog + File.separator + fileName);
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public static void createDirectory(String path) {
        if (Files.notExists(Paths.get("." + File.separator + path))) {
            try {
                Files.createDirectory(Paths.get("." + File.separator + path));
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }
}
