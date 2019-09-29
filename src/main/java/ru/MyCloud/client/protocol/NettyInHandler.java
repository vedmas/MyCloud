package ru.MyCloud.client.protocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import ru.MyCloud.common.FileListMassage;

public class NettyInHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof FileListMassage) {
                System.out.println("Получен список файлов на сервере от сервера");
                FileListMassage flm = (FileListMassage) msg;
                for (String file : flm.getListFile()) {
                    System.out.println("File: " + file);
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
