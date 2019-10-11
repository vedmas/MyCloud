package ru.mycloud;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.apache.log4j.Logger;
import ru.mycloud.message.AuthMessage;
import ru.mycloud.message.CommandMessage;
import ru.mycloud.message.FileListMassage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class ServerInHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = Logger.getLogger(ServerInHandler.class);

    public static final String SERVER_DIRECTORY = "server_storage";
    private Settings settings = new Settings();
    private AuthService authService = new AuthService();
    private FileActions fileActions = new FileActions();
    private List<PackageFile> list = new ArrayList<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        try {
            //Если вдруг сообщение от клиента пустое, то ничего не делаем
            if (msg == null) {
                log.error("Msg is empty");
            }
             else if (msg instanceof PackageFile) {
                PackageFile pf = (PackageFile) msg;
                list.add(pf);
                System.out.println("pf.isLastPackage() = " + pf.isLastPackage());
                if(pf.isLastPackage()) {
                    log.info("Получен файл от клиента");
                    Files.write(Paths.get(  SERVER_DIRECTORY + "/" + pf.getFileName()),
                            fileActions.fileRestoredPackets(list), StandardOpenOption.CREATE);
                    list.clear();
                    CommandMessage om = new CommandMessage(Settings.RESPONSE_SEND_FILE, null);
                    ctx.writeAndFlush(om);
                }
            }

            //Sending a file to a client
           else if(msg instanceof CommandMessage) {
                CommandMessage order = (CommandMessage) msg;
                if(order.getNumberOrder() == Settings.RECEIVED_FILE) {
                    for (PackageFile packageFile : fileActions.createListPackage(Paths.get(SERVER_DIRECTORY  + "/" + order.getFileName()))) {
                        ctx.writeAndFlush(packageFile);
                    }
                }

                //Sending a list of files on the server to the client
                else if(order.getNumberOrder() == Settings.FILE_LIST_ORDER) {
                    FileListMassage flm = new FileListMassage(refreshLocalFilesList());
                    ctx.write(flm);
                }
                //Delete a file in the cloud on customer request
                else if(order.getNumberOrder() == Settings.ORDER_REMOVE_FILE) {
                    fileActions.fileDeletion(SERVER_DIRECTORY, order.getFileName());
                    log.info("Файл " + order.getFileName() + " удален");
                    CommandMessage om = new CommandMessage(Settings.RESPONSE_ORDER_REMOVE_FILE, null);
                    ctx.writeAndFlush(om);
                }
            }
           else if(msg instanceof AuthMessage) {
               AuthMessage am = (AuthMessage) msg;
               if(authService.isAuthorization(am)) {
                   CommandMessage om = new CommandMessage(Settings.AUTHORIZATION_PASSED, null);
                   ctx.writeAndFlush(om);
                   log.info("Authorization passed");
               }
               else {
                   CommandMessage om = new CommandMessage(Settings.AUTHORIZATION_FAILED, null);
                   ctx.writeAndFlush(om);
                   log.info("Authorization failed");
               }
            }
        }
        finally{
            ReferenceCountUtil.release(msg);
        }
    }
    //Save all file names in the cloud to the list
        private List<String> refreshLocalFilesList() {
            List<String> filesList = new ArrayList<>();
            try {
                Files.list(Paths.get(SERVER_DIRECTORY)).map(p -> p.getFileName().toString()).forEach(filesList::add);
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
