package ru.MyCloud;

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
        Path path = Paths.get("./client_storage/5.txt");
        byte[] data = new byte[5];
            try {
                data = Files.readAllBytes(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int[] n1 = new int[] {1, 2, 3};
            int[] n2 = new int[] {4, 5};
            n2 = Arrays.copyOf(n2, n1.length + n2.length);
        System.arraycopy(n1, 0, n2, 2, n1.length);
        System.out.println("n2.length = " + n2.length);
        for (int i : n2) {
            System.out.println(i);
        }



//            Files.write(Paths.get("./client_storage/6.txt"), data, StandardOpenOption.CREATE);

    }

    private static List<byte[]> splitFile(byte[] data) {
        List<byte[]> list = new ArrayList<>();
        byte[] dataTemp = new byte[5];
        int marker = -1;
        for (byte datum : data) {
            marker++;
            if (marker == dataTemp.length) {
                list.add(dataTemp);
                marker = 0;
                dataTemp = new byte[5];
            }
            dataTemp[marker] = datum;

        }
        list.add(dataTemp);
        return list;
    }

    private static byte[] mergeFile(List<byte[]> list) {
        int marker = 0;
        byte[] newData = new byte[5];
            for (byte[] bytes : list) {
                if(marker == newData.length -1) {

                }
                    for (byte aByte : bytes) {
                        newData[marker] = aByte;
                        marker++;
                    }

            }


        return newData;
    }
}
