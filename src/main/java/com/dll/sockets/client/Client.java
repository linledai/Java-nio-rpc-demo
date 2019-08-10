package com.dll.sockets.client;

import com.dll.sockets.message.Message;
import com.dll.sockets.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Client implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(Client.class);
    private static Integer maxThread = 1;
    private static AtomicInteger atomicInteger = new AtomicInteger(maxThread);
    private String name;
    private static CountDownLatch countDownLatch = new CountDownLatch(1);
    private LinkedBlockingQueue<Message> invokeQueue;

    public Client(String name) {
        this(name, 100);
    }

    public Client(String name, int cap) {
        this.name = name;
        this.invokeQueue = new LinkedBlockingQueue<>(cap);
    }
    public static void main(String[] args) {
        int totalThread = 0;
        Client client = new Client("client" + totalThread);
        Thread thread = new Thread(client);
        thread.start();
        client.invoke(client.getClass(), "testMethod");
        try {
            countDownLatch.await();
            logger.info("finish.");
        } catch (Exception ex) {
            logger.error("", ex);
        }
    }

    public void run() {
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", 80));
            while (true) {
                sendMsg(socketChannel, pullInvokeRequest());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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

    public void invoke(Class clazz, String method) {
        Message message = Protocol.defaultProtocol().generateMessage(clazz, method);
        this.invokeQueue.add(message);
    }

    private ByteBuffer pullInvokeRequest() throws InterruptedException {
        Message message = invokeQueue.take();
        return message.toByteBuffer();
    }

    private void sendMsg(SocketChannel socketChannel, ByteBuffer writeBuffer) throws IOException {
        socketChannel.write(writeBuffer);
    }
}
