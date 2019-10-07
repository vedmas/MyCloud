package ru.MyCloud.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.log4j.Logger;
import ru.MyCloud.common.OrdersNumbers;

public class Server {
    private static final Logger log = Logger.getLogger(Server.class);

    private void run() throws Exception {
        new OrdersNumbers().createDirectory(new ServerInHandler().getSERVER_DIRECTORY());
        EventLoopGroup mainGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        OrdersNumbers ordersNumbers = new OrdersNumbers();
        try {
            ServerBootstrap b = new ServerBootstrap();
            log.info("Start server!");
            b.group(mainGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new ObjectDecoder(500 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new ChunkedWriteHandler(),
                                    new ServerInHandler()
                            );
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = b.bind(ordersNumbers.getPORT()).sync();
            future.channel().closeFuture().sync();
        }
        finally {
            mainGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new Server().run();
    }
}
