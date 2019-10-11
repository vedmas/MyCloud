package ru.mycloud.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;
import ru.mycloud.FileActions;
import ru.mycloud.PackageFile;
import ru.mycloud.Settings;
import ru.mycloud.message.CommandMessage;
import ru.mycloud.message.FileListMassage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class InHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(InHandler.class);
    private Controller controller;
    private List<PackageFile> list = new ArrayList<>();

    InHandler(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof FileListMassage) {
                ProcessingOnObjectFileListMessage((FileListMassage) msg);
            } else if(msg instanceof PackageFile) {
                ProcessingOnObjectPackageFile(ctx, (PackageFile) msg);
            } else if(msg instanceof CommandMessage) {
                ProcessingOnObjectCommandMessage(ctx, (CommandMessage) msg);
            }
    }
        finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void ProcessingOnObjectCommandMessage(ChannelHandlerContext ctx, CommandMessage msg) {
        if(msg.getNumberOrder() == Settings.RESPONSE_SEND_FILE ||
                msg.getNumberOrder() == Settings.RESPONSE_ORDER_REMOVE_FILE) {
            CommandMessage order = new CommandMessage(Settings.FILE_LIST_ORDER, null);
            ctx.writeAndFlush(order);
        }
        else if(msg.getNumberOrder() == Settings.AUTHORIZATION_PASSED) {
            controller.setAuthorized();
        }
        else if(msg.getNumberOrder() == Settings.AUTHORIZATION_FAILED) {
            controller.setMessage();
        }
    }

    private void ProcessingOnObjectPackageFile(ChannelHandlerContext ctx, PackageFile msg) throws IOException {
        list.add(msg);
        if (msg.isLastPackage()) {
            log.info("The received file from the client");
            Files.write(Paths.get(Settings.CLIENT_DIRECTORY + msg.getFileName()),
                    FileActions.fileRestoredPackets(list), StandardOpenOption.CREATE);
            CommandMessage order = new CommandMessage(Settings.FILE_LIST_ORDER, null);
            ctx.writeAndFlush(order);
            controller.refreshLocalFilesList();
        }
    }

    private void ProcessingOnObjectFileListMessage(FileListMassage msg) {
        log.info("Received a list of files from the server");
        controller.refreshCloudFileList(msg.getListFile());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
