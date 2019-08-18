package com.dll.sockets.protocol;

import com.dll.sockets.base.ShutdownNode;
import com.dll.sockets.message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;

public class ReturnBusHandler implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ReturnBusHandler.class);
    private byte[] data;
    private ResponseMessage message;
    private SocketChannel socketChannel;
    private ShutdownNode node;

    public ReturnBusHandler(ShutdownNode node, SocketChannel socketChannel, byte[] msg) {
        this.data = msg;
        this.socketChannel = socketChannel;
        this.message = TypeLengthContentProtocol.defaultProtocol().parseReturnMessage(msg);
        this.node = node;
    }

    @Override
    public void run() {
        dealMsg();
    }

    protected void dealMsg() {
        logger.info("\n token:" + new String(message.getToken()));
    }

    public ResponseMessage getMessage() {
        return message;
    }

    public void setMessage(ResponseMessage message) {
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

    public ShutdownNode getNode() {
        return node;
    }

    public void setNode(ShutdownNode node) {
        this.node = node;
    }
}
