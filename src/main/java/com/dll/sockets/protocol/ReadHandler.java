package com.dll.sockets.protocol;

import com.dll.sockets.base.ShutdownNode;
import com.dll.sockets.client.ClientBusHandler;
import com.dll.sockets.server.ServerBusHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ReadHandler {

    private static Logger logger = LoggerFactory.getLogger(ReadHandler.class);

    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private SocketChannel socketChannel;
    private AtomicInteger atomicInteger = new AtomicInteger(0);
    private volatile boolean server = false;
    ExecutorService executorService;
    ShutdownNode node;

    public ReadHandler(ShutdownNode node, SocketChannel socketChannel, ExecutorService executorService) {
        this(node, socketChannel, Executors.newFixedThreadPool(1), true);
    }

    public ReadHandler(ShutdownNode node, SocketChannel socketChannel, Boolean server) {
        this(node, socketChannel, Executors.newFixedThreadPool(1), server);
    }

    public ReadHandler(ShutdownNode node, SocketChannel socketChannel, ExecutorService executorService, Boolean server) {
        this.socketChannel = socketChannel;
        this.executorService = executorService;
        this.server = server;
        this.node = node;
    }

    public void doRead() throws Exception {
        if (socketChannel == null) {
            throw new NullPointerException("SocketChannel can not be null.");
        }
        int remain = 0;
        boolean skip = false;
        int length = 0;
        byte[] dataContent = null;
        while (!node.isShutdown()) {
//            Thread.yield();
            if (remain < 9 || remain < length) {
                int read = remain;
                if (read == 0) {
                    byteBuffer.clear();
                    read = socketChannel.read(byteBuffer);
                    if (read == -1) {
                        break;
                    } else if (read == 0) {
                        Thread.sleep(20);
                        continue;
                    }
                    byteBuffer.flip();
                } else {
                    dataContent = new byte[remain];
                    int index = 0;
                    while (remain > 0) {
                        dataContent[index++] = byteBuffer.get();
                        remain--;
                    }
                    byteBuffer.clear();
                    socketChannel.read(byteBuffer);
                    byteBuffer.flip();
                }
            }
            if (!skip) {
                length = readTypelength(dataContent, byteBuffer);
                if (dataContent != null && dataContent.length > 8) {
                    byte[] newDataContent = new byte[dataContent.length - 8];
                    System.arraycopy(dataContent, 8, newDataContent, 0, dataContent.length - 8);
                    dataContent = newDataContent;
                } else {
                    dataContent = null;
                }
            }
            byte[] content = new byte[length];
            if (dataContent != null) {
                if (byteBuffer.remaining() + dataContent.length < length) {
                    remain = byteBuffer.remaining();
                    skip = true;
                    continue;
                }
            } else {
                if (byteBuffer.remaining() < length) {
                    remain = byteBuffer.remaining();
                    skip = true;
                    continue;
                }
            }
            remain = readMessage(content, dataContent);
            dataContent = null;
            skip = false;
        }
    }

    private int readMessage(byte[] content, byte[] dataContent) {
        if (dataContent == null) {
            byteBuffer.get(content);
        } else {
            byteBuffer.get(content, dataContent.length, content.length - dataContent.length);
            System.arraycopy(dataContent, 0, content, 0, dataContent.length);
        }

        logger.debug("解析包的计数：" + atomicInteger.incrementAndGet());
        if (server) {
            executorService.execute(new ServerBusHandler(node, socketChannel, content));
        } else {
            executorService.execute(new ClientBusHandler(node, socketChannel, content));
        }
        return byteBuffer.remaining();
    }

    private int readTypelength(byte[] data, ByteBuffer byteBuffer) {
        java.nio.ByteBuffer byteBufferTL = java.nio.ByteBuffer.allocate(8);
        int length = 8;
        if (data != null) {
            if (data.length > 8) {
                byteBufferTL.put(data, 0, 8);
            } else {
                byteBufferTL.put(data);
            }
            length = 8 - data.length;
        }
        while (length > 0) {
            length--;
            byteBufferTL.put(byteBuffer.get());
        }
        byteBufferTL.flip();
        int type = byteBufferTL.getInt();
        length = byteBufferTL.getInt();
        return length;
    }
}
