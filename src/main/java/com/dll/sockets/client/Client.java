package com.dll.sockets.client;

import com.dll.sockets.base.ShutdownNode;
import com.dll.sockets.message.ByteBufferMessage;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Client<T> implements Runnable, ShutdownNode, InvocationHandlerClient {

    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);
    private static final Set<Client> clients = new HashSet<>();

    private final ExecutorService clientExecutor = Executors.newFixedThreadPool(2);
    private final ExecutorService executorServiceRequest = Executors.newFixedThreadPool(1);
    private final ExecutorService executorServiceResult = Executors.newFixedThreadPool(1);
    private final AtomicInteger sendCount = new AtomicInteger(0);
    private final Map<String, Object> requestResultMonitor = new ConcurrentHashMap<>();
    private final Map<String, Object> requestResultMapping = new ConcurrentHashMap<>();
    private final Map<String, ByteBufferMessage> sendedMessage = new ConcurrentHashMap<>();
    private final ThreadLocal<List<ByteBufferMessage>> messageThreadLocal = new ThreadLocal<>();
    private final Semaphore maxClient = new Semaphore(1);
    private final AtomicInteger invokeTimes = new AtomicInteger(0);
    private final AtomicInteger nullFillResult = new AtomicInteger(0);
    private final Object sendLock = new Object();

    private volatile int retry = 0;
    private volatile boolean shutdown = false;
    private volatile ReadHandler readHandler;
    private volatile SocketChannel socketChannel;

    private LinkedBlockingQueue<ByteBufferMessage> invokeQueue;
    private Semaphore maxUnFinishRequest;
    private int maxConcurrentRequest;
    private String name;

    Client(String name, Integer maxConcurrentRequest) {
        this.name = name;
        this.invokeQueue = new LinkedBlockingQueue<>(maxConcurrentRequest * 2);
        this.maxConcurrentRequest = maxConcurrentRequest;
        this.maxUnFinishRequest = new Semaphore(maxConcurrentRequest);
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
                if (requestResultMonitor.get(new String(requestMessage.getToken())) != null) {
                    getInvokeQueue().add(requestMessage);
                    retryPackage++;
                }
            }

            logger.warn("重试发送的包的数字:" + retryPackage);
            getInvokeQueue().addAll(requestMessages);
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
                            List<ByteBufferMessage> requestMessage = messageThreadLocal.get();
                            sendMsg(requestMessage);
                            logger.warn("处理了一次失败的发送." + (requestMessage == null || requestMessage.isEmpty()));
                            return;
                        } else {
                            sendMsg(pullInvokeRequests());
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
                getInvokeQueue().clear();
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
        return invokeInternal(new ClientTask(token, requestMessage));
    }

    abstract protected T invokeInternal(ClientTask clientTask) throws InterruptedException;

    private Object removeResourceByToken(String token) {
        sendedMessage.remove(token);
        requestResultMonitor.remove(token);
        return requestResultMapping.remove(token);
    }

    private List<ByteBufferMessage> pullInvokeRequests() throws InterruptedException {
        List<ByteBufferMessage> requestMessages = messageThreadLocal.get();
        if (requestMessages != null && requestMessages.size() > 0) {
            return requestMessages;
        } else {
            requestMessages = new ArrayList<>();
        }
        ByteBufferMessage requestMessage = getInvokeQueue().poll(3000, TimeUnit.MILLISECONDS);
        if (requestMessage == null) {
            return null;
        }
        requestMessages.add(requestMessage);
        for (int i = 1; i < maxConcurrentRequest; i++) {
            ByteBufferMessage byteBufferMessage = getInvokeQueue().poll();
            if (byteBufferMessage == null) {
                break;
            } else {
                requestMessages.add(byteBufferMessage);
            }
        }
        messageThreadLocal.set(requestMessages);
        return requestMessages;
    }

    private void sendMsg(List<ByteBufferMessage> requestMessages) throws IOException {
        if (requestMessages == null) {
            return;
        }
        ByteBuffer[] byteBuffers = new ByteBuffer[requestMessages.size()];
        int packetSize = 0;
        for (int i = 0; i < requestMessages.size(); i++) {
            ByteBuffer byteBuffer = requestMessages.get(i).toSendByteBuffer();
            byteBuffers[i] = byteBuffer;
            packetSize += byteBuffer.remaining();
        }
        long write = 0L;
        while (write != packetSize) {
            write += socketChannel.write(byteBuffers);
        }
        for (ByteBufferMessage requestMessage : requestMessages) {
            sendedMessage.put(new String(requestMessage.getToken()), requestMessage);
            sendCount.incrementAndGet();
        }
        logger.debug("发送包的计数:" + sendCount.get());
        messageThreadLocal.remove();
    }

    private Object addMonitor(String token) {
        requestResultMonitor.put(token, new Object());
        return requestResultMonitor.get(token);
    }

    void fillResult(byte[] token, Object result) {
        String tokenKey = new String(token);
        requestResultMapping.put(tokenKey, result);
        Object monitor = requestResultMonitor.remove(tokenKey);
        if (monitor == null) {
            logger.warn(tokenKey + "未获取返回值.");
            logger.warn("找不到对应线程数量:" + nullFillResult.incrementAndGet());
            return;
        }
        synchronized (monitor) {
            monitor.notify();
        }
    }

    public static void shutdownAll() {
        for (Client client : clients) {
            client.shutdown();
        }
    }

    private void finishRequest() {
        maxUnFinishRequest.release();
    }

    private LinkedBlockingQueue<ByteBufferMessage> getInvokeQueue() {
        return invokeQueue;
    }

    public synchronized void shutdown() {
        if (this.shutdown) {
            return;
        }
        this.shutdown = true;
        shutdownInternal();
        clientExecutor.shutdownNow();
        executorServiceRequest.shutdownNow();
        executorServiceResult.shutdownNow();
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

    public class ClientTask implements Callable<Object> {

        private String token;
        private ByteBufferMessage requestMessage;

        public ClientTask(String token, ByteBufferMessage requestMessage) {
            this.token = token;
            this.requestMessage = requestMessage;
        }

        @Override
        public Object call() {
            Object response;
            Object monitor = addMonitor(token);
            synchronized (monitor) {
                try {
                    logger.debug("Invoke 次数：" + invokeTimes.incrementAndGet());
                    getInvokeQueue().add(requestMessage);
                    logger.debug("请求队列大小：" + getInvokeQueue().size());
                    monitor.wait();
                } catch (InterruptedException e) {
                    logger.error("", e);
                } finally {
                    response = removeResourceByToken(token);
                    if (response == null) {
                        logger.warn(token + "未获取返回值.");
                    }
                }
            }
            finishRequest();
            return response;
        }
    }
}
