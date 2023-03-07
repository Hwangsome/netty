package com.bh.netty01.reactor.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Boss , Worker 都有各自独立的 selector
 *
 * Reactor模型就是：
 *      Reactor中包含selector 和dispatcher ，让主线程监听客户端的请求，再起一个新的线程去处理业务
 *  Reactor 模式，通过一个或多个输入同时传递给服务处理器的模式 , 服务器端程序处理传入的多个 请求,
 *  并将它们同步分派到相应的处理线程， 因此 Reactor 模式也叫 Dispatcher模式.
 *  Reactor 模式使用 IO 复用监听事件, 收到事件后，分发给某个线程(进程), 这点就是网络服务器高并发处理关键.
 */

/**
 * <h2>单 Reactor 单线程 模型：</h2>
 * Reactor 模式，通过一个或多个输入同时传递给服务处理器的模式 , 服务器端程序处理传入的多个 请求,并将它们同步分派到相应的处理线程， 因此 Reactor 模式也叫 Dispatcher模式. Reactor 模式使用 IO 复用监听事件, 收到事件后，分发给某个线程(进程), 这点就是网络服务器高并发处理关键.
 * <img width="640" height="320" alt="" src="../../../../../../../../../img/img.png">
 *
 * <div>1. Selector是可以实现应用程序通过一个阻塞对象监听多路连接请求。</div>
 * <div>2. Reactor 对象通过 Selector监控客户端请求事件，收到事件后通过 Dispatch 进行分发,建立连接请求事件，则由 Acceptor 通过 Accept 处理连接请求，然后创建一个 Handler 对 象处理连接完成后的后续业务处理。</div>
 * <div>3. Handler 会完成 Read→业务处理→Send 的完整业务流程。</div>
 *
 * <div>
 *     <h3>优点：</h3>
 *     <p>模型简单，没有多线程、进程通信、竞争的问题，全部都在一个线程中完成</p>
 *     <h3>缺点:</h3>
 *          <p>1. 性能问题: 只有一个线程，无法完全发挥多核 CPU 的性能。Handler 在处理某个连接上的业务时，整个进程无法处理其他连接事件，很容易导致性能瓶颈</p>
 *          <p>2. 可靠性问题: 线程意外终止或者进入死循环，会导致整个系统通信模块不可用，不能接收和处 理外部消息，造成节点故障</p>
 * </div>
 *
 *
 *
 * <h2>单Reactor 多线程模型：</h2>
 * <img width="640" height="320" alt="" src="../../../../../../../../../README.assets/image-20221101171035428.png">
 *
 * <ol>
 *   <li>Reactor 对象通过 selector 监控客户端请求事件, 收到事件后，通过 dispatch 进行分发</li>
 *   <li>如果建立连接请求, 则右 Acceptor 通过accept 处理连接请求</li>
 *   <li>如果不是连接请求，则由 reactor 分发调用连接对应的 handler 来处理</li>
 *   <li>handler 只负责响应事件，不做具体的业务处理, 通过 read 读取数据后，会分发给后面的 worker 线程池的某个线程处理业务</li>
 *   <li>worker 线程池会分配独立线程完成真正的业务，并将结果返回给 handler</li>
 *   <li>handler 收到响应后，通过 send 将结果返回给 client</li>
 * </ol>
 * <h3>优点</h3>
 * <p>可以充分的利用多核 cpu 的处理能力</p>
 * <h3>缺点</h3>
 * <p>多线程数据共享和访问比较复杂， reactor 处理所有的事件的监听和响应，在单线程运行，在高并发场景容易出现性能瓶颈</p>
 *
 * <h2>主从 Reactor 多线程模型：</h2>
 *  <img width="640" height="320" alt="" src="../../../../../../../../../README.assets/image-20221101171547711.png">
 * <ol>
 *   <li>Reactor 主线程 MainReactor 对象通过 select 监听客户端连接事件，收到事件后，通过 Acceptor 处理客户端连接事件</li>
 *   <li>当 Acceptor 处理完客户端连接事件之后(与客户端建立好 Socket 连接)，MainReactor 将 连接分配给 SubReactor。(即:MainReactor 只负责监听客户端连接请求，和客户端建立连 接之后将连接交由 SubReactor 监听后面的 IO 事件。)</li>
 *   <li>SubReactor 将连接加入到自己的连接队列进行监听，并创建 Handler 对各种事件进行处理</li>
 *   <li>当连接上有新事件发生的时候，SubReactor 就会调用对应的 Handler 处理</li>
 *   <li>Handler 通过 read 从连接上读取请求数据，将请求数据分发给 Worker 线程池进行业务处理</li>
 *   <li>Worker 线程池会分配独立线程来完成真正的业务处理，并将处理结果返回给 Handler。 Handler 通过 send 向客户端发送响应数据</li>
 *   <li>一个 MainReactor 可以对应多个 SubReactor，即一个 MainReactor 线程可以对应多个 SubReactor 线程</li>
 * </ol>
 *
 * <h3>优点:</h3>
 * <ol>
 *   <li>MainReactor 线程与 SubReactor 线程的数据交互简单职责明确，MainReactor 线程只需要 接收新连接，SubReactor 线程完成后续的业务处理</li>
 *   <li> MainReactor 线程与 SubReactor 线程的数据交互简单， MainReactor 线程只需要把新连接 传给 SubReactor 线程，SubReactor 线程无需返回数据</li>
 *   <li>多个 SubReactor 线程能够应对更高的并发请求</li>
 * </ol>
 *
 * <h3>缺点:</h3>
 * <ol>
 *   <li>这种模式的缺点是编程复杂度较高。但是由于其优点明显，在许多项目中被广泛使用，包括 Nginx、Memcached、Netty 等。这种模式也被叫做服务器的 1+M+N 线程模式，即使用该模式开 发的服务器包含一个(或多个，1 只是表示相对较少)连接建立线程+M 个 IO 线程+N 个业务处理 线程。这是业界成熟的服务器程序设计模式。</li>
 * </ol>
 */

public class ReactorBossServer {
    public static void main(String[] args) throws IOException {

        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(10000));

        final Selector masterSelector = Selector.open();

        serverSocketChannel.register(masterSelector, SelectionKey.OP_ACCEPT, null);

        //Worker worker = new Worker("worker-01");
        final Worker[] workers = new Worker[2];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker - " + i);//worker-0 worker-1
        }
        AtomicInteger index = new AtomicInteger();

        while (true) {
            masterSelector.select();

            final Iterator<SelectionKey> iterator = masterSelector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                final SelectionKey selectedKey = iterator.next();
                iterator.remove();

                if (selectedKey.isAcceptable()) {
                    final ServerSocketChannel selectedKeyWithServerSocketChannel = (ServerSocketChannel) selectedKey.channel();
                    final SocketChannel selectedKeyWithSocketChannel = selectedKeyWithServerSocketChannel.accept();
                    selectedKeyWithSocketChannel.configureBlocking(false);

                    System.out.println("boss invoke worker register ...");
                    //worker-0 worker-1 worker-0 worker-1
                    //hash取摸    x%2= 0  1 [0,1,0,1]
                    workers[index.getAndIncrement()% workers.length].register(selectedKeyWithSocketChannel);
                    System.out.println("boss invoked worker register");

                    /**
                     * 单worker版本
                     */
                    // 现在需要在worker 中进行SocketChannel 的注册工作
                    //worker.register(selectedKeyWithSocketChannel);
                }

            }
        }
    }
}
