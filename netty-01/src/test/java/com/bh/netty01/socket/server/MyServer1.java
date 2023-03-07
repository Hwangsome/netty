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
 *
 * 这个服务端存在的两个阻塞：
 * 1。 客户端与服务端建立连接的时候阻塞
 * 2。 服务端去读取客户端传输的数据阻塞
 */
public class MyServer1 {
    public static void main(String[] args) throws IOException {

        // 创建ServerSocketChannel
        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设置服务端的监听端口 client通过网络进行访问 http://localhost:9999
        serverSocketChannel.bind(new InetSocketAddress(9999));

        // 每个客户端与这个服务端连接都需要一个socketChannel，有很多客户端来连接这个服务端，所以需要一个集合来管理这个SocketChannel
        final List<SocketChannel> socketChannels = new ArrayList<>();

        final ByteBuffer byteBuffer = ByteBuffer.allocate(30);

        // 接受client的连接
        while (true) {

            System.out.println("等待客户端连接服务器...");

            // 服务端与客户端连接的一个通道
            // 阻塞等待当前的这个线程与客户端连接
            final SocketChannel socketChannel = serverSocketChannel.accept();

            System.out.println("服务器已连接...\t"+socketChannel);
            socketChannels.add(socketChannel);

            // 基于socketChannels 通信
            // 客户端与服务端通信过程的nio代码
            for (SocketChannel channel : socketChannels) {
                System.out.println("开始实际的数据通信...");
                // 这里会每次等待第一个连接结束后，才会去处理第二个的客户端连接
                channel.read(byteBuffer); // 阻塞 对应的io通信的阻塞

                // 切换 byteBuffer 为读模式
                byteBuffer.flip();

                final CharBuffer charBuffer = Charset.forName("UTF-8").decode(byteBuffer);
                System.out.println("charBuffer.toString() = " + charBuffer.toString());
                byteBuffer.clear();
                System.out.println("数据通信结束...");
            }
        }
    }
}
