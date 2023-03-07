package com.bh.netty01.web.tcp.server;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * <h2>网络上传文件</h2>
 * <ol>
 *     <li>客户端连接到服务端，发送一张某个路径下的图片</li>
 *     <li>服务端接收到客户端的图片，保存到resources，发送"收到图片"再退出</li>
 *     <li>客户端收到服务端发送的"收到图片"再退出</li>
 * </ol>
 */
public class TCPUploadServer {

    public static void main(String[] args) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(9999);

    }
}
