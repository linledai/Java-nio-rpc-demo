package com.dll.sockets.server;

import com.dll.sockets.protocol.ReadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSocketTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ServerSocketTask.class);
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    private static volatile Map<SocketChannel, ReadHandler> socketContext = new ConcurrentHashMap<>();
    private final SocketChannel socketChannel;
    private volatile ReadHandler readHandler;
    private volatile SelectionKey selectionKey;

    public ServerSocketTask(Server server, SelectionKey key) {
        selectionKey = key;
        this.socketChannel = (SocketChannel) key.channel();
        ReadHandler mappingReadHandler = socketContext.get(socketChannel);
        synchronized (ServerSocketTask.class) {
            if (mappingReadHandler == null) {
                this.readHandler = new ReadHandler(server, socketChannel, executorService);
                socketContext.put(socketChannel, this.readHandler);
            } else {
                this.readHandler = mappingReadHandler;
            }
        }
    }

    @Override
    public void run() {
        try {
            readHandler.doRead();
            this.selectionKey.interestOps(SelectionKey.OP_READ);
        } catch (Exception e) {
            logger.error("", e);
            try {
                this.socketChannel.close();
            } catch (IOException ex) {
                logger.warn("Close socket exception");
            }
        }
    }


}
