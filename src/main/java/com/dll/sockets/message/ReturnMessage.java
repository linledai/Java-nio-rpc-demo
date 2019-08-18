package com.dll.sockets.message;

import com.dll.sockets.protocol.Protocol;

import java.nio.ByteBuffer;

public class ReturnMessage {
    private MessageType type = MessageType.STRING;
    private byte[] token;
    private byte[] object;


    public ByteBuffer toSendByteBuffer() {
        int length = token.length + 4 + object.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(length + 8);
        byteBuffer.put(Protocol.defaultProtocol().intToByteArray(this.type.getValue()));
        byteBuffer.put(Protocol.defaultProtocol().intToByteArray(length));
        byteBuffer.put(token);
        byteBuffer.put(Protocol.defaultProtocol().intToByteArray(object.length));
        byteBuffer.put(object);
        byteBuffer.flip();
        return byteBuffer;
    }

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public byte[] getObject() {
        return object;
    }

    public void setObject(byte[] object) {
        this.object = object;
    }
}
