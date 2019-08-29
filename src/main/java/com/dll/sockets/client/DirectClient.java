package com.dll.sockets.client;

import com.dll.sockets.proxy.DirectInvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;

public class DirectClient extends Client<Object> {

    private static final Logger logger = LoggerFactory.getLogger(DirectClient.class);
    private static final InvocationHandler invocationHandler = new DirectInvocationHandler();

    public DirectClient(String name) {
        this(name, 60);
    }

    public DirectClient(String name, Integer maxQueueSize) {
        super(name, maxQueueSize);
    }

    public Object invokeInternal(String token) throws InterruptedException {
        return new ClientFutureTask(token).call();
    }

    @Override
    public InvocationHandler getInvocationHandler() {
        return invocationHandler;
    }
}
