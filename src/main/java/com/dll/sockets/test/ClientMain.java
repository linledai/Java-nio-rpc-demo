package com.dll.sockets.test;

import com.dll.sockets.client.Client;
import com.dll.sockets.context.Context;
import com.dll.sockets.proxy.DirectInvocationHandler;
import com.dll.sockets.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientMain {
    private static Logger logger = LoggerFactory.getLogger(ClientMain.class);
    private static volatile Integer taskCount = 1000;
    private static volatile CountDownLatch countDownLatch = new CountDownLatch(taskCount);
    private static volatile AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {
        Client client = new Client("client");
        Context.register("client", client);
        client.start();
        Service service = (Service) Proxy.newProxyInstance(client.getClass().getClassLoader(),
                new Class[]{Service.class}, new DirectInvocationHandler());
        Method method;
        try {
            method = Service.class.getMethod("echo");
        } catch (NoSuchMethodException ex) {
            logger.error("", ex);
            return;
        }
        final Method echoMethod = method;
        ExecutorService executorService = Executors.newFixedThreadPool(taskCount);
        for (int i = 0; i < taskCount; i++) {
            executorService.execute(() -> {
                try {
                    Object invoke = echoMethod.invoke(service);
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
            Client.shutdownAll();
        } catch (Exception ex) {
            logger.error("", ex);
        }
        logger.info("finish.");
        Runtime.getRuntime().exit(0);
    }
}
