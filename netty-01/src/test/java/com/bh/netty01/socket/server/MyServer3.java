package com.bh.netty01.socket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Selector 的两个属性
 * keys:
 *  注册的channel
 *
 * SelectionKeys：
 *  实实在在发生了这些状态的channel
 *
 *    selector.select();
 *    SelectionKeys 发生了特定状态的channel ==》处理
 *
 *    Set<SelectionKey> keys();
 *    Set<SelectionKey> selectedKeys();
 *
 *    当client 连接服务器，发起操作之后，服务器必须全部处理完成
 *    整个交互才算结束，如果没有全部处理完成，select方法就会被一直调用
 *
 *    1。 数据没有被处理完整前，select()方法就会被调用多次了
 *    2。 在某些特殊情况下（突然对客户端进行关闭处理），服务器端无法处理，那么select()就会被调用多次了
 *    SelectionKey 下有一个cancel方法，等同于处理了client这个特殊情况，避免再次调用select()
 */
public class MyServer3 {
    public static void main(String[] args) throws IOException {

        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(9999));

        // selector只有在非阻塞的情况下才可以使用
        serverSocketChannel.configureBlocking(false);

        // 引入监管者 Selector
        final Selector selector = Selector.open();

        // 管理谁？serverSocketChannel注册到Selector
        // 他关注的是serverSocketChannel 的连接时候的状态
        // 一旦被注册，都会把被防止在Selector的keys属性中,keys 是 HashSet类型的
        final SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, null);
        // 对ACCEPT 时的状态感兴趣
        // 最终的结果是： Selector 中有个Hashset keys； register 注册到ServerSocketChannel

        // 这一行也可以写进register()方法的第二个参数中
        // selectionKey.interestOps(SelectionKey.OP_ACCEPT);
        System.out.println("selector：\t"+selector);
        // 开始监控
        while (true) {
            // 等待 只有监控到了 有实际的连接 或者 读写操作，才会处理
            // 一旦发现了实际的连接和读写操作的时候就将对应的有ACCEPT状态的serverSocketChannel 和 read write 状态的SocketChannel存起来
            // 存到 selectionKey hashSet
            // select 监控后，把实际存在的serverSocketChannel 和 read write 状态的SocketChannel存起来
            selector.select();

            /**
             * 增强for不能删除操作
             * 迭代器可以删除操作
             */
            final Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                final SelectionKey selectionKeyBindChannel = iterator.next();

                // 这些事件用完之后就要把他从selectedKeys 集合中移除掉，否则下一次还会触发这个事件
                iterator.remove();

                if (selectionKeyBindChannel.isAcceptable()) {
                    final ServerSocketChannel channel = (ServerSocketChannel) selectionKeyBindChannel.channel();
                    final SocketChannel socketChannel = channel.accept();
                    socketChannel.configureBlocking(false);

                    // 监控socketChannel的状态
                    /**
                     * 将channel与ByteBuffer 绑定
                     * 防止ByteBuffer  太小，导致数据丢失的问题
                     *
                     * 如何保证前后2次的操作的ByteBuffer 是同一个？
                     * ByteBuffer 和谁有关？ ==》channel,将ByteBuffer 和channel绑定
                     *
                     * 怎么绑定？
                     * final SelectionKey socketChannelSelectionKey = socketChannel.register(selector, 0, byteBuffer);
                     */
                    final ByteBuffer byteBuffer = ByteBuffer.allocate(7);
                    final SelectionKey socketChannelSelectionKey = socketChannel.register(selector, 0, byteBuffer);
                    socketChannelSelectionKey.interestOps(SelectionKey.OP_READ);
                }else if (selectionKeyBindChannel.isReadable()) {
                    try {
                        final SocketChannel socketChannel = (SocketChannel) selectionKeyBindChannel.channel();
                        //final ByteBuffer byteBuffer = ByteBuffer.allocate(7);
                        //从channel中获取绑定的ByteBuffer
                        final ByteBuffer byteBuffer = (ByteBuffer)selectionKeyBindChannel.attachment();
                        final int read = socketChannel.read(byteBuffer);

                        // 客户端关闭会写一个-1 过来， select() 就会监控到，就会进入到isReadable 分支
                        if (read == -1 ){
                            // 我们需要对这个关闭操作 进行处理
                            selectionKeyBindChannel.cancel();
                        }else {
                            doLineSplit(byteBuffer);

                            // ByteBuffer需要扩容了
                            /**
                             * 缓冲区的扩容问题：
                             */
                            if (byteBuffer.position() == byteBuffer.limit()) {
                                // 1. 空间扩大
                                final ByteBuffer newByteBuffer = ByteBuffer.allocate(byteBuffer.capacity() * 2);
                                //2. 老的缓冲区的数据 ==》移动到新扩容的缓冲区中
                                byteBuffer.flip();

                                newByteBuffer.put(byteBuffer);

                                //3. channel --byteBuffer 绑定newBuffer
                                selectionKeyBindChannel.attach(newByteBuffer);

                            }
//                            byteBuffer.flip();
//                            System.out.println("客户端发送的内容：\t"+Charset.defaultCharset().decode(byteBuffer).toString());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        // 有异常的时候， 对这个处理为cancel
                        selectionKeyBindChannel.cancel();
                    }

                }
            }
        }

    }

    /**
     * 解决半包/粘包的问题
     * @param buffer
     *
     * 网络通信过程中才需要处理半包 粘包，采用的方式通过特殊字符进行信息的分割
     * 1。 channel和bytebuffer绑定的问题 attachemnt
     * 2. bytebuffer容量不够的问题，扩容的问题
     */
    // ByteBuffer接受的数据 \n
    private static void doLineSplit(ByteBuffer buffer) {
        buffer.flip();
        for (int i = 0; i < buffer.limit(); i++) {
            if (buffer.get(i) == '\n') {
                int length = i + 1 - buffer.position();
                ByteBuffer target = ByteBuffer.allocate(length);
                for (int j = 0; j < length; j++) {
                    target.put(buffer.get());
                }

                //截取工作完成
                target.flip();
                System.out.println("StandardCharsets.UTF_8.decode(target).toString() = " + StandardCharsets.UTF_8.decode(target).toString());
            }
        }
        buffer.compact();
    }
}
