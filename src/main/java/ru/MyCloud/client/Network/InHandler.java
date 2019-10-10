package ru.MyCloud.client.Network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;
import ru.MyCloud.common.*;
import ru.MyCloud.common.Message.AbstractMessage;
import ru.MyCloud.common.Message.OrderMessage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class InHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(InHandler.class);
    private Settings settings = new Settings();
    private FileActions fileActions = new FileActions();
    private Controller controller;
    private List<PackageFile> list = new ArrayList<>();

    public InHandler(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //Обработка полученных сообщений от сервера
        try {
            //Обработка информации о файлах на сервере
            if(msg instanceof AbstractMessage.FileListMassage) {
                log.info("Получен список файлов от сервера");
                AbstractMessage.FileListMassage flm = (AbstractMessage.FileListMassage) msg;
                controller.refresh(flm.getListFile());
            }
            //Получаем и сохраняем файл запрошенный у облака
            else if(msg instanceof PackageFile) {
                PackageFile pf = (PackageFile) msg;
                list.add(pf);
                if (pf.isLastPackage()) {
                    log.info("Получен файл от клиента");
                    Files.write(Paths.get(controller.getCLIENT_DIRECTORY() + pf.getFileName()),
                            fileActions.fileRestoredPackets(list), StandardOpenOption.CREATE);
                    OrderMessage order = new OrderMessage(settings.getFILE_LIST_ORDER(), null);
                    ctx.writeAndFlush(order);
                    controller.refreshLocalFilesList();
                }
            }

            //Запрашиваем информацию о файлах на сервере
            else if(msg instanceof OrderMessage) {
                OrderMessage om = (OrderMessage) msg;
                if(om.getNumberOrder() == settings.getRESPONSE_SEND_FILE() ||
                        om.getNumberOrder() == settings.getRESPONSE_ORDER_REMOVE_FILE()) {
                    OrderMessage order = new OrderMessage(settings.getFILE_LIST_ORDER(), null);
                    ctx.writeAndFlush(order);
                }
                else if(om.getNumberOrder() == settings.getAUTHORIZATION_PASSED()) {
                    controller.setAuthorized(true);
                }
                else if(om.getNumberOrder() == settings.getAUTHORIZATION_FAILED()) {
                    controller.setMessage("Login or password incorrect");
                }
            }
    }
        finally {
            ReferenceCountUtil.release(msg);
        }
        }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
