package com.bh.netty01.web;

/**
 * <h1>网络的相关概念</h1>
 * <ol>
 *     <li>概念：两台设备直接通过网络实现数据传输</li>
 *     <li>网络通信：将数据通过网络从一台设备传输到另外一台设备</li>
 *     <li>java.net包下提供了一系列的类或接口</li>
 * </ol>
 * <h2>网络</h2>
 * <ol>
 *     <li>概念:两台或多台设备通过一定无力设备连接起来构成了网络</li>
 *     <li>根据网络的覆盖范围不同，对网络进行分类</li>
 *     <ul>
 *         <li>局域网：覆盖范围最小，仅仅覆盖一个教师或一个机房</li>
 *         <li>广域网：覆盖范围大，可以覆盖全国，甚至全球，万维网是广域网的代表</li>
 *     </ul>
 * </ol>
 *
 * <h2>IP 地址</h2>
 * <ol>
 *     <li>概念：用于唯一标志网络中的每台计算机/主机</li>
 *     <li>查看IP地址：ifconfig</li>
 *     <li>ip地址的表示形式：点十分进制 xxx.xxx.xxx.xxx</li>
 *     <li>每一个十进制的范围：0～255</li>
 *     <li>IP 地址的组成=网络地址+主机地址 比如 192.168.200.1</li>
 *     <li>由于ipv4最大的问题在于网络地址资源优先，严重制约了互联网的应用和发展，ipv6 的使用，不仅可以解决网络地址资源数量优先的问题，而且也解决了多种接入设备联入网络的障碍</li>
 * </ol>
 *
 *
 * <h2>Socket</h2>
 * <img width="640" height="320" src="../../../../../../../../README.assets/image-20221102002011655.png">
 * <ol>
 *     <li>套接字（Socket）开发网络应用程序被广泛的采用，以至于成为事实上的标准</li>
 *     <li>通信的两端都需要有Socket，是两台机器间通信的端点</li>
 *     <li>网络通信其实就是Socket 间的通信</li>
 *     <li>Socket允许程序把网络连接当成一个流，数据在两个Socket间通过IO传输</li>
 *     <li>一般主动发起通信的应用程序属于客户端，等待通信请求的为服务器</li>
 *     <li>Socket的理解</li>
 *     <ul>
 *         <li>TCP编程 =》可靠</li>
 *         <li>UDP编程 -》不可靠</li>
 *     </ul>
 * </ol>
 *
 * <h2>TCP网络通信编程</h2>
 * <ol>
 *     <li>基于科幻端--服务器的网络通信</li>
 *     <li>底层使用的是TCP/IP协议</li>
 *     <li>应用场景举例：客户端发送数据，服务端接收并显示</li>
 *     <li>基于Socket的TCP编程</li>
 * </ol>
 */
public class a {
    public static void main(String[] args) {

    }
}
