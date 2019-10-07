package ru.MyCloud.client;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.log4j.Logger;
import ru.MyCloud.common.OrdersNumbers;

public class Network {
    private static final Logger log = Logger.getLogger(Network.class);
    private static Network ourInstance = new Network();
    private OrdersNumbers ordersNumbers = new OrdersNumbers();

    static Network getInstance() {
        return ourInstance;
    }

    private Network() {
    }

    private Channel currentChannel;

    Channel getCurrentChannel() {
        return currentChannel;
    }

    void start(Controller controller) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress(ordersNumbers.getHOST(), ordersNumbers.getPORT() ));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new ObjectDecoder(500 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                            new ObjectEncoder(),
                            new ChunkedWriteHandler(),
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
        currentChannel.close();
        System.exit(0);
    }

}
