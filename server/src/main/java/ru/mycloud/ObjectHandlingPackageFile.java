package ru.mycloud;

import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import ru.mycloud.message.CommandMessage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class ObjectHandlingPackageFile {
    private static final Logger log = Logger.getLogger(ServerInHandler.class);

    static void processingOnObjectPackageFile(ChannelHandlerContext ctx, PackageFile msg) {
        try (FileOutputStream fos = new FileOutputStream(Settings.serverDirectory + File.separator + msg.getFileName(), true);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(msg.getDataPackage(), 0, msg.getDataPackage().length);
        } catch (IOException e) {
            e.printStackTrace();
            log.info(e.getMessage());
        }
        if (msg.isLastPackage()) {
            log.info("The received file from the client");
            CommandMessage om = new CommandMessage(Settings.RESPONSE_SEND_FILE, null);
            ctx.writeAndFlush(om);
        }
    }
}
