package com.bh.netty01.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

public class MyNettyClient {
    public static void main(String[] args) throws InterruptedException {
        final Bootstrap bootstrap = new Bootstrap();

        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(new NioEventLoopGroup());

        /**
         * 站在客户端的角度来看的时候，他就不应该写ServerSocketChannel,他就是用来做IO通信的,只能写SocketChannel
         * 再者来说Bootstrap 是父类， ServerBootstrap 是子类，ServerBootstrap对handler() 做了扩展，只能支持ServerSocketChannel
         */
        /**
         * 首先，通过bootstrap.handler()方法为客户端的ChannelPipeline添加一个ChannelInitializer，这个ChannelInitializer的泛型类型是NioSocketChannel，表示要对客户端的连接通道进行初始化。
         * 在ChannelInitializer的initChannel()方法中，对NioSocketChannel进行初始化配置。这里，首先添加了一个LoggingHandler，用于记录客户端收发数据的日志。
         * 然后，将一个StringEncoder添加到ChannelPipeline中，用于将客户端发送的字符串数据编码成字节流，以便发送给服务端。
         * 通过这段代码，我们可以看到，Netty中的数据处理流程都是通过添加不同的ChannelHandler到ChannelPipeline中来实现的。在这里，通过添加LoggingHandler和StringEncoder，可以对客户端的数据进行日志记录和编码处理，以便发送到服务端。
         */
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new LoggingHandler());

                        /**
                         * 将发送的数据进行编码后发送到服务端
                         */
                        nioSocketChannel.pipeline().addLast(new StringEncoder());

                    }
                });


        /**
         * 00:11:56.116 [nioEventLoopGroup-2-2] DEBUG io.netty.util.ResourceLeakDetector - -Dio.netty.leakDetectionLevel: simple
         * 00:11:56.118 [nioEventLoopGroup-2-2] DEBUG io.netty.util.Recycler - -Dio.netty.recycler.maxCapacity.default: 262144
         * connect() 使用了一个新的线程，用于连接...
         *
         * 如果channelFuture.sync(); 不阻塞，会有获取不到连接的风险
         */
        final ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(9999));
        // 阻塞获取channel
        /**
         * 为什么要做同步，阻塞
         */
        channelFuture.sync();


        /**
         *  bootstrap.connect(new InetSocketAddress(9999)); 新的线程异步进行网络的连接
         */
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                final Channel channel = channelFuture.channel();
                channel.writeAndFlush("hello netty hello netty hello netty");
            }
        });
    }
}
