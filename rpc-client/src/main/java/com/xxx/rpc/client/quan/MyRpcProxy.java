package com.xxx.rpc.client.quan;

import com.xxx.rpc.client.RpcProxy;
import com.xxx.rpc.common.bean.RpcRequest;
import com.xxx.rpc.common.bean.RpcResponse;
import com.xxx.rpc.common.util.StringUtil;
import com.xxx.rpc.registry.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @author liuquanquan
 */
public class MyRpcProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private String serviceAddress;


    private ServiceDiscovery serviceDiscovery;

    public MyRpcProxy(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public MyRpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass) {
        return create(interfaceClass, "");
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {

        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 创建 RPC 请求对象并设置请求属性
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setInterfaceName(method.getDeclaringClass().getName());
                        request.setServiceVersion(serviceVersion);
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
                        // 获取 RPC 服务地址
                        if (serviceDiscovery != null) {
                            String serviceName = interfaceClass.getName();
                            if (StringUtil.isNotEmpty(serviceVersion)) {
                                serviceName += "-" + serviceVersion;
                            }
                            serviceAddress = serviceDiscovery.discover(serviceName);
                            LOGGER.debug("discover service: {} => {}", serviceName, serviceAddress);
                        }
                        if (StringUtil.isEmpty(serviceAddress)) {
                            throw new RuntimeException("server address is empty");
                        }
                        // 从 RPC 服务地址中解析主机名与端口号
                        String[] array = StringUtil.split(serviceAddress, ":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        // 创建 RPC 客户端对象并发送 RPC 请求
                        long time = System.currentTimeMillis();
                        FutureResponse futureResponse = new FutureResponse();
                        System.out.println( "request : "  +  request.getRequestId());
                        MyRpcClient.getMyRpcClient().send(host,port,request,futureResponse);
                        LOGGER.debug("time: {}ms", System.currentTimeMillis() - time);
                        RpcResponse rpcResponse = futureResponse.get();
                        return rpcResponse.getResult();
                    }
                }
        );
    }
}
