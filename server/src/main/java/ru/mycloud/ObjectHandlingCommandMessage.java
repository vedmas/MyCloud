package ru.mycloud;

import io.netty.channel.ChannelHandlerContext;
import org.apache.log4j.Logger;
import ru.mycloud.message.CommandMessage;
import ru.mycloud.message.FileListMassage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class ObjectHandlingCommandMessage {
    private static final Logger log = Logger.getLogger(ServerInHandler.class);

    static void processingOnObjectCommandMessage(ChannelHandlerContext ctx, CommandMessage msg) {
        if (msg.getNumberOrder() == Settings.RECEIVED_FILE) {
            packetTransmission(ctx, msg);

            //Sending a list of files on the server to the client
        } else if (msg.getNumberOrder() == Settings.FILE_LIST_ORDER) {
            FileListMassage flm = new FileListMassage(refreshLocalFilesList());
            ctx.write(flm);
            //Delete a file in the cloud on customer request
        } else if (msg.getNumberOrder() == Settings.ORDER_REMOVE_FILE) {
            FileActions.fileDeletion(Settings.serverDirectory, msg.getFileName());
            log.info("File " + msg.getFileName() + " remove");
            CommandMessage om = new CommandMessage(Settings.RESPONSE_ORDER_REMOVE_FILE, null);
            ctx.writeAndFlush(om);
        }
    }

    private static void packetTransmission(ChannelHandlerContext ctx, CommandMessage msg) {
        String path = Settings.serverDirectory + File.separator + msg.getFileName();
        int marker = 0;
        try {
            byte[] data = Files.readAllBytes(Paths.get(path));
            byte[] dataTemp = new byte[Settings.PACKAGE_SIZE];
            if (data.length < Settings.PACKAGE_SIZE) {
                dataTemp = new byte[data.length];
            }
            for (int i = 0; i < data.length; i++) {
                dataTemp[marker] = data[i];
                marker++;
                if (marker == dataTemp.length) {
                    ctx.writeAndFlush(new PackageFile(Paths.get(path), false, dataTemp));
                    marker = 0;
                    if (data.length - i < Settings.PACKAGE_SIZE) {
                        dataTemp = new byte[data.length - i];
                    } else {
                        dataTemp = new byte[Settings.PACKAGE_SIZE];
                    }
                }
            }
            ctx.writeAndFlush(new PackageFile(Paths.get(path), true, dataTemp));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Save all file names in the cloud to the list
    private static List<String> refreshLocalFilesList() {
        List<String> filesList = new ArrayList<>();
        try {
            Files.list(Paths.get(Settings.serverDirectory)).map(p -> p.getFileName().toString()).forEach(filesList::add);
        } catch (IOException e) {
            log.info("Method refreshLocalFilesList error: " + e.getMessage());
            e.printStackTrace();
        }
        return filesList;
    }
}
