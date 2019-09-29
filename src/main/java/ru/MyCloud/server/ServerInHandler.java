package ru.MyCloud.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import javafx.application.Platform;
import ru.MyCloud.common.*;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ServerInHandler extends ChannelInboundHandlerAdapter {
    private final String SERVER_DIRECTORY = "server_storage";
    private OrdersNumbers ordersNumbers;
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
                System.out.println("Объект получен, приступаю к обработке");
                FileMessage obj = (FileMessage) msg;
                Files.write(Paths.get("server_storage/" + obj.getFilename()), obj.getData(), StandardOpenOption.CREATE);
//                if (Files.exists(Paths.get("server_storage/" + fr.getFilename()))) {
//                    FileMessage fm = new FileMessage(Paths.get("server_storage/" + fr.getFilename()));
//                    ctx.writeAndFlush(fm);
//                }
            }
           else if(msg instanceof OrderMessage) {
               Thread.sleep(5);
                OrderMessage order = (OrderMessage) msg;
                if(order.getNumberOrder() == 8008) {
                    System.out.println("Получен запрос на обновление списка файлов на сервере");
                    FileListMassage flm = new FileListMassage(list);
                    ctx.writeAndFlush(flm);
                    System.out.println("Отправлен список файлов на сервере клиенту");
                }
            }
        } finally{
            ReferenceCountUtil.release(msg);
        }
    }

    public List<String> refreshLocalFilesList() {
        List<String> filesList = new ArrayList<>();
        if (Platform.isFxApplicationThread()) {
            try {
                filesList.clear();
                Files.list(Paths.get(SERVER_DIRECTORY)).map(p -> p.getFileName().toString()).forEach(filesList::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    filesList.clear();
                    Files.list(Paths.get(SERVER_DIRECTORY)).map(p -> p.getFileName().toString()).forEach(filesList::add);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return filesList;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
