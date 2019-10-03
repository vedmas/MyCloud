package ru.MyCloud.client;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import ru.MyCloud.common.FileRequest;
import ru.MyCloud.common.OrdersNumbers;

public class NettyNetwork {
    private static NettyNetwork ourInstance = new NettyNetwork();
    OrdersNumbers ordersNumbers = new OrdersNumbers();

    public static NettyNetwork getInstance() {
        return ourInstance;
    }

    private NettyNetwork() {
    }

    private Channel currentChannel;

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void start() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group);
            clientBootstrap.channel(NioSocketChannel.class);
            clientBootstrap.remoteAddress(new InetSocketAddress(ordersNumbers.getHOST(), ordersNumbers.getPORT() ));
            clientBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(
                            new ObjectDecoder(5 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                            new ObjectEncoder(),
                            new NettyInHandler()
                    );
                    currentChannel = socketChannel;
                }
            })
                    .option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnectionOpened() {
        return currentChannel != null && currentChannel.isActive();
    }

    public void closeConnection() {
        currentChannel.close();
        System.exit(0);
    }

}
