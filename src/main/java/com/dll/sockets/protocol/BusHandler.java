package com.dll.sockets.protocol;

import com.dll.sockets.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;

public class BusHandler implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(BusHandler.class);
    private byte[] data;
    private Message message;
    private SocketChannel socketChannel;

    public BusHandler(SocketChannel socketChannel, byte[] msg) {
        this.data = msg;
        this.socketChannel = socketChannel;
        this.message = Protocol.defaultProtocol().parseSendMessage(msg);
    }

    @Override
    public void run() {
        dealMsg();
    }

    protected void dealMsg() {
        logger.info(("\n token:" + new String(message.getToken()) + "\n accessService:" + new String(message.getAccessService()) + "\n method:" + new String(message.getMethod())));
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
