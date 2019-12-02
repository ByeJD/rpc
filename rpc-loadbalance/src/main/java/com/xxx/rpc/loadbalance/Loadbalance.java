package com.xxx.rpc.loadbalance;

import java.util.HashSet;

public interface Loadbalance {
    String route(String serviceName, HashSet<String> addressSet);
}
