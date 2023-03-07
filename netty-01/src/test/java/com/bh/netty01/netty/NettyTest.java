package com.bh.netty01.netty;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoop;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 异步处理过程中：
 * 1. 阻塞 主线程 完成异步操作的配合。
 * 2. 异步处理 （新线程 异步线程完成的），用回调方法去解决
 * 只要是异步的操作 只能通过上述的2种方式的一种处理。
 * 在Netty只要涉及到 网络 IO的相关操作 那么Netty都会涉及异步处理。
 * <p>
 * Netty为什么 要把 网络 IO的相关操作 做成异步？
 * 异步的好处？
 * 1.提高系统的吞吐量。。。
 * 2.效率上的提高
 */
@Slf4j
public class NettyTest {

    @Test
    public void testEventLoopGroup() {
        /**
         * 创建多个EventLoop （线程），并存起来
         * 1。 通过构造方法可以指定创建EventLoop的个数（线程个数）
         * 2。 通过无参构造，会创建DEFAULT_EVENT_LOOP_THREADS=Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));
         *
         */

        //io.netty.channel.nio.NioEventLoop@5d534f5d
        //io.netty.channel.nio.NioEventLoop@2e3967ea
        //io.netty.channel.nio.NioEventLoop@5d534f5d
        // 表明线程池中的线程数是2
        final NioEventLoopGroup eventExecutors = new NioEventLoopGroup(2);
        System.out.println(eventExecutors.next());
        System.out.println(eventExecutors.next());
        System.out.println(eventExecutors.next());

        //io.netty.channel.DefaultEventLoop@6fdbe764
        //io.netty.channel.DefaultEventLoop@6fdbe764
        //io.netty.channel.DefaultEventLoop@6fdbe764
        // 默认线程池中的线程数是1
        final DefaultEventLoop defaultEventLoop = new DefaultEventLoop();
        System.out.println(defaultEventLoop.next());
        System.out.println(defaultEventLoop.next());
        System.out.println(defaultEventLoop.next());


        //io.netty.channel.DefaultEventLoop@7b36aa0c
        //io.netty.channel.DefaultEventLoop@7b36aa0c
        //io.netty.channel.DefaultEventLoop@7b36aa0c
        final DefaultEventLoop defaultEventLoop1 = new DefaultEventLoop(eventExecutors);
        System.out.println(defaultEventLoop1.next());
        System.out.println(defaultEventLoop1.next());
        System.out.println(defaultEventLoop1.next());

        defaultEventLoop.submit(() -> System.out.println("hello"));
    }

    /**
     * JDKFuture
     * 结果只能通过阻塞的方式完成。
     */
    @Test
    public void testJDKFuture() throws ExecutionException, InterruptedException {
  /*
           JDK什么情况下考虑使用Future
           异步化的工作 Future
                        启动一个新的线程进行处理。最后把处理结果返回给主线程（调用者线程）

           JDK Future处理过程中 异步操作的处理 只能通过阻塞的方式完成。
         */
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Future<Integer> future = executorService.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("异步的工作处理...");
                TimeUnit.SECONDS.sleep(10);
                return 10;
            }
        });

        log.debug("可以处理结果了...");
        log.debug("处理结果 {}", future.get());//阻塞的操作..
        log.debug("------------------------------------");
    }

    /**
     * NettyFuture
     * 1。 阻塞方式处理异步
     * 2。 异步监听的方式处理异步
     */
    @Test
    public void testNettyFuture() {
        // EventLoopGroup 使用Netty Future---> EventLoopGroup -- NioEventLoopGroup--> selector 事件的监听
        // 异步工作的处理 ，启动一个新的线程 完成操作
        DefaultEventLoopGroup defaultEventLoopGroup = new DefaultEventLoopGroup(2);

        EventLoop eventLoop = defaultEventLoopGroup.next();

        //如何证明 10 主线程 获得结果. 但是不能证明 这个结果是正常的还是失败呢
        // 异步处理的2个问题
        //  runable接口 ---》 主线程（调用者线程）返回结果
        //  callable接口 ---》 返回值 也不能准确的表达 结果 成功 还是 失败了。
        io.netty.util.concurrent.Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    log.debug("异步操作处理...");
                    TimeUnit.SECONDS.sleep(10);
                    return 10;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return -1;
                }

            }
        });

        //log.debug("可以接受异步处理");
        //同步阻塞 处理
        //log.debug("异步处理的结果...{} ",future.get());
        //log.debug("--------------------------------------------");

        future.addListener(new GenericFutureListener<io.netty.util.concurrent.Future<? super Integer>>() {
            @Override
            public void operationComplete(io.netty.util.concurrent.Future<? super Integer> future) throws Exception {
                log.debug("异步处理的结果...{} ", future.get());
            }
        });

        log.debug("---------------------------------------");
    }


    /**
     * 1。 阻塞方式处理异步
     * 2。 异步监听的方式处理异步
     * 3。 增加了获取异步处理结果的功能
     */
    @Test
    public void testPromise() {
        EventLoop eventLoop = new DefaultEventLoop().next();
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        new Thread(() -> {
            log.debug("异步处理....");
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            promise.setSuccess(10);
        }).start();

        /*log.debug("等待异步处理的结果");
        log.debug("结果是 {}",promise.get());
        log.debug("----------------------");*/

        promise.addListener(new GenericFutureListener<io.netty.util.concurrent.Future<? super Integer>>() {
            @Override
            public void operationComplete(io.netty.util.concurrent.Future<? super Integer> future) throws Exception {
                log.debug("等待异步处理的结果");
                log.debug("结果是 {}", promise.get());
            }
        });

        log.debug("---------------------------------");
    }

    @Test
    public void testEmbededHandler() {
        ChannelInboundHandlerAdapter h1 = new ChannelInboundHandlerAdapter() {
            @Override
            //ByteBuf
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("h1 {}", msg);
                super.channelRead(ctx, msg);
            }
        };
        ChannelInboundHandlerAdapter h2 = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("h2");
                super.channelRead(ctx, msg);
            }
        };
        ChannelInboundHandlerAdapter h3 = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.debug("h3");
                super.channelRead(ctx, msg);
            }
        };
        ChannelOutboundHandlerAdapter h4 = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                log.debug("h4");
                super.write(ctx, msg, promise);
            }
        };
        ChannelOutboundHandlerAdapter h5 = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                log.debug("h5");
                super.write(ctx, msg, promise);
            }
        };
        ChannelOutboundHandlerAdapter h6 = new ChannelOutboundHandlerAdapter() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                log.debug("h6");
                super.write(ctx, msg, promise);
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(h1, h2, h3, h4, h5, h6);

        //channel.writeInbound("xiaohei");
        channel.writeOutbound("xiaojr");
        //channel.writeInbound(ByteBufAllocator.DEFAULT.buffer().writeBytes("hello netty".getBytes()));
    }


    @Test
    public void testByteBuf() {
        // 如何获取ByteBuf
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);

        // 默认的ByteBuf 256，最大的内存空间是Integer的最大值 21亿
        //         final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        buffer.writeByte('a');
        buffer.writeInt(10);
        buffer.writeInt(11);
        // 容量超过设定的容量之后，会自动扩容
        //PooledUnsafeDirectByteBuf(ridx: 0, widx: 13, cap: 16)
        buffer.writeInt(12);
        System.out.println(buffer);
        System.out.println(ByteBufUtil.prettyHexDump(buffer));
    }


    /**
     * 为了测试堆内存和直接内存的创建
     * <p>
     * buffer = PooledUnsafeDirectByteBuf(ridx: 0, widx: 0, cap: 10)
     * byteBuf = PooledUnsafeDirectByteBuf(ridx: 0, widx: 0, cap: 256)
     * byteBuf2 = PooledUnsafeHeapByteBuf(ridx: 0, widx: 0, cap: 256)
     * <p>
     * 堆内存：
     * 创建和销毁代价小，读写洗哦阿绿低，gc压力大
     * <p>
     * 直接内存：
     * 创建和销毁 代价大 gc压力大
     * <p>
     * 池化的作用：
     * 1。 站在调用者提高创建的效率
     * 2。 合理的使用了资源
     * 3。 觉少了内存溢出的可能（内存不够用，内存使用过多）
     * <p>
     * netty 池化默认开启
     * 4.1 以后 默认开启 ByteBuf池化
     * 4.1 以前 默认关闭。 -Dio.netty.allocator.type = unpooled
     */
    @Test
    public void testByteBuf2() {

        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);
        System.out.println("buffer = " + buffer);

        final ByteBuf byteBuf = ByteBufAllocator.DEFAULT.directBuffer();
        System.out.println("byteBuf = " + byteBuf);

        final ByteBuf byteBuf2 = ByteBufAllocator.DEFAULT.heapBuffer();
        System.out.println("byteBuf2 = " + byteBuf2);
    }


    /**
     * read 和get都可以读取 ByteBuf的数据
     * read 读取完成后会操作读指针
     * get 读取完成后不会操作读指针
     *
     * write是顺序写，一个一个字节的去写，改变写指针
     * set在特定的位置中去写数据，不会改变写指针，但是你set的时候不能set超过写指针的
     * set可以理解为重置某个索引下的数据，前提是那个索引下需要有数据
     *
     * buffer = PooledUnsafeDirectByteBuf(ridx: 0, widx: 1, cap: 10)
     *          +-------------------------------------------------+
     *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
     * +--------+-------------------------------------------------+----------------+
     * |00000000| 06                                              |.               |
     * +--------+-------------------------------------------------+----------------+
     *
     * buffer = PooledUnsafeDirectByteBuf(ridx: 0, widx: 1, cap: 10)
     *          +-------------------------------------------------+
     *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
     * +--------+-------------------------------------------------+----------------+
     * |00000000| 06                                              |.               |
     * +--------+-------------------------------------------------+----------------+
     **/

    @Test
    public void testByteBuf4() {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);
        buffer.writeByte(6);
        System.out.println("buffer = " + buffer);
        System.out.println(ByteBufUtil.prettyHexDump(buffer));

        // 这个写在index=1 的时候是写不进去的，因为你的写指针还在0
       buffer.setByte(1,2);
        System.out.println("buffer = " + buffer);
        System.out.println(ByteBufUtil.prettyHexDump(buffer));

        // 这个写在index=0 的时候是可以写进去的，因为你的写指针在0
        buffer.setByte(0,12);
        System.out.println("buffer = " + buffer);
        System.out.println(ByteBufUtil.prettyHexDump(buffer));
    }


    /**
     *    buffer.markReaderIndex();
     *    buffer.resetReaderIndex();
     *    markReaderIndex和resetReaderIndex 结合可以重复读取某个索引下的数据，不这样做是重复读取不了的，因为你每读取一次，读指针就会往前移
     */
    @Test
    public void testByteBuf3() {

        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);
        buffer.writeByte(6);

        System.out.println("buffer = " + buffer);
        System.out.println(ByteBufUtil.prettyHexDump(buffer));

        //如果想重复读取ByteBuf中的数据，需要重置读指针
        buffer.markReaderIndex();
        final byte b = buffer.readByte();
        System.out.println("b = " + b);

        buffer.resetReaderIndex();
        final byte b2 = buffer.readByte();
        System.out.println("b2 = " + b2);
    }

    /**
     * ByteBuf 释放内存
     * 是不是回收？清空，销毁？
     * 不是，如果ByteBuf 是池化的 ，内存释放《=等同于=》 是放回ByteBuf的池子里
     *
     * Netty在处理内存释放的时候，因为内存释放情况种类繁多，所以netty对于编程人员来说，设计了统一的内存释放接口
     *
     * Netty引入了 ReferenceCounted（引用计数器），ByteBuf实现了ReferenceCounted接口
     * 创建ByteBuf的时候，引用计数器加1
     * 使用完ByteBuf的时候，引用计数器减1
     * 当引用计数器为0的时候，就可以释放ByteBuf的内存了
     *
     * ByteBuf 什么时候需要释放？
     * 1。 ByteBuf一定只能应用在pipeline中，在handler中进行释放数据最为理想且稳妥
     * 2。 TailContext会对读到的数据进行ByteBuf释放内存， HeadContext会对写的数据进行ByteBuf 释放
     * 所以 我们需要在最后一次使用 ByteBuf的时候，做ByteBuf释放
     *
     * deepl 翻译软件
     *
     * head h1 h2 h3 tail
     * tail 是读入数据的handler处理的最后一环，之后ByteBuf 就没用了，所以在这一环中可以对
     * ByteBuf 进行释放。
     *
     * TailContext:
     * public void channelRead(ChannelHandlerContext ctx, Object msg) {
     *             onUnhandledInboundMessage(ctx, msg);
     *         }
     *
     *   protected void onUnhandledInboundMessage(Object msg) {
     *         try {
     *             logger.debug(
     *                     "Discarded inbound message {} that reached at the tail of the pipeline. " +
     *                             "Please check your pipeline configuration.", msg);
     *         } finally {
     *         // 这里就是释放内存
     *             ReferenceCountUtil.release(msg);
     *         }
     *     }
     *
     *  当是写数据的时候，最后一环是 HeadContext
     *  write()的时候会释放内存
     */


    /**
     * slice() 切片
     * 切片之后的ByteBuf是有自己独立的读写指针（来限制从哪里开始读写）的，本质上还是同一块内存
     * buffer = PooledUnsafeDirectByteBuf(ridx: 0, widx: 11, cap: 16)
     *          +-------------------------------------------------+
     *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
     * +--------+-------------------------------------------------+----------------+
     * |00000000| 00 01 02 03 04 05 06 07 08 09 0a                |...........     |
     * +--------+-------------------------------------------------+----------------+
     *
     * slice1 = UnpooledSlicedByteBuf(ridx: 0, widx: 6, cap: 6/6, unwrapped: PooledUnsafeDirectByteBuf(ridx: 0, widx: 11, cap: 16))
     *          +-------------------------------------------------+
     *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
     * +--------+-------------------------------------------------+----------------+
     * |00000000| 00 01 02 03 04 05                               |......          |
     * +--------+-------------------------------------------------+----------------+
     *
     * slice2 = UnpooledSlicedByteBuf(ridx: 0, widx: 4, cap: 4/4, unwrapped: PooledUnsafeDirectByteBuf(ridx: 0, widx: 11, cap: 16))
     *          +-------------------------------------------------+
     *          |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
     * +--------+-------------------------------------------------+----------------+
     * |00000000| 06 07 08 09                                     |....            |
     * +--------+-------------------------------------------------+----------------+
     *
     */
    @Test
    public void testByteBuf5() {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);
        for (int i = 0; i <= 10; i++) {
            buffer.writeByte(Integer.valueOf(i).byteValue());
        }
        System.out.println("buffer = " + buffer);
        System.out.println(ByteBufUtil.prettyHexDump(buffer));

        final ByteBuf slice1 = buffer.slice(0, 6);
        final ByteBuf slice2 = buffer.slice(6, 4);

        System.out.println("slice1 = " + slice1);
        slice1.retain();
        System.out.println(ByteBufUtil.prettyHexDump(slice1));


        System.out.println("slice2 = " + slice2);
        slice1.retain();
        System.out.println(ByteBufUtil.prettyHexDump(slice2));

        // release 会影响切片的数据，想要没有影响你可以使用retain去维持引用,否则会有异常//io.netty.util.IllegalReferenceCountException: refCnt: 0
        buffer.release();
        System.out.println(ByteBufUtil.prettyHexDump(slice1));
    }


    /**
     * netty中的半包，粘包
     * 本质的问题是：接收数据的时候，数据可能接收不完整也可能接收过多的问题
     * 啥时候会出现半包，粘包这种情况？
     * 当你发送的数据量超过ByteBuf的时候会出现半包。
     *
     * 当你发送的数据量很小，一次性接收到的几个数据在一个ByteBuf的时候会出现粘包。
     *
     * tcp消息传递过程中，是流式协议，消息无边界
     * 必须对消息进行边界化的处理 ，只有我们对消息进行了边界化的处理之后，我们才可以得到正确的消息，才可以
     * 精准的解析到网络通信中的数据。我们需要使用编程的方式对半包，粘包进行处理
     *
     * 网络通信过程中，什么情况数据可能接收不完整也可能接收过多？
     * 原因：
     *  1. ByteBuf的问题
     *
     *  1. Socket接收（服务端/客户端） 和 发送缓冲区（客户端/服务端）是独立的两个
     *  2. 用户缓存 <   Socket 缓冲区的大小，因为一般来说用户的数据会传输到Socket 缓冲区几批之后再发送出去
     *
     *  客户端发送数据帧给服务端的时候，当服务端接收完毕之后，会发送一个ack给客户端，这是串行的操作
     *  优化的策略，一次性发送多个数据帧给服务端，服务端返回一个ack给客户端
     *
     *  一次性可以发送多少个数据帧，这就是滑动窗口
     *
     *  滑动窗口默认大小多大？
     *  等同于socket缓冲区的大小
     *
     *  write()方法有一个返回值 ，返回100个字节就是真的写到服务器端了吗？
     *  不是，write()方法返回值的真正含义返回的字节数是 用户态往内部态的socket写的数据
     *  为什么有时候会返回0？
     *  是因为当我们向内部态写数据的时候，缓冲区满了，满了还没有做flush操作，所以write()方法返回值仅仅代表
     *  往内核态socket缓冲区写的数据的多少，这个数据并不代实际往服务器发送。
     *  总结：write()方法 并不代表进行了网络传输，得等我们的socket去刷新数据才会发送
     *  返回0的含义：socket缓冲区暂时满了。write()有返回值不代表我们的网络（心跳）是通的
     *
     *  socket缓冲区的默认大小？
     *  65535
     *
     *  注意：
     *  netty通信过程中，用于网络通信的 ByteBuf 是netty帮我们创建的
     *      1。 如何创建的？（第一个非head handler 的 inboundHandler 获得的object msg 就是Netty读取数据后，封装的ByteBuf）
     *          在handler中，object msg,msg就是netty接收客户端传输过来的封装好的数据（ByteBuf），
     *          我们需要从用户区从内核区读取数据，netty帮我们调read()方法，将相关的ByteBuf读取到，然后
     *          这个ByteBuf作为参数传递给了handler
     *      2。 netty帮我们创建的ByteBuf的默认大小是多大？
     *          接收数据所创建的ByteBuf 大小默认为1024
     *          可以进行修改：
     *                  serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,new AdaptiveRecvByteBufAllocator());
     *                   static final int DEFAULT_MINIMUM = 64;
     *                   static final int DEFAULT_INITIAL = 1024;
     *                   static final int DEFAULT_MAXIMUM = 65536;
     *
     *      3. 修改socket默认缓冲区的大小
     *                  serverBootstrap.childOption(ChannelOption.SO_RCVBUF,xxx);
     *
     *  Netty半包粘包的解决方式思路分析
     *  1. 先获得ByteBuf之后再去解决半包粘包，通过第一个非head handler 获得ByteBuf
     *  2. decoder,encoder 主要解决的是 ByteBuf相关的内容，在netty中会将原始的字节 称为Byte，将转化好的数据称为Message(半包，粘包已经解决完毕)
     *  3. ByteToMessageDecoder 完成 ByteBuf --》Message（完整的消息，不应该存在半包粘包）
     */
    @Test
    public void test() {

    }



}
