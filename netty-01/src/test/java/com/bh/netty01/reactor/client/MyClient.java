package com.bh.netty01.reactor.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class MyClient {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(10000));
        socketChannel.write(Charset.defaultCharset().encode("hello netty\n"));
        System.out.println("----------------------------------------------");
    }
}
