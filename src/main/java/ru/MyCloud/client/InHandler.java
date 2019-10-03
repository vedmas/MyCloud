package ru.MyCloud.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.MyCloud.common.FileListMassage;
import ru.MyCloud.common.FileMessage;
import ru.MyCloud.common.OrderMessage;
import ru.MyCloud.common.OrdersNumbers;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class NettyInHandler extends ChannelInboundHandlerAdapter {
    private OrdersNumbers ordersNumbers = new OrdersNumbers();
    private NettyController nettyController;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof FileListMassage) {
                System.out.println("Получен список файлов от сервера");
                FileListMassage flm = (FileListMassage) msg;
                for (String s : flm.getListFile()) {
                    System.out.println(s);
                }
//                nettyController.refresh(flm.getListFile());
            }
            else if(msg instanceof FileMessage) {
                System.out.println("Получен запрашиваемый файл от сервера");
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get(   nettyController.getCLIENT_DIRECTORY() + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                OrderMessage order = new OrderMessage(ordersNumbers.getFILE_LIST_ORDER(), null);
                ctx.writeAndFlush(order);
//                nettyController.refreshLocalFilesList();
            }
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
