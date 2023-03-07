package com.bh.netty01.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

import java.net.InetSocketAddress;

/**
 * <p>Bootstrap：</p>
 * <ol>
 *     <li>ServerBootstrap：Netty封装的 服务端类</li>
 *     <li>Bootstrap： Netty封装的 客户端类</li>
 * </ol>
 *
 * <p>channel:</p>
 * <ol>
 *     <li> NioServerSocketChannel: Netty封装的 服务端的SocketChannel</li>
 *     <li>  NioSocketChannel： Netty封装的 客户端的SocketChannel</li>
 *</ol>
 *
 * <p>EventLoopGroup：(事件 循环 组) （相当于多个selector，一个selector绑定在一个线程中）</p>
 *      <p>EventLoop：(相当于 nio 的selector)</p>
 *         <p> Event：事件 (NIO中的 ACCEPT,READ,WRITE事件)</p>
 *        <p>  Loop：(NIO中的死循环)</p>
 *
 *        <p>  ACCEPT 事件死循环(BOSS 的 一个线程)</p>
 *        <p>  READ,WRITE 事件死循环 (WORKER 的多个线程)</p>
 *
 *       <p>Group：</p>
 *         <p> 一组 BOSS WORKER 线程</p>
 *         <p> 多个线程的管理者</p>
 *
 * <h2>Handler 处理器</h2>
 * <p>当我们监控读写操作后，进行的处理。解码 bytebuffer ===> 字符串。编码 字符串   ===> bytebuffer</p>
 *
 * <h2>业务处理</h2>
 * <p>Netty对于Handler 封装 java设计类的过程中，各司其职</p>
 * <p>解码类：Decoder</p>
 * <p>编码类：Encoder</p>
 * <p>业务处理一个或几个类</p>
 *
 * Pipeline：就是上面的Handler组成了一个Pipeline,一个socket对应一个pipeline.每个请求过来的时候都对应一份pipeline
 *
 *
 * NioEventLoop:
 *  实现SingleThreadEventLoop：将Channel注册到selector并且在事件循环中对这些channel进行多路复用
 *  SingleThreadEventLoop：单线程线程池
 *  EventLoop：注册后，将处理channel的所有i/o操作，一个EventLoop通常会处理多个channel。相当于reactor中的worker
 *       WORKER线程 select==》READ,WRITE
 *       BOSS线程 select ==》ACCEPT
 *
 *  开发中如何使用EventLoop，查看构造方法不是public，表明不会通过构造方法让程序员去创建
 *  那怎么去创建？
 *  EventLoopGroup去创建。
 *
 *  EventLoopGroup：
 *  编程的过程中，开放的编程接口是EventLoopGroup
 *  EventLoopGroup创建EventLoop （一个线程） 多个EventLoop（多个线程）。
 *  管理 EventLoop，也可以任务EventLoopGroup是EventLoop的工厂
 *
 *  DefaultEventLoop: h还可以发现他是一个单线程池
 *      查看run()方法发现很简单，是1个空的线程。内容由后续的开发去设置
 *
 *  NioEventLoopGroup vs DefaultEventLoop
 *  NioEventLoopGroup: 是一个多线程的线程池， IO WRITE READ事件的监控
 *  DefaultEventLoop: 就是一个普通的线程，内容工作可以由程序员决定，他不做io 监控 读写的处理
 *      defaultEventLoop.submit(()-> System.out.println("hello"));
 *
 *  <h2>注意事项：</h2>
 *      <p>EventLoop是会绑定channel,EventLoop可以支持多个channel访问的。
 *      一个EventLoop 就是一个selector,一个selector可以绑定了多个channel</p>
 *
 *  <h2>服务端实现步骤:</h2>
 *  <ol>
 *     <li>创建bossGroup线程组: 处理网络事件--连接事件</li>
 *     <li>创建workerGroup线程组: 处理网络事件--读写事件 </li>
 *     <li>创建服务端启动助手</li>
 *     <li>设置bossGroup线程组和workerGroup线程组</li>
 *     <li>设置服务端通道实现为NIO</li>
 *     <li>参数设置</li>
 *     <li>创建一个通道初始化对象</li>
 *     <li>向pipeline中添加自定义业务处理handler</li>
 *     <li>启动服务端并绑定端口,同时将异步改为同步</li>
 *     <li>关闭通道和关闭连接池</li>
 * </ol
 *
 */
public class MyNettyServer {
    public static void main(String[] args) {


        final ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class);

        // 创建了一组线程，通过死循环 监控状态（accept,read,write）
        //Netty 内部实际是在调用EventLoop next() 方法做事件监听，将监听的事件交给Handler去处理
        serverBootstrap.group(new NioEventLoopGroup());


        /**
         * childHandler 处理的是workgroup
         * handler 处理的是bossgroup
         */
        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {

            /**
             * channel 已经接通了，在监控状态，需要通过pipeline用handler进行处理
             *
             * @param nioSocketChannel
             * @throws Exception
             */
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                /**
                 * 解码：new StringDecoder()
                 */
                nioSocketChannel.pipeline().addLast(new StringDecoder());

                /**
                 * ChannelInboundHandler 在读取数据端使用，读取bytebuffer的数据
                 * ChannelInboundHandlerAdapter 适配器
                 *
                 * msg：将上一步pipeline解码的数据传输给这个参数
                 */
                nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println(msg);
                    }
                });
            }
        });
        serverBootstrap.bind(new InetSocketAddress(9999));

    }
}
