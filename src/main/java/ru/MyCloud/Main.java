package ru.MyCloud;

import ru.MyCloud.common.FileActions;
import ru.MyCloud.common.PackageFile;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
//        Path path = Paths.get("./client_storage/1.mp3");
//        FileActions fileActions = new FileActions();
//        List<PackageFile> list = fileActions.createListPackage("1.mp3", path);
//        System.out.println("Размер списка объектов: " + list.size());
//        for (PackageFile packageFile : list) {
//            System.out.println("Имя файла: " + packageFile.getFileName());
//            System.out.println("Номер пакета: " + packageFile.getNumberPackage());
//            System.out.println("Признак последнего пакета: " + packageFile.isLastPackage());
//        }



//        byte[] data = new byte[0];
//            try {
//                data = Files.readAllBytes(path);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            byte[] newData = mergeFile(splitFile(Paths.get("./client_storage/1.mp3"), 100 * 1024));
//        System.out.println(newData.length);


//            int[] n1 = new int[] {1, 2, 3};
//            int[] n2 = new int[] {4, 5};
//            n2 = Arrays.copyOf(n2, n1.length + n2.length);
//        System.arraycopy(n1, 0, n2, 2, n1.length);
//        System.out.println("n2.length = " + n2.length);
//        for (int i : n2) {
//            System.out.println(i);
//        }


//        try {
//            Files.write(Paths.get("./client_storage/2.mp3"), fileActions.fileRestoredPackets(list), StandardOpenOption.CREATE);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    private static List<byte[]> splitFile(Path path, int packageSize) {
        List<byte[]> list = new ArrayList<>();
        byte[] data = new byte[0];
        try {
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] dataTemp;
        if(data.length > packageSize) {
            dataTemp = new byte[packageSize];
        }
        else dataTemp = new byte[data.length];
        int marker = -1;
        for (int i = 0; i < data.length; i++) {
            marker++;
            if (marker == dataTemp.length) {
                list.add(dataTemp);
                marker = 0;
                if(data.length - i < packageSize) {
                    dataTemp = new byte[data.length - i];
                } else dataTemp = new byte[packageSize];
            }
            dataTemp[marker] = data[i];
        }
        list.add(dataTemp);
        return list;
    }

    private static byte[] mergeFile(List<byte[]> list) {
        byte[] newData = new byte[0];
        int  currentLength;
        for (byte[] bytes : list) {
            currentLength = newData.length;
            newData = Arrays.copyOf(newData, newData.length + bytes.length);
            System.arraycopy(bytes, 0, newData, currentLength, bytes.length);
        }
        return newData;
    }
}
