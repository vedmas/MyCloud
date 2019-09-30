package ru.MyCloud.client.protocol;

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

    public void sendData(FileRequest obg) {
        ByteBufAllocator allocator = new PooledByteBufAllocator();
        ByteBuf buf = allocator.buffer(16);
        for (int i = 65; i < 75; i++) {
            for (int j = 0; j < 4; j++) {
                if (buf.isWritable()) {
                    buf.writeByte(i);
                } else {
                    try {
                        currentChannel.writeAndFlush(buf).await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    buf.clear();
                    buf.retain();
                }
            }
        }
//        buf.writeByte(15);
//
//        buf.writeLong(8L);
//
//
//
//        for (byte b : "test.txt".getBytes()) {
//            buf.writeByte(b);
//        }
//
//        currentChannel.writeAndFlush(buf);
    }

    public boolean isConnectionOpened() {
        return currentChannel != null && currentChannel.isActive();
    }

    public void closeConnection() {
        currentChannel.close();
    }
}
