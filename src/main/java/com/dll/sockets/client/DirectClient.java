package com.dll.sockets.client;

import com.dll.sockets.proxy.DirectInvocationHandler;

import java.lang.reflect.InvocationHandler;

public class DirectClient extends Client<Object> {

    private static final InvocationHandler invocationHandler = new DirectInvocationHandler();

    public DirectClient(String name) {
        this(name, 30);
    }

    public DirectClient(String name, Integer maxConcurrentRequest) {
        super(name, maxConcurrentRequest);
    }

    public Object invokeInternal(ClientTask clientTask) throws InterruptedException {
        return clientTask.call();
    }

    @Override
    public InvocationHandler getInvocationHandler() {
        return invocationHandler;
    }
}
