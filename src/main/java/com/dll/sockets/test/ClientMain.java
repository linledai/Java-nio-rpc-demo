package com.dll.sockets.test;

import com.dll.sockets.client.Client;
import com.dll.sockets.client.DirectClient;
import com.dll.sockets.client.FutureClient;
import com.dll.sockets.context.Context;
import com.dll.sockets.proxy.DirectInvocationHandler;
import com.dll.sockets.proxy.TimeOutInvocationHandler;
import com.dll.sockets.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientMain {
    private static final Logger logger = LoggerFactory.getLogger(ClientMain.class);
    private static final Integer taskCount = 30000;
    private static final Integer threadCount = 150;
    private static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {
        Client directClient = new DirectClient("directClient");
        Client futureClient = new FutureClient("futureClient");
        Context.register("directClient", directClient);
        Context.register("futureClient", futureClient);
        long test1Begin = System.currentTimeMillis();
        testClient(directClient);
        long test1End = System.currentTimeMillis();
        testClient(futureClient);
        long test2End = System.currentTimeMillis();
        logger.info("DirectClient cost:" + (test1End - test1Begin));
        logger.info("FutureClient cost:" + (test2End - test1End));
        Runtime.getRuntime().exit(0);
    }

    private static void testClient(Client client) {
        CountDownLatch countDownLatch = new CountDownLatch(taskCount);
        InvocationHandler invocationHandler;
        if (client instanceof DirectClient) {
            invocationHandler = new DirectInvocationHandler();
        } else {
            invocationHandler = new TimeOutInvocationHandler();
        }
        client.start();
        Service service = (Service) Proxy.newProxyInstance(client.getClass().getClassLoader(),
                new Class[]{Service.class}, invocationHandler);
        Method method;
        try {
            method = Service.class.getMethod("echo", Integer.class);
        } catch (NoSuchMethodException ex) {
            logger.error("", ex);
            return;
        }
        final Method echoMethod = method;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < taskCount; i++) {
            final Integer echoCount = i + 1;
            executorService.execute(() -> {
                try {
                    Object invoke = echoMethod.invoke(service, echoCount);
                    if (invoke != null) {
                        String echo = (String) invoke;
                        logger.info(count.incrementAndGet() + " Response message:" + echo);
                    } else {
                        logger.error(count.incrementAndGet() + " Response message is null.");
                    }
                } catch (Throwable e) {
                    logger.error("", e);
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await(100000, TimeUnit.MILLISECONDS);
            executorService.shutdownNow();
            client.shutdown();
            Context.deRegister(client.getName());
        } catch (Exception ex) {
            logger.error("", ex);
        }
        logger.info("finish.");
    }
}
