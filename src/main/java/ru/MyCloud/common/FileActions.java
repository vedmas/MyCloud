package ru.MyCloud.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileActions {

    OrdersNumbers ordersNumbers = new OrdersNumbers();

    //Создание списка с объектами-пакетами файла для оптравки
    public List<PackageFile> createListPackage(Path path) {
        byte[] data = convertToByteArray(path);
        List<PackageFile> list = new ArrayList<>();
        byte[] dataTemp;
        int marker = -1;
        int packageNumber = 0;
        boolean lastPackage = false;
        if(data.length > ordersNumbers.getPACKAGE_SIZE()) {
            dataTemp = new byte[ordersNumbers.getPACKAGE_SIZE()];
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
                if(data.length - i < ordersNumbers.getPACKAGE_SIZE()) {
                    dataTemp = new byte[data.length - i];
                } else dataTemp = new byte[ordersNumbers.getPACKAGE_SIZE()];
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

}
