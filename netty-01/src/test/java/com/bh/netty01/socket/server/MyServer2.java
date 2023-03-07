package com.bh.netty01.socket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 网络通信过程中的服务端
 * ServerSocketChannel
 * 解决阻塞：
 *         serverSocketChannel.configureBlocking(false);
 *         socketChannel.configureBlocking(false);
 *
 *         现在这个server还是有问题，因为while (true) {} 一直在循环，空转，会导致我们的机器的cpu占用率很高
 *         我们需要一个管理者，这个管理者的作用是监控连接，io
 *
 *         1。 谁来充当监管者
 *         2。 监管谁？
 *         3。 怎么来监管？
 *
 *         Selector:
 *         监管谁？ ==》accept read write
 *
 *         Accept ==> ServerSocketChannel
 *         Read,Write ==> SocketChannel
 *
 *         思考：
 *         什么时候去监管？ 当触发特定的状态的时候才会被Selector监管
 *
 *         怎么来监管？ 目的就是不要使用while(true) ,
 *         当你的客户端进行连接的时候做一些操作
 *         当你的客户端进行读/写的时候进行一些操作
 *
 *
 *
 */
public class MyServer2 {
    public static void main(String[] args) throws IOException {

        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(9999));

        final List<SocketChannel> socketChannels = new ArrayList<>();

        final ByteBuffer byteBuffer = ByteBuffer.allocate(20);

        while (true) {

            // serverSocketChannel.configureBlocking(false);配置了serverSocketChannel 为非阻塞，当你的服务端没有客户端进行连接的时候，accept不会阻塞.
            // 当你没有客户端进行连接的时候，socketChannel 返回值为null
            final SocketChannel socketChannel = serverSocketChannel.accept();
            // 配置不需要等待第一个客户端连接结束后，再处理第二个客户端连接
            // 只有当你有客户端进行连接的时候，你才把客户端的连接加入到list中
            if (socketChannel != null) {
                System.out.println(socketChannel);
                socketChannel.configureBlocking(false);
                socketChannels.add(socketChannel);
            }

            for (SocketChannel channel : socketChannels) {
                final int read = channel.read(byteBuffer);
                // 只有当读取到客户端的数据的时候
                if (read > 0) {
                    System.out.println("开始实际的数据通信...");
                    byteBuffer.flip();
                    final CharBuffer charBuffer = Charset.forName("UTF-8").decode(byteBuffer);
                    System.out.println("charBuffer.toString() = " + charBuffer.toString());
                    byteBuffer.clear();
                    System.out.println("数据通信结束...");
                }


            }
        }
    }
}
