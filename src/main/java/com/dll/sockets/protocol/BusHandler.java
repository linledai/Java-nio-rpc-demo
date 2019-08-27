package com.dll.sockets.protocol;

import com.dll.sockets.base.ShutdownNode;
import com.dll.sockets.message.ByteBufferMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;

public abstract class BusHandler implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(BusHandler.class);
    private byte[] data;
    private ByteBufferMessage requestMessage;
    private SocketChannel socketChannel;
    private ShutdownNode node;

    public BusHandler(ShutdownNode node, SocketChannel socketChannel, byte[] msg) {
        this.node = node;
        this.data = msg;
        this.socketChannel = socketChannel;
        this.requestMessage = TypeLengthContentProtocol.defaultProtocol().parseRequestMessagePackage(msg);
    }

    @Override
    public void run() {
        try {
            dealMsg();
        } catch (Throwable throwable) {
            logger.error("", throwable);
        }
    }

    protected void dealMsg() throws Throwable {
    }

    public ByteBufferMessage getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(ByteBufferMessage requestMessage) {
        this.requestMessage = requestMessage;
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
