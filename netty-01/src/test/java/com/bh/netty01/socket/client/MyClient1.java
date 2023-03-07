package com.bh.netty01.socket.client;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * 模拟多个客户端
 */
public class MyClient1 {

    public static void main(String[] args) throws IOException {
        final SocketChannel socketChannel = SocketChannel.open();
        // 根据ip和端口号与服务端进行连接
        // 端口号与协议挂钩，不同的协议的相同端口号不会冲突
        // SocketChannel用来与服务端建立连接，服务端使用serverSocketChannel.accept();接收客户端发送的数据。
        socketChannel.connect(new InetSocketAddress(9999));
        System.out.println("-----------------------------------");
        socketChannel.write(Charset.defaultCharset().encode("hello\nnetty\n"));
        // 一条消息字节高于ByteBuffer的情况。服务端的设置：ByteBuffer byteBuffer = ByteBuffer.allocate(7);
        socketChannel.write(Charset.defaultCharset().encode("hello netty\n"));

    }

}
