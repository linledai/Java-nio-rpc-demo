package com.dll.sockets.client;

import com.dll.sockets.proxy.DirectInvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureClient extends Client<Future<Object>> {

    private static final Logger logger = LoggerFactory.getLogger(FutureClient.class);

    private static final Integer DEFAULT_THREAD = 30;
    private static final InvocationHandler invocationHandler = new DirectInvocationHandler();

    private ExecutorService executorServiceInvoke;

    public FutureClient(String name) {
        super(name, 2 * DEFAULT_THREAD);
        this.executorServiceInvoke = Executors.newFixedThreadPool(DEFAULT_THREAD);
    }

    public Future<Object> invokeInternal(String token) throws InterruptedException {
        return executorServiceInvoke.submit(() -> {
            Object response = getResponse(token);
            if (response != null) {
                removeResourceByToken(token);
                finishRequest();
                return response;
            }
            Object monitor = getMonitor(token);
            try {
                synchronized (monitor) {
                    monitor.wait();
                }
            } catch (InterruptedException e) {
                logger.error("", e);
                Thread.interrupted();
            } finally {
                response = removeResourceByToken(token);
                finishRequest();
            }
            return response;
        });
    }

    public void shutdownInternal() {
        this.executorServiceInvoke.shutdownNow();
    }

    @Override
    public InvocationHandler getInvocationHandler() {
        return invocationHandler;
    }
}
