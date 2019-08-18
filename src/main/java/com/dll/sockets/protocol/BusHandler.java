package com.dll.sockets.protocol;

import com.dll.sockets.message.RequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;

public abstract class BusHandler implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(BusHandler.class);
    private byte[] data;
    private RequestMessage requestMessage;
    private SocketChannel socketChannel;

    public BusHandler(SocketChannel socketChannel, byte[] msg) {
        this.data = msg;
        this.socketChannel = socketChannel;
        this.requestMessage = TypeLengthContentProtocol.defaultProtocol().parseSendMessage(msg);
    }

    @Override
    public void run() {
        dealMsg();
    }

    protected void dealMsg() {
        logger.info(("\n token:" + new String(requestMessage.getToken()) + "\n accessService:" + new String(requestMessage.getAccessService()) + "\n method:" + new String(requestMessage.getMethod())));
    }

    public RequestMessage getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(RequestMessage requestMessage) {
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
}