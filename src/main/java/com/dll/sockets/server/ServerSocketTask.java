package com.dll.sockets.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSocketTask implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ServerSocketTask.class);
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    private SocketChannel socketChannel;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    public ServerSocketTask(SelectionKey key) {
        this.socketChannel = (SocketChannel) key.channel();
    }

    @Override
    public void run() {
        try {
             doRead();
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

    private void doRead() throws Exception {
        int remain = 0;
        boolean skip =false;
        int length = 0;
        byte[] dataContent = null;
        while (true) {
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
                        remain --;
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
            System.arraycopy(dataContent,0, content, 0, dataContent.length);
        }
        executorService.execute(new BusHandler(content));
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
            length --;
            byteBufferTL.put(byteBuffer.get());
        }
        byteBufferTL.flip();
        int type = byteBufferTL.getInt();
        length = byteBufferTL.getInt();
        return length;
    }
}
