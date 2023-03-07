package com.bh.netty01.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 *
 * <h2>阻塞 & 非阻塞</h2>
 * <ol>
 *     <li>阻塞和非阻塞指的是执行一个操作是等操作结束再返回，还是马上返回。</li>
 *     <li>比如餐馆的服务员为用户点菜，当有用户点完菜后，服务员将菜单给后台厨师，此时有两种方式：</li>
 *     <ul>
 *         <li>第一种：就在出菜窗口等待，直到厨师炒完菜后将菜送到窗口，然后服务员再将菜送到用户手中；</li>
 *         <li>第二种：等一会再到窗口来问厨师，某个菜好了没？如果没有先处理其他事情，等会再去问一次；</li>
 *     </ul>
 * </ol>
 * <p>第一种就是阻塞方式，第二种则是非阻塞的。</p>
 *
 * <h2>异步&同步</h2>
 * <p>
 *     同步和异步又是另外一个概念，它是事件本身的一个属性。还拿前面点菜为例，服务员直接跟厨师打交道，菜出来没出来，服务员直接指导，但只有当厨师将菜送到服务员手上，这个过程才算正常完成，这就是同步的事件。同样是点菜，有些餐馆有专门的传菜人员，当厨师炒好菜后，传菜员将菜送到传菜窗口，并通知服务员，这就变成异步的了。其实异步还可以分为两种：带通知的和不带通知的。前面说的那种属于带通知的。有些传菜员干活可能主动性不是很够，不会主动通知你，你就需要时不时的去关注一下状态。这种就是不带通知的异步。
 *  对于同步的事件，你只能以阻塞的方式去做。而对于异步的事件，阻塞和非阻塞都是可以的。非阻塞又有两种方式：主动查询和被动接收消息。被动不意味着一定不好，在这里它恰恰是效率更高的，因为在主动查询里绝大部分的查询是在做无用功。对于带通知的异步事件，两者皆可。而对于不带通知的，则只能用主动查询。
 *  但是对于非阻塞和异步的概念有点混淆，非阻塞只是意味着方法调用不阻塞，就是说作为服务员的你不用一直在窗口等，非阻塞的逻辑是"等可以读（写）了告诉你"，但是完成读（写）工作的还是调用者（线程）服务员的你等菜到窗口了还是要你亲自去拿。而异步意味这你可以不用亲自去做读（写）这件事，你的工作让别人（别的线程）来做，你只需要发起调用，别人把工作做完以后，或许再通知你，它的逻辑是“我做完了 告诉/不告诉 你”，他和非阻塞的区别在于一个是"已经做完"另一个是"可以去做"。
 * </p>
 *
 * <h2>问题：</h2>
 * <p>客户端发请求，找到boss处理accept事件，然后去找worker去处理
 * 如果worker 处理是很费时间的，意味着worker接收到请求的时候需要占用大量的事件去处理，会影响新的客户端对worker的操作
 * 这样系统的吞吐量就低了，原来可以处理多个请求的worker，现在worker只可以处理一个请求了。</p>
 * <h2>怎么解决？</h2>
 * <p>不让worker去做实际的业务处理（费时处理），只负责请求的接入，当接入的时候，再把业务处理交给新的线程去处理
 * 这样worker的并发性就高了，这个行为在netty中就叫做异步化，异步化就是多线程，实际上就是把非io的操作转移到新的线程去处理</p>
 *
 * <ol>
 *     <li> 创建新的线程去处理非io操作？==>DefaultEventLoop</li>
 *     <li>怎么来获得非io操作？数据怎么获得？==》在netty体系中，io数据的获得是通过handler来获取的.使用：ChannelPipeline addLast(EventExecutorGroup var1, ChannelHandler... var2);</li>
 *     <li> DefaultEventLoopGroup 辅助 NioEventLoopGroup 去完成普通的业务操作</li>
 * </ol>
 *     使用后的现象：
 *    <p> // nioEventLoopGroup-3-1 进行io的线程</p>
 *     <p> 2022-11-01 01:56:17.585 [nioEventLoopGroup-3-1] DEBUG io.netty.util.ResourceLeakDetectorFactory - Loaded default ResourceLeakDetector: io.netty.util.ResourceLeakDetector@2ccf31d9</p>
 *    <p>  // 处理业务的线程defaultEventLoopGroup-4-1</p>
 *    <p> 2022-11-01 01:56:17.609 [defaultEventLoopGroup-4-1] DEBUG com.bh.netty01.netty.server.MyNettyServer2 - hello netty</p>
 *
 * <h2>小问题：</h2>
 * <p>监听读写和编码解码有啥区别？</p>
 * <ol>
 *     <li>监听读写：是客户端的数据流入服务端的一个过程，数据还没有到达服务端</li>
 *     <li>编码解码：就是数据已经到达服务端了，可以进行编码解码了</li>
 * </ol>
 *
 * <h2> 简单版Netty模型 </h2>
 * <img width="640" height="320" src="../../../../../../../../../README.assets/image-20221101215255313.png">
 *     <ol>
 *         <li>BossGroup 线程维护 Selector，ServerSocketChannel 注册到这个 Selector 上，只关注连接 建立请求事件(主 Reactor)</li>
 *         <li>当接收到来自客户端的连接建立请求事件的时候，通过 ServerSocketChannel.accept 方法获 得对应的 SocketChannel，并封装成 NioSocketChannel 注册到 WorkerGroup 线程中的 Selector，每个 Selector 运行在一个线程中(从 Reactor)</li>
 *         <li>当 WorkerGroup 线程中的 Selector 监听到自己感兴趣的 IO 事件后，就调用 Handler 进行处理</li>
 *     </ol>
 *
 *  <h2>详细版Netty模型</h2>
 *   <img width="640" height="320" src="../../../../../../../../../README.assets/image-20221101220653506.png">
 *  <ol>
 *      <li>Netty 抽象出两组线程池:BossGroup 和 WorkerGroup，也可以叫做 BossNioEventLoopGroup 和 WorkerNioEventLoopGroup。每个线程池中都有 NioEventLoop 线程。BossGroup 中的线程专门负责和客户端建立连接，WorkerGroup 中的 线程专门负责处理连接上的读写。BossGroup 和 WorkerGroup 的类型都是 NioEventLoopGroup</li>
 *      <li>NioEventLoopGroup 相当于一个事件循环组，这个组中含有多个事件循环，每个事件循环就 是一个 NioEventLoop</li>
 *      <li>NioEventLoop 表示一个不断循环的执行事件处理的线程，每个 NioEventLoop 都包含一个 Selector，用于监听注册在其上的 Socket 网络连接(Channel)</li>
 *      <li>NioEventLoopGroup 可以含有多个线程，即可以含有多个 NioEventLoop</li>
 *      <li>每个 BossNioEventLoop 中循环执行以下三个步骤:
 *          <ul>
 *              <li>select:轮训注册在其上的 ServerSocketChannel 的 accept 事件(OP_ACCEPT 事件)</li>
 *              <li>processSelectedKeys:处理 accept 事件，与客户端建立连接，生成一个 NioSocketChannel，并将其注册到某个 WorkerNioEventLoop 上的 Selector 上</li>
 *              <li>runAllTasks:再去以此循环处理任务队列中的其他任务</li>
 *          </ul>
 *      </li>
 *      <li>每个 WorkerNioEventLoop 中循环执行以下三个步骤
 *          <ul>
 *              <li>select:轮训注册在其上的 NioSocketChannel 的 read/write 事件 (OP_READ/OP_WRITE 事件)</li>
 *              <li>processSelectedKeys:在对应的 NioSocketChannel 上处理 read/write 事件</li>
 *              <li>runAllTasks:再去以此循环处理任务队列中的其他任务</li>
 *          </ul>
 *      </li>
 *      <li>在以上两个processSelectedKeys步骤中，会使用 Pipeline(管道)，Pipeline 中引用了 Channel，即通过 Pipeline 可以获取到对应的 Channel，Pipeline 中维护了很多的处理器 (拦截处理器、过滤处理器、自定义处理器等)。</li>
 *  </ol>
 *
 *  <h2>Netty里面的编解码</h2>
 *  <ol>
 *      <li>解码器:负责处理“入站 InboundHandler”数据。</li>
 *      <li>编码器:负责“出站,OutboundHandler” 数据。</li>
 *      <li>读消息不会经过出站，写消息不会经过出站</li>
 *      <li></li>
 *  </ol>
 *  <p>
 *      理解：进来(Inbound)的数据就是需要"解码"，出去(Outbound)的数据需要"编码"
 *  </p>
 *
 *
 */
@Slf4j
public class MyNettyServer2 {
    public static void main(String[] args) {

        final ServerBootstrap serverBootstrap = new ServerBootstrap();

        /**
         * 客户端通过主机 和IP 连接 服务端的NioServerSocketChannel
         *
         * NioServerSocketChannel和NioSocketChannel是Netty中两种不同类型的通道，分别代表服务端监听通道和客户端连接通道。它们之间的区别如下：
         * 作用不同。NioServerSocketChannel用于监听客户端的连接请求，
         * 创建新的NioSocketChannel来处理客户端请求；
         * 而NioSocketChannel用于连接服务器，与服务器进行通信。
         * 事件处理不同。NioServerSocketChannel主要处理ACCEPT事件，
         * 即监听到新的客户端连接请求；NioSocketChannel主要处理READ、WRITE等事件，
         * 即读取客户端发送的数据并向客户端发送数据。
         * 初始化参数不同。NioServerSocketChannel需要设置服务端Socket的一些参数，
         * 例如backlog等；NioSocketChannel需要设置连接远程服务器的IP地址和端口等。
         * 方法不同。NioServerSocketChannel有一些独有的方法，例如bind()用于绑定服务端Socket地址，
         * accept()用于接受客户端连接；NioSocketChannel有一些独有的方法，例如connect()用于连接远程服务器。
         * 总的来说，NioServerSocketChannel和NioSocketChannel分别代表服务端监听通道和客户端连接通道，
         * 它们具有不同的作用、事件处理方式、初始化参数和方法。在Netty中，
         * 我们可以通过这两种通道来实现高效的网络通信。
         *
         */
        // 用来监听客户端发起的 "OP_ACCEPT" 事件。你可以点进去NioServerSocketChannel查看
        serverBootstrap.channel(NioServerSocketChannel.class);




        /**
         * reactor 主从的设计
         * 主1 从多
         */
        final NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        final NioEventLoopGroup workerGroup = new NioEventLoopGroup(3);
        serverBootstrap.group(bossGroup,workerGroup);

        final DefaultEventLoopGroup defaultEventLoopGroup = new DefaultEventLoopGroup();


        /**
         *  serverBootstrap.handler() vs serverBootstrap.childHandler()
         *  服务端做的两件事
         *  1。 ServerSocketChannel: 接收客户端的请求，建立连接
         *  2。 SocketChannel： 建立好连接之后，建立SocketChannel进行IO通信
         *  handler() 处理的是ServerSocketChannel --> accept -->NioEventLoop 线程boss -->handler()
         *  服务端的handler()我们很少使用，因为我们很少去干预客户端建立连接时候的 ACCEPT事件
         *
         *  childHandler() 处理的是SocketChannel -->write and read -->NioEventLoop 线程worker --> childHandler()
         *  与client实际建立的 SocketChannel， 客户端每来一个请求，服务端都会建立与之IO的 SocketChannel
         *
         *  客户端每个请求到来的时候，服务端监控到是ACCEPT事件的时候，分配一个worker,去处理与客户端建立连接的IO请求
         *  可以看看reactor的图。
         *
         *  对比客户端的handler()
         */
        //serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,new AdaptiveRecvByteBufAllocator());

        /**
         * childHandler 是用来为ServerSocketChannel的每个新连接创建一个新的SocketChannel，
         * 并添加一组ChannelHandler来处理该SocketChannel的网络事件。
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

                //

                /**
                 * ChannelInboundHandler 在读取数据端使用，读取bytebuffer的数据
                 * ChannelInboundHandlerAdapter 适配器
                 *
                 * msg：将上一步pipeline解码的数据传输给这个参数
                 */
                nioSocketChannel.pipeline().addLast(defaultEventLoopGroup, new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        System.out.println("msg = "+msg.toString() );
                        log.debug("msg ="+msg.toString());
                    }
                });
            }
        });
        serverBootstrap.bind(new InetSocketAddress(9999));

    }
}
