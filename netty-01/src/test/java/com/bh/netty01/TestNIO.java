package com.bh.netty01;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TestNIO {

    @Test
    public void test01() throws IOException {
        // 创建channel通道 FileChannel
        final FileChannel channel = new FileInputStream("/Users/bhuang/IdeaProjects/netty/netty-01/data.txt").getChannel();

        // 2. 创建缓冲区 此时的缓冲区是写模式，在新建一个缓冲区的时候，他是写模式
        // 如果你的文件中的字节超过10 ，剩下的内容不会读取不出来
        /**
         * 怎么解决剩下的内容不会读取不出来？
         * buffer不够用，复用buffer
         * 循环去读取buffer
         */
        final ByteBuffer buffer = ByteBuffer.allocate(10);

        //3。 把通道内获取的文件数据写入缓冲区
        final int read = channel.read(buffer);

        // 4。 程序读取buffer的内容，后续的操作，设置buffer 为读模式
        buffer.flip();

        // 5。 循环读取缓冲区的数据
        while(buffer.hasRemaining()){
            final byte b = buffer.get();
            System.out.println("b = " + (char)b);
        }

        // 6. 重新设置buffer为写模式
        buffer.clear();
    }

    /**
     * 怎么解决剩下的内容不会读取不出来？
     * buffer不够用，复用buffer
     * 循环去读取buffer
     */
    @Test
    public void test02() throws IOException {
        final FileChannel channel = new FileInputStream("/Users/bhuang/IdeaProjects/netty/netty-01/data.txt").getChannel();
        final ByteBuffer buffer = ByteBuffer.allocate(10);

        boolean flag = true;
        /**
         * 循环读取
         */
        while(flag) {
        final int read = channel.read(buffer);
        if (read == -1) flag =false;

        buffer.flip();

        while(buffer.hasRemaining()){
            final byte b = buffer.get();
            System.out.println("b = " + (char)b);
        }
        // 这行不能省略，当重复利用缓冲区的时候，需要将buffer重新修改为写模式
        buffer.clear();
        }
    }

    /**
     * 使用RandomAccessFile 创建channel
     * @throws IOException
     */
    @Test
    public void test03() throws IOException {
        final FileChannel channel = new RandomAccessFile("/Users/bhuang/IdeaProjects/netty/netty-01/data.txt", "rw").getChannel();
        final ByteBuffer buffer = ByteBuffer.allocate(10);

        boolean flag = true;
        /**
         * 循环读取
         */
        while(flag) {
            final int read = channel.read(buffer);
            if (read == -1) flag =false;

            buffer.flip();

            while(buffer.hasRemaining()){
                final byte b = buffer.get();
                System.out.println("b = " + (char)b);
            }
            // 这行不能省略，当重复利用缓冲区的时候，需要将buffer重新修改为写模式
            buffer.clear();
        }
    }

    @Test
    public void test04() throws IOException {
        final FileChannel channel = FileChannel.open(Paths.get("/Users/bhuang/IdeaProjects/netty/netty-01/data.txt"), StandardOpenOption.READ);
        final ByteBuffer buffer = ByteBuffer.allocate(10);

        boolean flag = true;
        /**
         * 循环读取
         */
        while(flag) {
            final int read = channel.read(buffer);
            if (read == -1) flag =false;

            buffer.flip();

            while(buffer.hasRemaining()){
                final byte b = buffer.get();
                System.out.println("b = " + (char)b);
            }
            // 这行不能省略，当重复利用缓冲区的时候，需要将buffer重新修改为写模式
            buffer.clear();
        }
    }


    @Test
    public void test05(){
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a', 'b', 'c', 'd'});

        buffer.flip();
        System.out.println("buffer.get() = " + (char) buffer.get());//a
        System.out.println("buffer.get() = " + (char) buffer.get());//b
        buffer.mark();
        System.out.println("buffer.get() = " + (char) buffer.get());//c
        System.out.println("buffer.get() = " + (char) buffer.get());//d
        buffer.reset();
        System.out.println("buffer.get() = " + (char) buffer.get());//c
        System.out.println("buffer.get() = " + (char) buffer.get());//d
    }

}
