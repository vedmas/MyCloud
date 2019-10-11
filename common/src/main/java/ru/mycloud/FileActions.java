package ru.mycloud;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileActions {
    private static final Logger log = Logger.getLogger(FileActions.class);

    public List<PackageFile> createListPackage(Path path) {
        byte[] data = convertToByteArray(path);
        List<PackageFile> list = new ArrayList<>();
        byte[] dataTemp;
        int marker = -1;
        if(data.length > Settings.PACKAGE_SIZE) {
            dataTemp = new byte[Settings.PACKAGE_SIZE];
        }
        else {
            dataTemp = new byte[data.length];
        }
        for (int i = 0; i < data.length; i++) {
            marker++;
            if (marker == dataTemp.length) {
                list.add(new PackageFile(path, false, dataTemp));
                marker = 0;
                if(data.length - i < Settings.PACKAGE_SIZE) {
                    dataTemp = new byte[data.length - i];
                } else dataTemp = new byte[Settings.PACKAGE_SIZE];
            }
            dataTemp[marker] = data[i];
        }
        list.add(new PackageFile(path, true, dataTemp));
        return list;
    }

    private byte[] convertToByteArray (Path path) {
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

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

    public void fileDeletion(String catalog, String fileName) {
        Path path = Paths.get(catalog  + File.separator + fileName);
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public void createDirectory(String path) {
        if(Files.notExists(Paths.get("." + File.separator + path))) {
            try {
                Files.createDirectory(Paths.get("." + File.separator + path));
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
    }
}
