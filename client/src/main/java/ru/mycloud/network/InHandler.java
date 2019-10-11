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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class InHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(InHandler.class);
    private Settings settings = new Settings();
    private FileActions fileActions = new FileActions();
    private Controller controller;
    private List<PackageFile> list = new ArrayList<>();

    InHandler(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            //Processing the list of files on the server
            if(msg instanceof FileListMassage) {
                log.info("Получен список файлов от сервера");
                FileListMassage flm = (FileListMassage) msg;
                controller.refreshCloudFileList(flm.getListFile());
            }
            //Processing a file received from the cloud
            else if(msg instanceof PackageFile) {
                PackageFile pf = (PackageFile) msg;
                list.add(pf);
                if (pf.isLastPackage()) {
                    log.info("Получен файл от клиента");
                    Files.write(Paths.get(Controller.CLIENT_DIRECTORY + pf.getFileName()),
                            fileActions.fileRestoredPackets(list), StandardOpenOption.CREATE);
                    CommandMessage order = new CommandMessage(Settings.FILE_LIST_ORDER, null);
                    ctx.writeAndFlush(order);
                    controller.refreshLocalFilesList();
                }
            }

            //Request a list of files in the cloud
            else if(msg instanceof CommandMessage) {
                CommandMessage om = (CommandMessage) msg;
                if(om.getNumberOrder() == Settings.RESPONSE_SEND_FILE ||
                        om.getNumberOrder() == Settings.RESPONSE_ORDER_REMOVE_FILE) {
                    CommandMessage order = new CommandMessage(Settings.FILE_LIST_ORDER, null);
                    ctx.writeAndFlush(order);
                }
                else if(om.getNumberOrder() == Settings.AUTHORIZATION_PASSED) {
                    controller.setAuthorized(true);
                }
                else if(om.getNumberOrder() == Settings.AUTHORIZATION_FAILED) {
                    controller.setMessage();
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
