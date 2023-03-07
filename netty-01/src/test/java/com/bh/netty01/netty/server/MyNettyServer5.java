package com.bh.netty01.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * <h2>Codec 编解码</h2>
 * <h3>ObjectDecoder</h3>
 */
@Slf4j
public class MyNettyServer5 {
    public static void main(String[] args) {

        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);

        serverBootstrap.group(new NioEventLoopGroup());

        serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,new AdaptiveRecvByteBufAllocator(16,16,16));

        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                nioSocketChannel.pipeline().addLast(new LoggingHandler());
                // ObjectDecoder 间接继承了ByteToMessageDecoder ，所以他自己就有解决 封帧的能力
                nioSocketChannel.pipeline().addLast("handler1",new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                nioSocketChannel.pipeline().addLast("handler2",new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        log.info("handler2");
                        System.out.println(msg);
                       // super.channelRead(ctx, msg);
                    }
                });
            }
        });
        serverBootstrap.bind(new InetSocketAddress(9999));

    }
}
