package com.xxx.rpc.client.quan;


import com.xxx.rpc.common.bean.RpcRequest;
import com.xxx.rpc.common.bean.RpcResponse;
import com.xxx.rpc.common.codec.RpcDecoder;
import com.xxx.rpc.common.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1. 使用长连接的方式实现同步调用，huangyong老师使用的是短链接: 完成
 * 2. 实现回调
 * 3. 实现channel池调用，可以复用channel：完成
 * 4. 实现心跳检测机制 idleHandle
 * 5. 实现负载均衡调用
 * 6. 实现注册中心的检测机制,服务端不可用,客户端移除连接
 */
public class MyRpcClient {

    private EventLoopGroup group;
    private  static volatile MyRpcClient rpcClient;

    private static Map<String, FutureResponse> futureResponseMap = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, Channel> map = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<String, Object> mapLock = new ConcurrentHashMap<>();

    public static MyRpcClient getMyRpcClient(){
        if (rpcClient == null){
            synchronized (MyRpcClient.class){
                if (rpcClient == null){
                    rpcClient = new MyRpcClient();
                }
            }
        }

        return rpcClient;
    }

    private MyRpcClient() {

    }


    private Channel init(String host, int port) {
        try {
            // 创建并初始化 Netty 客户端 Bootstrap 对象
            Bootstrap bootstrap = new Bootstrap();
            group =  new NioEventLoopGroup();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new RpcEncoder(RpcRequest.class)); // 编码 RPC 请求
                    pipeline.addLast(new RpcDecoder(RpcResponse.class)); // 解码 RPC 响应
                    pipeline.addLast(new ChannelHandler(futureResponseMap)); // 处理 RPC 响应
                }
            });
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            // 连接 RPC 服务器
            Channel channel = bootstrap.connect(host, port).sync().channel();
            map.put(host + port, channel);
            System.out.println("init");
            return channel;
            // todo: 判断channel合法性
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Channel getChannel(String host, int port) throws InterruptedException {

        String key = host + port;
        System.out.println("key Port : " + key);
        Channel channel = map.get(key);

        if (channel != null && channel.isActive()) {
            return channel;
        }

        Object lockObject = mapLock.get(key);
        if (lockObject == null) {
            mapLock.putIfAbsent(key, new Object());
            lockObject = mapLock.get(key);
        }

        // 这里针对每一个url创建一个锁对象,双重校验
        synchronized (lockObject) {
            channel = map.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            }

            // channel不是在激活状态，但是他有可能不为空，如果不为空的话，同步关闭掉。
            if (channel != null) {
                channel.close().sync();
                map.remove(key);
            }

            channel = init(host, port);
            if (channel == null || !channel.isActive()) {
                if (channel != null) {
                    channel.close().sync();
                }
                throw new RuntimeException("failed");
            }

            return channel;
        }
    }


    public void send(String host, int port, RpcRequest request, FutureResponse futureResponse) throws InterruptedException {
        Channel channel = getChannel(host, port);
        // futureResponseMap.put(request.getRequestId(), futureResponse); 和 channel.writeAndFlush(request).sync();能不能调换？
        // 答案： 不能，如果先调用发送请求,服务器端返回，这个时候futureResponseMap的put动作尚未完成,在ChannelHandler处理的话,可能会出现
        // 空指针异常的情况。
        futureResponseMap.put(request.getRequestId(), futureResponse);
        channel.writeAndFlush(request).sync();
    }
}
