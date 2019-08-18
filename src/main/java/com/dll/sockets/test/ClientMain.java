package com.dll.sockets.test;

import com.dll.sockets.client.Client;
import com.dll.sockets.context.Context;
import com.dll.sockets.service.Service;
import com.dll.sockets.service.impl.ServiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientMain {
    private static Logger logger = LoggerFactory.getLogger(ClientMain.class);

    public static void main(String[] args) {

        Context context = new Context();
        Client client = new Client("client");
        context.register("client", client);
        client.start();
        Service service = new ServiceProxy();
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch countDownLatch = new CountDownLatch(20);
        for (int i = 0; i < 20; i++) {
            executorService.execute(() -> {
                String echo = service.echo();
                System.out.println(echo);
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
