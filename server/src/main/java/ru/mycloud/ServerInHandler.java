package ru.mycloud;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;
import ru.mycloud.message.AuthMessage;
import ru.mycloud.message.CommandMessage;

public class ServerInHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(ServerInHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg == null) {
                log.error("Msg is empty");
            } else if (msg instanceof PackageFile) {
                ObjectHandlingPackageFile.processingOnObjectPackageFile(ctx, (PackageFile) msg);
            } else if (msg instanceof CommandMessage) {
                ObjectHandlingCommandMessage.processingOnObjectCommandMessage(ctx, (CommandMessage) msg);
            } else if (msg instanceof AuthMessage) {
                ObjectHandlingAuthMessage.processingOnObjectAuthMessage(ctx, (AuthMessage) msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
