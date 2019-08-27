package com.dll.sockets.message;

import com.dll.sockets.protocol.TypeLengthContentProtocol;

import java.nio.ByteBuffer;

public class ResponseMessage implements ByteBufferMessage {
    private MessageType type = MessageType.DEFAULT;
    private byte[] token;
    private byte[] object;


    public ByteBuffer toSendByteBuffer() {
        int length = token.length + 4 + object.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(length + 8);
        byteBuffer.put(TypeLengthContentProtocol.defaultProtocol().intToByteArray(this.type.getValue()));
        byteBuffer.put(TypeLengthContentProtocol.defaultProtocol().intToByteArray(length));
        byteBuffer.put(token);
        byteBuffer.put(TypeLengthContentProtocol.defaultProtocol().intToByteArray(object.length));
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
