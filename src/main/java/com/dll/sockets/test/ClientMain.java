package com.dll.sockets.test;

import com.dll.sockets.client.Client;
import com.dll.sockets.context.Context;
import com.dll.sockets.proxy.MyInvocationHandler;
import com.dll.sockets.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientMain {
    private static Logger logger = LoggerFactory.getLogger(ClientMain.class);
    private static volatile CountDownLatch countDownLatch = new CountDownLatch(200000);
    private static volatile AtomicInteger atomicInteger = new AtomicInteger(0);

    public static void main(String[] args) {

        Context context = new Context();
        Client client = new Client("client");
        context.register("client", client);
        client.start();
        Service service = (Service) Proxy.newProxyInstance(client.getClass().getClassLoader(),
                new Class[]{Service.class}, new MyInvocationHandler());
        Method method = null;
        try {
            method = Service.class.getMethod("echo");
        } catch (NoSuchMethodException ex) {
            logger.error("", ex);
            return;
        }
        final Method echoMethod = method;
        ExecutorService executorService = Executors.newFixedThreadPool(30);
        for (int i = 0; i < 100000; i++) {
            executorService.execute(() -> {
                String echo = null;
                try {
                    int count = atomicInteger.incrementAndGet();
                    System.out.println("外部执行次数：" + count);
                    Object invoke = echoMethod.invoke(service);
                    if (invoke != null) {
                        echo = (String) invoke;
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.error("", e);
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
            Client.shutdownAll();
            logger.info("finish.");
        } catch (Exception ex) {
            logger.error("", ex);
        }
    }
}
