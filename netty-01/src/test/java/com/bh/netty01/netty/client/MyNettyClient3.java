package com.bh.netty01.netty.client;

import com.bh.netty01.netty.Teacher;
import com.bh.netty01.netty.User;
import com.google.gson.Gson;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * 用来测试服务端的 JsonObjectDecoder 的客户端
 */
@Slf4j
public class MyNettyClient3 {
    public static void main(String[] args) throws InterruptedException {
        final Bootstrap bootstrap = new Bootstrap();

        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(new NioEventLoopGroup());


        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new LoggingHandler());
                        //nioSocketChannel.pipeline().addLast(new StringEncoder());

                    }
                });


        final ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(9999));

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                final Channel channel = channelFuture.channel();
                final Teacher netty = new Teacher("netty", 10);
                final Gson gson = new Gson();
                final String teacherJson = gson.toJson(netty, Teacher.class);
                ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
                buffer.writeCharSequence(teacherJson, Charset.defaultCharset());
                channel.writeAndFlush(buffer);
            }
        });
    }
}
