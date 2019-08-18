package com.dll.sockets.server;

import com.dll.sockets.protocol.ReadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSocketTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ServerSocketTask.class);
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    private SocketChannel socketChannel;
    private ReadHandler readHandler;

    public ServerSocketTask(Server server, SelectionKey key) {
        this.socketChannel = (SocketChannel) key.channel();
        readHandler = new ReadHandler(server, socketChannel, executorService);
    }

    @Override
    public void run() {
        try {
            readHandler.doRead();
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            try {
                this.socketChannel.close();
            } catch (IOException e) {
                logger.warn("Close socket exception");
            }
        }
    }


}
