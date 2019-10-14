package ru.mycloud;

import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import ru.mycloud.message.AuthMessage;
import ru.mycloud.message.CommandMessage;

import java.io.File;

class ObjectHandlingAuthMessage {
    private static final Logger log = Logger.getLogger(ServerInHandler.class);

    static void processingOnObjectAuthMessage(ChannelHandlerContext ctx, AuthMessage msg) {
        if (AuthService.isAuthorization(msg)) {
            Settings.serverDirectory = msg.getLogin() + File.separator + Settings.USER_DIRECTORY_SERVER;
            FileActions.createDirectory(Settings.serverDirectory);
            CommandMessage om = new CommandMessage(Settings.AUTHORIZATION_PASSED, null);
            ctx.writeAndFlush(om);
            log.info("Authorization passed");
        } else {
            CommandMessage om = new CommandMessage(Settings.AUTHORIZATION_FAILED, null);
            ctx.writeAndFlush(om);
            log.info("Authorization failed");
        }
    }
}
