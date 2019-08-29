package com.dll.sockets.client;

import com.dll.sockets.base.ShutdownNode;
import com.dll.sockets.message.ByteBufferMessage;
import com.dll.sockets.protocol.ReadHandler;
import com.dll.sockets.protocol.TypeLengthContentProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Client<T> implements Runnable, ShutdownNode {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);
    private static final Set<Client> clients = new HashSet<>();

    private final ExecutorService clientExecutor = Executors.newFixedThreadPool(2);
    private final ExecutorService executorServiceRequest = Executors.newFixedThreadPool(1);
    private final ExecutorService executorServiceResult = Executors.newFixedThreadPool(1);
    private final AtomicInteger sendCount = new AtomicInteger(0);
    private final Map<String, Object> requestResultMonitorMapping = new ConcurrentHashMap<>();
    private final Map<String, Object> requestResultMapping = new ConcurrentHashMap<>();
    private final Map<String, ByteBufferMessage> sendedMessage = new ConcurrentHashMap<>();
    private final ThreadLocal<ByteBufferMessage> messageThreadLocal = new ThreadLocal<>();
    private final Semaphore maxClient = new Semaphore(1);
    private final AtomicInteger invokeTimes = new AtomicInteger(0);
    private final Object sendLock = new Object();

    private volatile int retry = 0;
    private volatile boolean shutdown = false;
    private volatile ReadHandler readHandler;
    private volatile SocketChannel socketChannel;

    private LinkedBlockingQueue<ByteBufferMessage> invokeQueue;
    private Semaphore maxUnFinishRequest;

    private String name;

    Client(String name, Integer maxQueueSize) {
        this.name = name;
        this.invokeQueue = new LinkedBlockingQueue<>(maxQueueSize);
        this.maxUnFinishRequest = new Semaphore(maxQueueSize / 2);
        clients.add(this);
    }

    public void start() {
        clientExecutor.execute(() -> {
            while (!shutdown) {
                if (!waitForRetry()) {
                    return;
                }
                try {
                    maxClient.acquire();
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

    private boolean waitForRetry() {
        if (retry > 3) {
            try {
                Thread.sleep((retry - 3) * 5000);
            } catch (InterruptedException e) {
                Thread.interrupted();
                return false;
            }
        }
        return true;
    }

    // TODO Clean old resource
    private void cleanClient() {
    }

    public void run() {
        try {
            socketChannel = SocketChannel.open();
            try {
                socketChannel.connect(new InetSocketAddress("localhost", 80));
            } catch (Exception ex) {
                retry++;
                maxClient.release();
                return;
            }
            retry = 0;
            synchronized (sendLock) {
                sendLock.notify();
            }
            Collection<ByteBufferMessage> requestMessages = sendedMessage.values();
            int retryPackage = 0;
            for (ByteBufferMessage requestMessage : requestMessages) {
                if (requestResultMonitorMapping.get(new String(requestMessage.getToken())) != null) {
                    invokeQueue.add(requestMessage);
                    retryPackage++;
                }
            }

            logger.warn("重试发送的包的数字:" + retryPackage);
            invokeQueue.addAll(requestMessages);
            sendedMessage.clear();
            readHandler = new ReadHandler(this, socketChannel, executorServiceResult, false);
            socketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ);
            executorServiceRequest.execute(() -> {
                boolean isWaitForRetry = false;
                while (!shutdown) {
                    try {
                        if (!waitForRetry()) {
                            return;
                        }
                        if (isWaitForRetry) {
                            ByteBufferMessage requestMessage = messageThreadLocal.get();
                            sendMsg(requestMessage);
                            logger.warn("处理了一次失败的发送." + (requestMessage == null));
                            return;
                        } else {
                            sendMsg(pullInvokeRequest());
                        }
                    } catch (IOException | InterruptedException e) {
                        if (!shutdown) {
                            logger.error("", e);
                        }
                        synchronized (sendLock) {
                            try {
                                sendLock.wait(3000);
                            } catch (InterruptedException e1) {
                                Thread.interrupted();
                                return;
                            }
                        }
                        isWaitForRetry = true;
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
                        try {
                            if (!readHandler.doRead()) {
                                maxClient.release();
                                return;
                            }
                        } catch (IOException ex) {
                            if (!shutdown) {
                                maxClient.release();
                                return;
                            }
                        }
                    }
                }
            }
            logger.info("Shutdown read.");
        } catch (Exception ex) {
            logger.error("", ex);
            logger.info((name + "failed, total" + atomicInteger .incrementAndGet()));
        } finally {
            if (shutdown) {
                executorServiceResult.shutdownNow();
                if (socketChannel != null) {
                    try {
                        socketChannel.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    public T invoke(Class clazz, String method, Object[] args) throws InterruptedException {
        maxUnFinishRequest.acquire();
        final ByteBufferMessage requestMessage = TypeLengthContentProtocol.defaultProtocol().generateRequestMessagePackage(clazz, method, args);
        String token = new String(requestMessage.getToken());
        logger.debug("Invoke 次数：" + invokeTimes.incrementAndGet());
        this.getInvokeQueue().add(requestMessage);
        logger.debug("请求队列大小：" + this.getInvokeQueue().size());
        addMonitor(token);
        return invokeInternal(token);
    }

    protected abstract T invokeInternal(String token) throws InterruptedException;

    Object removeResourceByToken(String token) {
        sendedMessage.remove(token);
        requestResultMonitorMapping.remove(token);
        return requestResultMapping.remove(token);
    }

    private ByteBufferMessage pullInvokeRequest() throws InterruptedException {
        ByteBufferMessage requestMessage = messageThreadLocal.get();
        if (requestMessage != null) {
            return requestMessage;
        }
        requestMessage = invokeQueue.poll(3000, TimeUnit.MILLISECONDS);
        if (requestMessage == null) {
            return null;
        }
        messageThreadLocal.set(requestMessage);
        return requestMessage;
    }

    private void sendMsg(ByteBufferMessage requestMessage) throws IOException {
        if (requestMessage == null) {
            return;
        }
        socketChannel.write(requestMessage.toSendByteBuffer());
        sendedMessage.put(new String(requestMessage.getToken()), requestMessage);
        logger.debug("发送包的计数:" + sendCount.incrementAndGet());
        messageThreadLocal.remove();
    }

    void fillResult(byte[] token, Object result) {
        String tokenKey = new String(token);
        requestResultMapping.put(tokenKey, result);
        Object monitor = requestResultMonitorMapping.remove(tokenKey);
        if (monitor == null) {
            return;
        }
        synchronized (monitor) {
            monitor.notify();
        }
    }

    public static synchronized void shutdownAll() {
        for (Client client : clients) {
            client.shutdown();
        }
    }

    private void addMonitor(String token) {
        Object monitor = new Object();
        requestResultMonitorMapping.put(token, monitor);
    }

    protected Object getMonitor(String token) {
        return requestResultMonitorMapping.get(token);
    }

    protected void finishRequest() {
        maxUnFinishRequest.release();
    }

    protected Object getResponse(String token) {
        return requestResultMapping.get(token);
    }

    protected LinkedBlockingQueue<ByteBufferMessage> getInvokeQueue() {
        return invokeQueue;
    }

    public void shutdown() {
        if (this.shutdown) {
            return;
        }
        this.shutdown = true;
        shutdownInternal();
        clientExecutor.shutdownNow();
        executorServiceRequest.shutdownNow();
        executorServiceResult.shutdownNow();
        requestResultMonitorMapping.clear();
        requestResultMapping.clear();
        invokeQueue.clear();
        clients.remove(this);
    }

    protected void shutdownInternal() {
    }

    @Override
    public boolean isShutdown() {
        return this.shutdown;
    }

    public String getName() {
        return name;
    }
}
