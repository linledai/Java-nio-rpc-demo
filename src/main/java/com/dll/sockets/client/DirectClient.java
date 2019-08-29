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
        Object response = getResponse(token);
        if (response != null) {
            removeResourceByToken(token);
            finishRequest();
            return response;
        }
        Object monitor = getMonitor(token);
        synchronized (monitor) {
            try {
                monitor.wait(30000);
            } catch (InterruptedException e) {
                logger.error("", e);
                Thread.interrupted();
            } finally {
                response = removeResourceByToken(token);
                finishRequest();
            }
        }
        return response;
    }

    @Override
    public InvocationHandler getInvocationHandler() {
        return invocationHandler;
    }
}
