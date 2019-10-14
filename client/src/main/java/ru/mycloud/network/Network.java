package ru.mycloud.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.log4j.Logger;
import ru.mycloud.Settings;

import java.net.InetSocketAddress;

public class Network {
    private static final Logger log = Logger.getLogger(Network.class);
    private static Network ourInstance = new Network();
    private static EventLoopGroup group;

    static Network getInstance() {
        return ourInstance;
    }

    public Network() {
    }

    private Channel currentChannel;

    Channel getCurrentChannel() {
        return currentChannel;
    }

    void start(Controller controller) {
        group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress(Settings.HOST, Settings.PORT));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) {
                    socketChannel.pipeline().addLast(
                            new ObjectDecoder(Settings.OBJECT_SIZE_FOR_DECODER, ClassResolvers.cacheDisabled(null)),
                            new ObjectEncoder(),
                            new InHandler(controller)
                    );
                    currentChannel = socketChannel;
                }
            })
                    .option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("Error client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                log.error("Error client: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    boolean isConnectionOpened() {
        return currentChannel != null && currentChannel.isActive();
    }

    public void closeConnection() {
        if (isConnectionOpened()) {
            currentChannel.close();
        }
        if (group != null) {
            group.shutdownGracefully();
        }
        System.exit(0);
    }

}
