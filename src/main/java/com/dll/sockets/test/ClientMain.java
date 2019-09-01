package com.dll.sockets.test;

import com.dll.sockets.client.Client;
import com.dll.sockets.client.DirectClient;
import com.dll.sockets.client.FutureClient;
import com.dll.sockets.context.Context;
import com.dll.sockets.service.Service;
import com.dll.sockets.service.ServiceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
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
        Context.register("directService", new ServiceBean(directClient, Service.class));
        Context.register("futureService", new ServiceBean(futureClient, Service.class));
        directClient.start();
        futureClient.start();
        long test1Begin = System.currentTimeMillis();
        testService("futureService");
        long test1End = System.currentTimeMillis();
        testService("directService");
        long test2End = System.currentTimeMillis();
        logger.warn("DirectClient cost:" + (test1End - test1Begin));
        logger.warn("FutureClient cost:" + (test2End - test1End));
        Runtime.getRuntime().exit(0);
    }

    private static void testService(String serviceBeanName) {
        CountDownLatch countDownLatch = new CountDownLatch(taskCount);
        Service service = (Service) Context.getBean(serviceBeanName);
        Method method;
        try {
            method = service.getClass().getMethod("echoTest");
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
                    Object invoke = echoMethod.invoke(service);
                    if (invoke != null) {
                        String echo = (String) invoke;
                        logger.debug(count.incrementAndGet() + " Response message:" + echo);
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
        } catch (Exception ex) {
            logger.error("", ex);
        }
        logger.info("finish.");
    }
}
