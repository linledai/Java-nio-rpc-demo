package com.dll.sockets.message;

import com.dll.sockets.protocol.TypeLengthContentProtocol;
import com.dll.sockets.protocol.serialize.SerializeFactory;

import java.nio.ByteBuffer;

public class SerializableRequestMessage implements ByteBufferMessage {

    private MessageType type = MessageType.DEFAULT;
    private byte[] token;
    private String accessService;
    private String methodName;
    private Object[] args;

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public String getAccessService() {
        return accessService;
    }

    public void setAccessService(String accessService) {
        this.accessService = accessService;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public ByteBuffer toSendByteBuffer() {
        byte[] bytes = SerializeFactory.buildSerializeHandler().serializeObject(this);
        int length = bytes.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(length + 8);
        byteBuffer.put(TypeLengthContentProtocol.defaultProtocol().intToByteArray(this.type.getValue()));
        byteBuffer.put(TypeLengthContentProtocol.defaultProtocol().intToByteArray(length));
        byteBuffer.put(bytes);
        byteBuffer.flip();
        return byteBuffer;
    }
}
