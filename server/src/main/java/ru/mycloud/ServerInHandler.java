package ru.mycloud;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;
import ru.mycloud.message.AuthMessage;
import ru.mycloud.message.CommandMessage;
import ru.mycloud.message.FileListMassage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ServerInHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(ServerInHandler.class);

    private AuthService authService = new AuthService();
    private List<PackageFile> list = new ArrayList<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                log.error("Msg is empty");
            } else if (msg instanceof PackageFile) {
                ProcessingOnObjectPackageFile(ctx, (PackageFile) msg);
            } else if (msg instanceof CommandMessage) {
                ProcessingOnObjectCommandMessage(ctx, (CommandMessage) msg);
            } else if (msg instanceof AuthMessage) {
                ProcessingOnObjectAuthMessage(ctx, (AuthMessage) msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void ProcessingOnObjectAuthMessage(ChannelHandlerContext ctx, AuthMessage msg) {
        if (authService.isAuthorization(msg)) {
            CommandMessage om = new CommandMessage(Settings.AUTHORIZATION_PASSED, null);
            ctx.writeAndFlush(om);
            log.info("Authorization passed");
        } else {
            CommandMessage om = new CommandMessage(Settings.AUTHORIZATION_FAILED, null);
            ctx.writeAndFlush(om);
            log.info("Authorization failed");
        }
    }

    private void ProcessingOnObjectCommandMessage(ChannelHandlerContext ctx, CommandMessage msg) {
        if (msg.getNumberOrder() == Settings.RECEIVED_FILE) {
            for (PackageFile packageFile : FileActions.createListPackage(Paths.get(Settings.SERVER_DIRECTORY + File.separator + msg.getFileName()))) {
                ctx.writeAndFlush(packageFile);
            }
            //Sending a list of files on the server to the client
        } else if (msg.getNumberOrder() == Settings.FILE_LIST_ORDER) {
            FileListMassage flm = new FileListMassage(refreshLocalFilesList());
            ctx.write(flm);
            //Delete a file in the cloud on customer request
        } else if (msg.getNumberOrder() == Settings.ORDER_REMOVE_FILE) {
            FileActions.fileDeletion(Settings.SERVER_DIRECTORY, msg.getFileName());
            log.info("Файл " + msg.getFileName() + " удален");
            CommandMessage om = new CommandMessage(Settings.RESPONSE_ORDER_REMOVE_FILE, null);
            ctx.writeAndFlush(om);
        }
    }

    private void ProcessingOnObjectPackageFile(ChannelHandlerContext ctx, PackageFile msg) throws IOException {
//        list.add(msg);
        try(FileOutputStream fos = new FileOutputStream(Settings.SERVER_DIRECTORY + File.separator + msg.getFileName(), true);
        BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(msg.getDataPackage(), 0, msg.getDataPackage().length);

        } catch (IOException e) {
            e.printStackTrace();
            log.info(e.getMessage());
        }
        if (msg.isLastPackage()) {
            log.info("Получен файл от клиента");

//            Files.write(Paths.get(Settings.SERVER_DIRECTORY + File.separator + msg.getFileName()),
//                    FileActions.fileRestoredPackets(list), StandardOpenOption.CREATE);
//            list.clear();
            CommandMessage om = new CommandMessage(Settings.RESPONSE_SEND_FILE, null);
            ctx.writeAndFlush(om);
        }
    }

    //Save all file names in the cloud to the list
    private List<String> refreshLocalFilesList() {
        List<String> filesList = new ArrayList<>();
        try {
            Files.list(Paths.get(Settings.SERVER_DIRECTORY)).map(p -> p.getFileName().toString()).forEach(filesList::add);
        } catch (IOException e) {
            log.info("Method refreshLocalFilesList error: " + e.getMessage());
            e.printStackTrace();
        }
        return filesList;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
