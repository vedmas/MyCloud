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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class InHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(InHandler.class);
    private Controller controller;

    InHandler(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
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
            Settings.clientDirectory = controller.getCurrentUser() + File.separator + Settings.USER_DIRECTORY_CLIENT;
            FileActions.createDirectory(Settings.clientDirectory);
            controller.starterRefreshFilesLists();
            controller.setUIListeners();
            controller.setAuthorized();
        }
        else if(msg.getNumberOrder() == Settings.AUTHORIZATION_FAILED) {
            controller.setMessage();
        }
    }

    private void ProcessingOnObjectPackageFile(ChannelHandlerContext ctx, PackageFile msg) {
        try(FileOutputStream fos = new FileOutputStream(Settings.clientDirectory + msg.getFileName(), true);
            BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(msg.getDataPackage(), 0, msg.getDataPackage().length);
        } catch (IOException e) {
            e.printStackTrace();
            log.info(e.getMessage());
        }
        if (msg.isLastPackage()) {
            log.info("The received file from the client");
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
