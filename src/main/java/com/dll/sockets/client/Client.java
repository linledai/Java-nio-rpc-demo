package com.dll.sockets.client;

import com.dll.sockets.base.ShutdownNode;
import com.dll.sockets.message.RequestMessage;
import com.dll.sockets.protocol.ReadHandler;
import com.dll.sockets.protocol.TypeLengthContentProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Client implements Runnable, ShutdownNode {

    private static Logger logger = LoggerFactory.getLogger(Client.class);
    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    private static volatile Set<Client> clients = new HashSet<>();

    private ExecutorService clientExecutor = Executors.newFixedThreadPool(2);
    private ExecutorService executorServiceRequest = Executors.newFixedThreadPool(1);
    // TODO 该线程池必须大于请求线程池，否则会死锁。
    private ExecutorService executorServiceInvoke = Executors.newFixedThreadPool(30);
    private ExecutorService executorServiceResult = Executors.newFixedThreadPool(1);
    private String name;
    private AtomicInteger sendCount = new AtomicInteger(0);

    private volatile boolean shutdown = false;
    private volatile AtomicInteger invokeTimes = new AtomicInteger(0);
    private volatile LinkedBlockingQueue<RequestMessage> invokeQueue;
    private volatile Map<String, Object> requestResultFutureMapping = new ConcurrentHashMap<>();
    private volatile Map<String, Object> requestResultMapping = new ConcurrentHashMap<>();
    private volatile ReadHandler readHandler;
    private volatile SocketChannel socketChannel;
    private volatile Semaphore semaphore = new Semaphore(1);

    public Client(String name) {
        this(name, 100000);
    }

    public Client(String name, int cap) {
        this.name = name;
        this.invokeQueue = new LinkedBlockingQueue<>(cap);
        clients.add(this);
    }

    public void start() {
        clientExecutor.execute(() -> {
            while (!shutdown) {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    if (!this.shutdown) {
                        logger.error("", e);
                    }
                    return;
                }
                cleanClient();
                clientExecutor.execute(Client.this);
            }
        });
    }

    // TODO Clean old resource
    private void cleanClient() {
    }

    public void run() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", 80));
            socketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ);
            executorServiceRequest.execute(() -> {
                while (!shutdown) {
                    try {
                        sendMsg(pullInvokeRequest());
                    } catch (IOException | InterruptedException e) {
                        if (!shutdown) {
                            logger.error("", e);
                        }
                    }
                }
                logger.info("Shutdown send.");
                invokeQueue.clear();
                executorServiceRequest.shutdownNow();
            });
            while (!shutdown) {
                selector.select();
                Set<SelectionKey> keys = selector.keys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {
                        if (readHandler == null) {
                            synchronized (this) {
                                if (readHandler == null) {
                                    readHandler = new ReadHandler(this, socketChannel, executorServiceResult, false);
                                }
                            }
                        }
                        if (!readHandler.doRead()) {
                            semaphore.release();
                            return;
                        }
                    }
                }
            }
            logger.info("Shutdown read.");
        } catch (Exception ex) {
            logger.error("", ex);
            logger.info((name + "failed, total" + atomicInteger .incrementAndGet()));
        } finally {
            executorServiceResult.shutdownNow();
            if (socketChannel != null) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public Future<Object> invoke(Class clazz, String method) {
        final RequestMessage requestMessage = TypeLengthContentProtocol.defaultProtocol().generateSendMessage(clazz, method);
        String token = new String(requestMessage.getToken());
        logger.debug("Invoke 次数：" + invokeTimes.incrementAndGet());
        this.invokeQueue.add(requestMessage);
        logger.debug("请求队列大小：" + invokeQueue.size());
        final Object monitor = new Object();
        requestResultFutureMapping.put(token, monitor);
        return executorServiceInvoke.submit(() -> {
            Object response = requestResultMapping.get(token);
            if (response != null) {
                return response;
            }
            synchronized (monitor) {
                monitor.wait();
            }
            return requestResultMapping.remove(token);
        });
    }

    public Object invokeDirect(Class clazz, String method) {
        final RequestMessage requestMessage = TypeLengthContentProtocol.defaultProtocol().generateSendMessage(clazz, method);
        String token = new String(requestMessage.getToken());
        logger.debug("Invoke 次数：" + invokeTimes.incrementAndGet());
        this.invokeQueue.add(requestMessage);
        logger.debug("请求队列大小：" + invokeQueue.size());
        final Object monitor = new Object();
        requestResultFutureMapping.put(token, monitor);
        Object response = requestResultMapping.get(token);
        if (response != null) {
            return response;
        }
        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.interrupted();
                return null;
            }
        }
        return requestResultMapping.get(token);
    }

    private ByteBuffer pullInvokeRequest() throws InterruptedException {
        RequestMessage requestMessage = invokeQueue.poll(3000, TimeUnit.MILLISECONDS);
        if (requestMessage == null) {
            return null;
        }
        return requestMessage.toSendByteBuffer();
    }

    private void sendMsg(ByteBuffer writeBuffer) throws IOException {
        if (writeBuffer == null) {
            return;
        }
        logger.debug("发送包的计数:" + sendCount.incrementAndGet());
        socketChannel.write(writeBuffer);
    }

    public void fillResult(byte[] token, Object result) {
        String tokenKey = new String(token);
        requestResultMapping.put(tokenKey, result);
        Object objectFuture = requestResultFutureMapping.get(tokenKey);
        if (objectFuture == null) {
            return;
        }
        synchronized (objectFuture) {
            objectFuture.notify();
        }
        requestResultFutureMapping.remove(tokenKey);
    }

    public static synchronized void shutdownAll() {
        for (Client client : clients) {
            client.shutdown();
        }
    }


    public void shutdown() {
        if (this.shutdown) {
            return;
        }
        this.shutdown = true;
        clientExecutor.shutdownNow();
        executorServiceInvoke.shutdownNow();
        executorServiceRequest.shutdownNow();
        executorServiceResult.shutdownNow();
        requestResultFutureMapping.clear();
        requestResultMapping.clear();
        invokeQueue.clear();
        clients.clear();
    }

    @Override
    public boolean isShutdown() {
        return this.shutdown;
    }
}
