package com.dll.sockets.protocol;

import com.dll.sockets.message.ReturnMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;

public class ReturnBusHandler implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ReturnBusHandler.class);
    private byte[] data;
    private ReturnMessage message;
    private SocketChannel socketChannel;

    public ReturnBusHandler(SocketChannel socketChannel, byte[] msg) {
        this.data = msg;
        this.socketChannel = socketChannel;
        this.message = Protocol.defaultProtocol().parseReturnMessage(msg);
    }

    @Override
    public void run() {
        dealMsg();
    }

    protected void dealMsg() {
        logger.info("\n token:" + new String(message.getToken()));
    }

    public ReturnMessage getMessage() {
        return message;
    }

    public void setMessage(ReturnMessage message) {
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
