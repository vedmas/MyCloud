package ru.MyCloud;

import ru.MyCloud.client.protocol.NettyController;
import ru.MyCloud.common.FileMessage;
import ru.MyCloud.common.OrdersNumbers;
import ru.MyCloud.server.ServerInHandler;

import javax.swing.text.html.ListView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final String FILENAME = "client_storage/11.txt";

    public static void main(String[] args) {

//        OrdersNumbers ordersNumbers = new OrdersNumbers();
//        System.out.println("ordersNumbers.getFILE_LIST_ORDER() = " + ordersNumbers.getFILE_LIST_ORDER());

        ServerInHandler serv = new ServerInHandler();
        for (String s : serv.refreshLocalFilesList()) {
            System.out.println(s);
        }
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
}
