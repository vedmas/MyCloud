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
import java.util.ArrayList;
import java.util.List;

public class ServerInHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(ServerInHandler.class);

    private AuthService authService = new AuthService();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
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
            String path = Settings.SERVER_DIRECTORY + File.separator + msg.getFileName();
            int marker = 0;
            try {
                byte[] data = Files.readAllBytes(Paths.get(path));
                byte[] dataTemp = new byte[Settings.PACKAGE_SIZE];
                if(data.length < Settings.PACKAGE_SIZE) {
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

            //Sending a list of files on the server to the client
        } else if (msg.getNumberOrder() == Settings.FILE_LIST_ORDER) {
            FileListMassage flm = new FileListMassage(refreshLocalFilesList());
            ctx.write(flm);
            //Delete a file in the cloud on customer request
        } else if (msg.getNumberOrder() == Settings.ORDER_REMOVE_FILE) {
            FileActions.fileDeletion(Settings.SERVER_DIRECTORY, msg.getFileName());
            log.info("File " + msg.getFileName() + " remove");
            CommandMessage om = new CommandMessage(Settings.RESPONSE_ORDER_REMOVE_FILE, null);
            ctx.writeAndFlush(om);
        }
    }

    private void ProcessingOnObjectPackageFile(ChannelHandlerContext ctx, PackageFile msg) {
        try(FileOutputStream fos = new FileOutputStream(Settings.SERVER_DIRECTORY + File.separator + msg.getFileName(), true);
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
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
