package com.xxx.rpc.loadbalance;

import java.util.HashSet;

public class RandomLoadBanlance implements Loadbalance {
    @Override
    public String route(String serviceName, HashSet<String> addressSet) {

        return null;
    }
}
