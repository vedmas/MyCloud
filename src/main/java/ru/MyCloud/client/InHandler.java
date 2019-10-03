package ru.MyCloud.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;
import ru.MyCloud.common.FileListMassage;
import ru.MyCloud.common.FileMessage;
import ru.MyCloud.common.OrderMessage;
import ru.MyCloud.common.OrdersNumbers;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class InHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(InHandler.class);
    private OrdersNumbers ordersNumbers = new OrdersNumbers();
    private Controller controller;

    public InHandler(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //Обработка полученных сообщений от сервера
        try {
            //Обработка информации о файлах на сервере
            if(msg instanceof FileListMassage) {
                log.info("Получен список файлов от сервера");
                FileListMassage flm = (FileListMassage) msg;
                controller.refresh(flm.getListFile());
            }
            //Получаем и сохраняем файл запрошенный у облака
            else if(msg instanceof FileMessage) {
                log.info("Получен запрашиваемый файл от сервера");
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get(   controller.getCLIENT_DIRECTORY() + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                OrderMessage order = new OrderMessage(ordersNumbers.getFILE_LIST_ORDER(), null);
                ctx.writeAndFlush(order);
                controller.refreshLocalFilesList();
            }
            //Запрашиваем информацию о файлах на сервере
            else if(msg instanceof OrderMessage) {
                OrderMessage om = (OrderMessage) msg;
                if(om.getNumberOrder() == ordersNumbers.getRESPONSE_SEND_FILE() ||
                        om.getNumberOrder() == ordersNumbers.getRESPONSE_ORDER_REMOVE_FILE()) {
                    OrderMessage order = new OrderMessage(ordersNumbers.getFILE_LIST_ORDER(), null);
                    ctx.writeAndFlush(order);
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
