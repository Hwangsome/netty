package com.bh.netty01.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * <h2>Codec 编解码</h2>
 * <h3>StringDecoder</h3>
 * <p>
 *     StringDecoder 继承了MessageToMessageDecoder，不具有解决封帧的能力
 * </p>
 * <pre>
 *     <code>
 *     public class StringDecoder extends MessageToMessageDecoder<ByteBuf> {
 *
 *          protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
 *              out.add(msg.toString(this.charset));
 *              }
 * }
 *     </code>
 * </pre>
 */
@Slf4j
public class MyNettyServer4 {
    public static void main(String[] args) {

        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);

        serverBootstrap.group(new NioEventLoopGroup());
        /**
         * 为了测试StringDecoder 没有解决封帧的能力，这里把接收的ByteBuf设小
         */
        serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,new AdaptiveRecvByteBufAllocator(16,16,16));

        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                nioSocketChannel.pipeline().addLast(new LoggingHandler());
                nioSocketChannel.pipeline().addLast("handler1",new StringDecoder());
                // 这里需要再加一个解决封帧 的handler
                nioSocketChannel.pipeline().addLast("handler2",new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        log.info("handler2");
                        System.out.println(msg);
                        super.channelRead(ctx, msg);
                    }
                });
            }
        });
        serverBootstrap.bind(new InetSocketAddress(9999));

    }
}
