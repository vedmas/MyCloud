package ru.MyCloud.common;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileActions {

    private static final org.apache.log4j.Logger log = Logger.getLogger(FileActions.class);

    private Settings settings = new Settings();

    //Создание списка с объектами-пакетами файла для оптравки
    public List<PackageFile> createListPackage(Path path) {
        byte[] data = convertToByteArray(path);
        List<PackageFile> list = new ArrayList<>();
        byte[] dataTemp;
        int marker = -1;
        int packageNumber = 0;
        boolean lastPackage = false;
        if(data.length > settings.getPACKAGE_SIZE()) {
            dataTemp = new byte[settings.getPACKAGE_SIZE()];
        }
        else {
            dataTemp = new byte[data.length];
        }
        for (int i = 0; i < data.length; i++) {
            marker++;
            if (marker == dataTemp.length) {
                list.add(new PackageFile(path, packageNumber, false, dataTemp));
                packageNumber++;
                marker = 0;
                if(data.length - i < settings.getPACKAGE_SIZE()) {
                    dataTemp = new byte[data.length - i];
                } else dataTemp = new byte[settings.getPACKAGE_SIZE()];
            }
            dataTemp[marker] = data[i];
        }
        list.add(new PackageFile(path, packageNumber, true, dataTemp));
        return list;
    }

    //Преобразование файла в byte массив
    private byte[] convertToByteArray (Path path) {
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    //Сборка файла из полученных пакетов
    public byte[] fileRestoredPackets(List<PackageFile> list) {
        byte[] newData = new byte[0];
        int  currentLength;
        for (PackageFile packageFile : list) {
            currentLength = newData.length;
            newData = Arrays.copyOf(newData, newData.length + packageFile.getDataPackage().length);
            System.arraycopy(packageFile.getDataPackage(), 0, newData, currentLength, packageFile.getDataPackage().length);
        }
        return newData;
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
