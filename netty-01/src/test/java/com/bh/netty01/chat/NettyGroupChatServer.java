package com.bh.netty01.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * <h1>案例要求:</h1>
 * <ol>
 *     <li>编写一个 Netty 群聊系统，实现服务器端和客户端之间的数据简单通讯</li>
 *     <li>实现多人群聊</li>
 *     <li>服务器端:可以监测用户上线，离线，并实现消息转发功能</li>
 *     <li>客户端:可以发送消息给其它所有用户，同时可以接受其它用户发送的消息</li>
 * </ol>
 */
@Slf4j
public class NettyGroupChatServer {
    private int port;
    private  final EventLoopGroup BOSS_EVENT_LOOP_GROUP = new NioEventLoopGroup(1);
    private  final EventLoopGroup WORKER_EVENT_LOOP_GROUP = new NioEventLoopGroup(3);
    private  final ServerBootstrap SERVER_BOOTSTRAP = new ServerBootstrap();



    public NettyGroupChatServer(int port) {
        this.port = port;
    }

    public void run() {
        log.info("netty 服务器正在启动...");
        try {
            SERVER_BOOTSTRAP.group(BOSS_EVENT_LOOP_GROUP,WORKER_EVENT_LOOP_GROUP)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                                nioSocketChannel.pipeline()
                                        .addLast("decoder",new StringDecoder())
                                        .addLast("encoder",new StringEncoder())
                                        .addLast(new NettyGroupChatServerHandler());
                        }
                    }).bind(port);
            log.info("netty 服务器启动完成~");

        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        new NettyGroupChatServer(9999).run();
    }

}
