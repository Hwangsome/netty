package com.bh.netty01.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyUploadFileServer {

    private int port;

    private static final NioEventLoopGroup BOSS_NIO_EVENT_LOOP_GROUP = new NioEventLoopGroup(1);
    private static final NioEventLoopGroup WORKER_NIO_EVENT_LOOP_GROUP = new NioEventLoopGroup();

    public NettyUploadFileServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        new NettyUploadFileServer(9999).run();
    }

    private void run() {
        final ServerBootstrap serverBootstrap = new ServerBootstrap();

        serverBootstrap.group(BOSS_NIO_EVENT_LOOP_GROUP,WORKER_NIO_EVENT_LOOP_GROUP)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline();
                    }
                })
                .bind(this.port);
    }
}
