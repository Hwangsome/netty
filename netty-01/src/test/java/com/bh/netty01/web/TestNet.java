package com.bh.netty01.web;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class TestNet {
    @Test
    public void testInetAddress() throws UnknownHostException {
        // 获取本机的InetAddress 对象
        final InetAddress localHost = InetAddress.getLocalHost();
        //localHost = EXPC02Z8114LVCH/192.168.132.147
        System.out.println("localHost = " + localHost);


        // 根据指定主机名/域名，获取InetAddress对象
        final InetAddress host = InetAddress.getByName("EXPC02Z8114LVCH");
        //host = EXPC02Z8114LVCH/192.168.132.147
        System.out.println("host = " + host);

        // 通过InetAddress对象 获取对应的地址
        final String hostAddress = host.getHostAddress();
        //hostAddress = 192.168.132.147
        System.out.println("hostAddress = " + hostAddress);

        // 通过 InetAddress 对象获取对应的主机名/或者域名
        final String hostName = host.getHostName();
        //hostName = EXPC02Z8114LVCH
        System.out.println("hostName = " + hostName);

    }
}
