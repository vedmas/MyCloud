package ru.MyCloud.server;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import io.netty.buffer.ByteBuf;
import ru.MyCloud.common.FileMessage;
import ru.MyCloud.common.FileRequest;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class ServerInHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ByteBuf in = (ByteBuf) msg;
        try {
            if (msg == null) {
                System.out.println("Msg is empty");
                return;
            }
            if (msg instanceof FileRequest) {
                System.out.println("Объект получен, приступаю к обработке");
                FileRequest fr = (FileRequest) msg;
//                if (Files.exists(Paths.get("server_storage/" + fr.getFilename()))) {
//                    FileMessage fm = new FileMessage(Paths.get("server_storage/" + fr.getFilename()));
//                    ctx.writeAndFlush(fm);
//                }
            }
            else {
                System.out.println("the received message does not belong to the FileRequest class.");
//                while (in.isReadable()) {
//                    System.out.print((char) in.readByte());
//                    }
            }
        } finally{
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
