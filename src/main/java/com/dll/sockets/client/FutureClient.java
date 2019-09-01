package com.dll.sockets.client;

import com.dll.sockets.proxy.DirectInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureClient extends Client<Future<Object>> {

    private static final Integer DEFAULT_THREAD = 30;
    private static final InvocationHandler invocationHandler = new DirectInvocationHandler();

    private ExecutorService executorServiceInvoke;

    public FutureClient(String name) {
        super(name, DEFAULT_THREAD);
        this.executorServiceInvoke = Executors.newFixedThreadPool(DEFAULT_THREAD);
    }

    public Future<Object> invokeInternal(ClientTask clientTask) throws InterruptedException {
        return executorServiceInvoke.submit(clientTask);
    }

    public void shutdownInternal() {
        this.executorServiceInvoke.shutdownNow();
    }

    @Override
    public InvocationHandler getInvocationHandler() {
        return invocationHandler;
    }
}
