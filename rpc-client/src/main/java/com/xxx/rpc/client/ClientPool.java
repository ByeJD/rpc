package com.xxx.rpc.client;

import java.util.concurrent.ConcurrentHashMap;

public class ClientPool {


    public static ConcurrentHashMap<String, MyRpcClient> connectClientMap;


    public static MyRpcClient getClient(String key) {
        if (connectClientMap == null) {
            synchronized (ClientPool.class) {
                if (connectClientMap == null) {
                    // init
                    connectClientMap = new ConcurrentHashMap<>();
                    // stop callback todo: 关闭所有的client
                }
            }
        }

        MyRpcClient myRpcClient = connectClientMap.get(key);


        return null;

    }
}
