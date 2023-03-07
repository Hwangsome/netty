package com.bh.netty01.web.tcp.client;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * <ol>
 *     <li>连接服务端</li>
 *     <li>连接后，生成socket,通过 socket.getOutputStream()</li>
 *     <li>通过输出流，写入数据到数据通道</li>
 * </ol>
 */
@Slf4j
public class TCPClient {
    public static void main(String[] args) throws IOException {
        final Socket socket = new Socket();
        socket.connect(new InetSocketAddress(9999));
        //得到和socket关联的输出流对象
        final OutputStream outputStream = socket.getOutputStream();

        final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

        bufferedWriter.write("hello server");
        // 插入一个换行符， 表示写入的内容结束，注意，要求对方使用readLine()
        bufferedWriter.newLine();
        // r如果使用了字符流，需要手动刷新，否则数据不会写入数据通道
        bufferedWriter.flush();



        // 获取和socket关联的输入流，读取数据，并显示
        final InputStream inputStream = socket.getInputStream();
        final byte[] bytes = new byte[1024];
        int readLine = 0;

        while ((readLine = inputStream.read(bytes)) != -1) {
            log.info("接收到的服务端的数据：\t" + new String(bytes, 0, readLine));
        }

        // 关闭流对象 和socket 必须关闭
        outputStream.close();
        log.info("客户端退出...");
    }
}
