package com.bh.netty01.web.tcp.server;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * <h2></h2>
 * <ol>
 *     <li>编写一个服务器端和一个客户端</li>
 *     <li>服务器端在9999端口监听</li>
 *     <li>客户端连接到服务器端，发送"hello" 然后退出</li>
 *     <li>服务器端接收到客户端发送的信息，输出并退出</li>
 * </ol>
 *
 * <h2>服务端思路</h2>
 * <ol>
 *     <li>在本机的9999端口监听，等待连接</li>
 *     <li>当没有客户端连接9999端口的时候，程序会阻塞等待连接</li>
 *     <li>通过socket.getInputStream()读取客户端写入到数据通道的数据，显示</li>
 * </ol>
 */
@Slf4j
public class TCPServer {

    public static void main(String[] args) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(9999);

            final Socket accept = serverSocket.accept();
            final InputStream inputStream = accept.getInputStream();

            //IO读取
//            final byte[] bytes = new byte[1024];
//            int readLine = 0;
//
//            while ((readLine = inputStream.read(bytes)) != -1) {
//                log.info("接收到的客户端的数据：\t" + new String(bytes, 0, readLine));
//            }

            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            final String s = bufferedReader.readLine();
            log.info("接收到的客户端的数据:\t"+s);

            // 获取socket相关联的输出流
            final OutputStream outputStream = accept.getOutputStream();
            final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write("hello client");
            bufferedWriter.newLine();
            bufferedWriter.flush();



        //关闭流和socket
        bufferedWriter.close();
        bufferedReader.close();
        accept.close();
        serverSocket.close();

    }
}
