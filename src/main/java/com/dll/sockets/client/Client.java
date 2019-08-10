package com.dll.sockets.client;

import com.dll.sockets.message.Message;
import com.dll.sockets.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Client implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(Client.class);
    private static Integer maxThread = 1;
    private static AtomicInteger atomicInteger = new AtomicInteger(maxThread);
    private String name;
    private static CountDownLatch countDownLatch = new CountDownLatch(maxThread);

    public Client(String name) {
        this.name = name;
    }
    public static void main(String[] args) {
        int totalThread = 0;
        while (totalThread < maxThread) {
            Thread thread = new Thread(new Client("client" + totalThread));
            thread.start();
            totalThread ++;
        }
        try {
            countDownLatch.await();
            logger.info("finish.");
        } catch (Exception ex) {
            logger.error("", ex);
        }
    }

    public void run() {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress("localhost", 80));
            Random random = new Random();
            int count = 10;
            while (count > 0) {
                int randomNum = random.nextInt(100000);
                sendMsg(socketChannel, wrapMsg(randomNum + name));
                count --;
            }
            Thread.sleep(10000);
            socketChannel.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.info((name + "failed, total" + atomicInteger .incrementAndGet()));
        }
        countDownLatch.countDown();
    }

    private ByteBuffer wrapMsg(String msg) {
        Message message = Protocol.defaultProtocol().generateMessage(this.getClass(), msg);
        return message.toByteBuffer();
    }

    private void sendMsg(SocketChannel socketChannel, ByteBuffer writeBuffer) throws IOException {
        socketChannel.write(writeBuffer);
    }
}
