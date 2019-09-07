package com.dll.sockets.base;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ResponseTokenNodeContext implements NodeContext {

    private Map<String, Class> responseTokenClass = new ConcurrentHashMap<>();

    @Override
    public Class getClassByResponseToken(String token) {
        return responseTokenClass.get(token);
    }

    @Override
    public void addClassForResponseToken(String token, Class clazz) {
        responseTokenClass.put(token, clazz);
    }

    @Override
    public void removeResponseToken(String token) {
        responseTokenClass.remove(token);
    }
}
