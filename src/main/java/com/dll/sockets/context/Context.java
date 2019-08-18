package com.dll.sockets.context;

import java.util.HashMap;
import java.util.Map;

public class Context {

    public static Map<String, Object> map = new HashMap<>();

    public static void register(String serviceName, Object bean) {
        map.put(serviceName, bean);
    }

    public static Object getBean(String name) {
        return map.get(name);
    }
}
