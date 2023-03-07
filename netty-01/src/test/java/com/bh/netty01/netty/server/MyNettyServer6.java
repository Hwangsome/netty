package com.bh.netty01.netty.server;

import com.bh.netty01.netty.Teacher;
import com.google.gson.Gson;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * <h2>Codec 编解码</h2>
 * <h3>JsonObjectDecoder</h3>
 * <p>
 *     JsonObjectDecoder 也是继承了ByteToMessageDecoder ，所以他也有解决封帧的能力
 * </p>
 */
@Slf4j
public class MyNettyServer6 {
    public static void main(String[] args) {

        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);

        serverBootstrap.group(new NioEventLoopGroup());
       
        serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,new AdaptiveRecvByteBufAllocator(16,16,16));

        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {

                // JsonObjectDecoder 间接继承了ByteToMessageDecoder ，所以他自己就有解决 封帧的能力
                nioSocketChannel.pipeline().addLast("handler1",new JsonObjectDecoder());
                nioSocketChannel.pipeline().addLast(new LoggingHandler());
                nioSocketChannel.pipeline().addLast("handler2",new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        log.info("handler2");
                        System.out.println(msg);
                        final Gson gson = new Gson();
                        ByteBuf byteBuf = (ByteBuf)msg;
                        final String teacherJson = byteBuf.toString(Charset.defaultCharset());
                        final Teacher teacher = gson.fromJson(teacherJson, Teacher.class);
                        log.info("teacher:\t"+teacher);
                        // super.channelRead(ctx, msg);
                    }
                });
            }
        });
        serverBootstrap.bind(new InetSocketAddress(9999));

    }
}
