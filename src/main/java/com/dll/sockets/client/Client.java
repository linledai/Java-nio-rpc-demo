package com.dll.sockets.client;

import com.dll.sockets.base.ShutdownNode;
import com.dll.sockets.message.Message;
import com.dll.sockets.protocol.Protocol;
import com.dll.sockets.protocol.ReadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Client implements Runnable, ShutdownNode {

    private static Logger logger = LoggerFactory.getLogger(Client.class);
    private static AtomicInteger atomicInteger = new AtomicInteger(0);
    private static ExecutorService executorServiceRequest = Executors.newFixedThreadPool(2);
    private static ExecutorService executorServiceResult = Executors.newFixedThreadPool(2);

    private volatile boolean shutdown = false;
    private String name;
    private LinkedBlockingQueue<Message> invokeQueue;
    private static Map<String, Object> requestResultFutureMapping = new HashMap<>();
    private static Map<String, Object> requestResultMapping = new HashMap<>();
    private static Set<Client> clients = new HashSet<>();

    public Client(String name) {
        this(name, 100);
    }

    public Client(String name, int cap) {
        this.name = name;
        this.invokeQueue = new LinkedBlockingQueue<>(cap);
        clients.add(this);
    }

    public void run() {
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", 80));
            socketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ);
            final SocketChannel thisSocketChannel = socketChannel;
            executorServiceRequest.execute(() -> {
                while (!shutdown) {
                    try {
                        sendMsg(thisSocketChannel, pullInvokeRequest());
                    } catch (IOException | InterruptedException e) {
                        logger.error("", e);
                    }
                }
                logger.info("Shutdown send.");
            });
            while (!shutdown) {
                int select = selector.select();
                Set<SelectionKey> keys = selector.keys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {
                        ReadHandler readHandler = new ReadHandler(this, thisSocketChannel, executorServiceResult, false);
                        readHandler.doRead();
                    }
                }
            }
            logger.info("Shutdown read.");
        } catch (Exception ex) {
            logger.error("", ex);
            logger.info((name + "failed, total" + atomicInteger .incrementAndGet()));
        } finally {
            if (socketChannel != null) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public Future<Object> invoke(Class clazz, String method) {
        final Message message = Protocol.defaultProtocol().generateSendMessage(clazz, method);
        String token = new String(message.getToken());
        this.invokeQueue.add(message);
        requestResultFutureMapping.put(token, new Object());
        return executorServiceResult.submit(() -> {
            Object response = requestResultMapping.get(token);
            if (response != null) {
                return response;
            }
            synchronized (requestResultFutureMapping.get(token)) {
                requestResultFutureMapping.get(token).wait();
            }
            return requestResultMapping.get(token);
        });
    }

    private ByteBuffer pullInvokeRequest() throws InterruptedException {
        Message message = invokeQueue.poll(3000, TimeUnit.MILLISECONDS);
        if (message == null) {
            return null;
        }
        return message.toSendByteBuffer();
    }

    private void sendMsg(SocketChannel socketChannel, ByteBuffer writeBuffer) throws IOException {
        if (writeBuffer == null) {
            return;
        }
        socketChannel.write(writeBuffer);
    }

    public static void fillResult(byte[] token, Object result) {
        String tokenKey = new String(token);
        requestResultMapping.put(tokenKey, result);
        Object objectFuture = requestResultFutureMapping.get(tokenKey);
        synchronized (objectFuture) {
            objectFuture.notify();
        }
    }

    public static void shutdownAll() {
        for (Client client : clients) {
            client.shutdown();
        }
    }


    public void shutdown() {
        this.shutdown = true;
    }

    @Override
    public boolean isShutdown() {
        return this.shutdown;
    }
}
