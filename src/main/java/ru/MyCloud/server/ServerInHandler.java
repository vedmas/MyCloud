package ru.MyCloud.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import ru.MyCloud.common.*;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ServerInHandler extends ChannelInboundHandlerAdapter {
    private final String SERVER_DIRECTORY = "server_storage";
    private OrdersNumbers ordersNumbers = new OrdersNumbers();
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        List<String> list = new ArrayList<>();
        list.add("It works");
        try {
            if (msg == null) {
                System.out.println("Msg is empty");
                return;
            }
            if (msg instanceof FileMessage) {
                System.out.println("Получен запрос на передачу файла на сервер");
                FileMessage obj = (FileMessage) msg;
                Files.write(Paths.get(  SERVER_DIRECTORY + "/" + obj.getFilename()), obj.getData(), StandardOpenOption.CREATE);
            }
           else if(msg instanceof OrderMessage) {
               Thread.sleep(5);
                OrderMessage order = (OrderMessage) msg;
                if(order.getNumberOrder() == ordersNumbers.getRECEIVED_FILE()) {
                    System.out.println("Получен запрос на передачу файла клиенту");
                    FileMessage sendObject = new FileMessage(Paths.get(SERVER_DIRECTORY  + "/" + order.getFileName()));
//                    ctx.writeAndFlush(sendObject);
                }
                else if(order.getNumberOrder() == ordersNumbers.getFILE_LIST_ORDER()) {
                    System.out.println("Получен запрос на обновление списка файлов на сервере");
                    FileListMassage flm = new FileListMassage(refreshLocalFilesList());
                    ctx.write(flm);  // Не работает отправка ответа на клиент
                    System.out.println("Отправлен список файлов на сервере клиенту");
                }
                else if(order.getNumberOrder() == ordersNumbers.getORDER_REMOVE_FILE()) {
                    System.out.println("Получен запрос на удаление файла на сервере");
                    ordersNumbers.fileDeletion(SERVER_DIRECTORY, order.getFileName());
                    System.out.println("Файл " + order.getFileName() + " удален");

                }
            }
        }
        finally{
            ReferenceCountUtil.release(msg);
        }
    }

        private List<String> refreshLocalFilesList() {
            List<String> filesList = new ArrayList<>();
            try {
                Files.list(Paths.get(SERVER_DIRECTORY)).map(p -> p.getFileName().toString()).forEach(filesList::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return filesList;
        }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
