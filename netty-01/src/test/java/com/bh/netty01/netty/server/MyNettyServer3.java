package com.bh.netty01.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * <h2>Handler</h2>
 */
@Slf4j
public class MyNettyServer3 {
    public static void main(String[] args) {

        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);

        serverBootstrap.group(new NioEventLoopGroup());


        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {


            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                /**
                 * ChannelInboundHandlerAdapter
                 * 读数据的时候：
                 * handler的调用顺序是
                 * 1-2-3
                 */
                nioSocketChannel.pipeline().addLast("handler1",new StringDecoder());

                nioSocketChannel.pipeline().addLast("handler2",new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        log.info("handler2");
                        System.out.println(msg);
                        super.channelRead(ctx, msg);
                    }
                });
                nioSocketChannel.pipeline().addLast("handler3",new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        log.info("handler3");

                    }
                });
                /**
                 * 如果服务端没有往客户端写数据的时候，不会去调用ChannelOutboundHandlerAdapter
                 * 如果服务端往客户端写数据的时候，调用的顺序与添加handler的顺序是相反的
                 * 6-5-4
                 */
                nioSocketChannel.pipeline().addLast("handler4", new ChannelOutboundHandlerAdapter(){
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        log.info("handler4");
                        super.write(ctx, msg, promise);
                    }
                });
                nioSocketChannel.pipeline().addLast("handler5", new ChannelOutboundHandlerAdapter(){
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        log.info("handler4");
                        super.write(ctx, msg, promise);
                    }
                });
                nioSocketChannel.pipeline().addLast("handler6", new ChannelOutboundHandlerAdapter(){
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                        log.info("handler4");
                        super.write(ctx, msg, promise);
                    }
                });


            }
        });
        serverBootstrap.bind(new InetSocketAddress(9999));

    }
}
