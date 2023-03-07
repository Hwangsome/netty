package com.bh.netty01.web.tcp.client;

import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPUploadClient {
    public static void main(String[] args) throws IOException {
        final Socket socket = new Socket();
        socket.connect(new InetSocketAddress(9999));

        final OutputStream outputStream = socket.getOutputStream();

    }
}
