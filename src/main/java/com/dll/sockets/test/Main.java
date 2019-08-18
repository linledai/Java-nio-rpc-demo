package com.dll.sockets.test;

import com.dll.sockets.client.Client;
import com.dll.sockets.service.Service;
import com.dll.sockets.service.impl.ServiceProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Service service = new ServiceProxy();
        String echo = service.echo();
        System.out.println(echo);
        Client.shutdownAll();
        try {
            logger.info("finish.");
        } catch (Exception ex) {
            logger.error("", ex);
        }
    }
}
