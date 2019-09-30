package ru.MyCloud;

import ru.MyCloud.common.OrdersNumbers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String FILENAME = "client_storage/11.txt";

    public static void main(String[] args) {

        OrdersNumbers ordersNumbers = new OrdersNumbers();
        ordersNumbers.fileDeletion("server_storage", "1.txt");

//        for (String s : refreshLocalFilesList()) {
//            System.out.println(s);
//
//        }

//        OrdersNumbers ordersNumbers = new OrdersNumbers();
//        System.out.println("ordersNumbers.getFILE_LIST_ORDER() = " + ordersNumbers.getFILE_LIST_ORDER());



//        try {
//            FileMessage obj = new FileMessage(Paths.get(FILENAME));
//            System.out.println("obj.getFilename() = " + obj.getFilename());
//            System.out.println("obj.getData().length = " + obj.getData().length);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        try {
//            final File file = new File(FILENAME);
//            final FileInputStream fis = new FileInputStream(FILENAME);
//            byte[] bytes = new byte[(int) file.length()];
//            fis.read(bytes);
//            fis.close();
//            System.out.println("> Нaчaлo фaйлa <");
//            for (byte aByte : bytes) {
//                System.out.print((char) aByte);
//            }
//            System.out.println("bytes.length = " + bytes.length);
//            System.out.println();
//            System.out.println("> Koнeц фaйлa <");
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        byte[] mas = new NettyController().convertingFileToByteArray(FILENAME);
//        for (byte ma : mas) {
//            System.out.print((char) ma);
//        }

    }

    public static List<String> refreshLocalFilesList() {
        List<String> filesList = new ArrayList<>();
            try {
                filesList.clear();
                Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(filesList::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        return filesList;
    }
}
