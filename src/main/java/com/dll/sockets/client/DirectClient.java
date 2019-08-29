package com.dll.sockets.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectClient extends Client<Object> {

    private static final Logger logger = LoggerFactory.getLogger(DirectClient.class);

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
}
