package com.bh.netty01.netty.client;

import com.bh.netty01.netty.User;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

/**
 * 用来测试服务端的 ObjectDecoder 的客户端
 */
public class MyNettyClient2 {
    public static void main(String[] args) throws InterruptedException {
        final Bootstrap bootstrap = new Bootstrap();

        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(new NioEventLoopGroup());


        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new LoggingHandler());
                        nioSocketChannel.pipeline().addLast(new ObjectEncoder());

                    }
                });


        final ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(9999));

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                final Channel channel = channelFuture.channel();
                // 这个user对象需要序列化
                final User netty = new User("netty", 10);
                channel.writeAndFlush(netty);
            }
        });
    }
}
