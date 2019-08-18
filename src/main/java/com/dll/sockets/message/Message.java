package com.dll.sockets.message;

import com.dll.sockets.protocol.Protocol;

import java.nio.ByteBuffer;

public class Message {
    private MessageType type = MessageType.STRING;
    private byte[] token;
    private byte[] accessService;
    private byte[] method;

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public byte[] getAccessService() {
        return accessService;
    }

    public void setAccessService(byte[] accessService) {
        this.accessService = accessService;
    }

    public byte[] getMethod() {
        return method;
    }

    public void setMethod(byte[] method) {
        this.method = method;
    }

    public ByteBuffer toSendByteBuffer() {
        int length = token.length + 4 + accessService.length + method.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(length + 8);
        byteBuffer.put(Protocol.defaultProtocol().intToByteArray(this.type.getValue()));
        byteBuffer.put(Protocol.defaultProtocol().intToByteArray(length));
        byteBuffer.put(token);
        byteBuffer.put(Protocol.defaultProtocol().intToByteArray(accessService.length));
        byteBuffer.put(accessService);
        byteBuffer.put(method);
        byteBuffer.flip();
        return byteBuffer;
    }
}
