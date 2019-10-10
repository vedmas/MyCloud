package ru.MyCloud.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import ru.MyCloud.client.AuthController;
import ru.MyCloud.common.*;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.MyCloud.common.Message.AbstractMessage;
import ru.MyCloud.common.Message.AuthMessage;
import ru.MyCloud.common.Message.OrderMessage;

public class ServerInHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(ServerInHandler.class);

    private final String SERVER_DIRECTORY = "server_storage";
    private Settings settings = new Settings();
    private AuthController authController = new AuthController();
    private FileActions fileActions = new FileActions();
    private List<PackageFile> list = new ArrayList<>();

    String getSERVER_DIRECTORY() {
        return SERVER_DIRECTORY;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        try {
            //Если вдруг сообщение от клиента пустое, то ничего не делаем
            if (msg == null) {
                log.error("Msg is empty");
                return;
            }
             else if (msg instanceof PackageFile) {
                PackageFile pf = (PackageFile) msg;
                list.add(pf);
                System.out.println("pf.isLastPackage() = " + pf.isLastPackage());
                if(pf.isLastPackage()) {
                    log.info("Получен файл от клиента");
                    Files.write(Paths.get(  SERVER_DIRECTORY + "/" + pf.getFileName()),
                            fileActions.fileRestoredPackets(list), StandardOpenOption.CREATE);
                    list.clear();
                    OrderMessage om = new OrderMessage(settings.getRESPONSE_SEND_FILE(), null);
                    ctx.writeAndFlush(om);
                }
            }

            //Отправляем запрошенный файл клиенту
           else if(msg instanceof OrderMessage) {
                OrderMessage order = (OrderMessage) msg;
                if(order.getNumberOrder() == settings.getRECEIVED_FILE()) {
                    for (PackageFile packageFile : fileActions.createListPackage(Paths.get(SERVER_DIRECTORY  + "/" + order.getFileName()))) {
                        ctx.writeAndFlush(packageFile);
                    }
//                    FileMessage sendObject = new FileMessage(Paths.get(SERVER_DIRECTORY  + "/" + order.getFileName()));
//                    ctx.writeAndFlush(sendObject);
                }
                //Отправляем клиенту список файлов хранящихся в облаке
                else if(order.getNumberOrder() == settings.getFILE_LIST_ORDER()) {
                    AbstractMessage.FileListMassage flm = new AbstractMessage.FileListMassage(refreshLocalFilesList());
                    ctx.write(flm);
                }
                //Удаляем файл в облаке по запросу клиента
                else if(order.getNumberOrder() == settings.getORDER_REMOVE_FILE()) {
                    fileActions.fileDeletion(SERVER_DIRECTORY, order.getFileName());
                    log.info("Файл " + order.getFileName() + " удален");
                    OrderMessage om = new OrderMessage(settings.getRESPONSE_ORDER_REMOVE_FILE(), null);
                    ctx.writeAndFlush(om);
                }
            }
           else if(msg instanceof AuthMessage) {
               AuthMessage am = (AuthMessage) msg;
               if(authController.isAuthorization(am)) {
                   OrderMessage om = new OrderMessage(settings.getAUTHORIZATION_PASSED(), null);
                   ctx.writeAndFlush(om);
                   log.info("Authorization passed");
               }
               else {
                   OrderMessage om = new OrderMessage(settings.getAUTHORIZATION_FAILED(), null);
                   ctx.writeAndFlush(om);
                   log.info("Authorization failed");
               }
            }
        }
        finally{
            ReferenceCountUtil.release(msg);
        }
    }
    //Сохраняем все имена файлов хранящихся в облаке в список
        private List<String> refreshLocalFilesList() {
            List<String> filesList = new ArrayList<>();
            try {
                Files.list(Paths.get(SERVER_DIRECTORY)).map(p -> p.getFileName().toString()).forEach(filesList::add);
            } catch (IOException e) {
                log.info("Method refreshLocalFilesList error: " + e.getMessage());
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
