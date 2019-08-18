package com.dll.sockets.server;

import com.dll.sockets.base.ShutdownNode;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements ShutdownNode {
    private ServerSocketChannel serverSocketChannel;
    private volatile boolean shutdown = false;

    public void start() {
        try {
            run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void run() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(80));
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        while (true) {
            selector.select();
            Iterator<SelectionKey> it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                if (key.isValid()) {
                    if (key.isAcceptable()) {
                        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    } else if (key.isReadable()) {
                        key.interestOps(SelectionKey.OP_WRITE);
                        executorService.execute(new ServerSocketTask(this, key));
                        // Thread.yield();
                    }
                }
            }
        }
    }

    public void shutdown() {
        this.shutdown = true;
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }
}
