package com.bh.netty01.reactor.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class Worker implements Runnable{
    private String name;
    private Thread thread;
    private Selector selector;
    private volatile boolean isCreated = false;

    public Worker(String name) {
        this.name = name;
    }

    public void register(SocketChannel selectedKeyWithSocketChannel) throws IOException {
        if (!isCreated) {
            thread = new Thread(this, this.name);
            thread.start();
            selector = Selector.open();
            this.isCreated = true ;
        }
        selectedKeyWithSocketChannel.register(selector, SelectionKey.OP_READ,null);
    }

    @Override
    public void run() {

        while (true) {
            try {
                selector.select();

                final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    final SelectionKey selectionKeyWithSocketChannel = iterator.next();
                    iterator.remove();

                    if (selectionKeyWithSocketChannel.isReadable()) {
                        final SocketChannel socketChannel = (SocketChannel) selectionKeyWithSocketChannel.channel();
                        final ByteBuffer byteBuffer = ByteBuffer.allocate(30);
                        final int read = socketChannel.read(byteBuffer);
                        if (read == -1) {
                            selectionKeyWithSocketChannel.cancel();
                        }else {
                            byteBuffer.flip();
                            String result = Charset.defaultCharset().decode(byteBuffer).toString();
                            System.out.println("result = " + result);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
