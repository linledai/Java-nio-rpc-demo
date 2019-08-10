package com.dll.sockets.message;

import com.dll.sockets.protocol.Protocol;

import java.nio.ByteBuffer;

public class Message {

    private Integer length;
    private Integer type;
    private byte[] token;
    private byte[] accessService;
    private byte[] args;

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

    public byte[] getArgs() {
        return args;
    }

    public void setArgs(byte[] args) {
        this.args = args;
    }

    public ByteBuffer toByteBuffer() {
        this.type = 9;
        this.length = token.length + 4 + accessService.length + args.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(this.length + 8);
        byteBuffer.put(Protocol.defaultProtocol().intToByteArray(type));
        byteBuffer.put(Protocol.defaultProtocol().intToByteArray(length));
        byteBuffer.put(token);
        byteBuffer.put(Protocol.defaultProtocol().intToByteArray(accessService.length));
        byteBuffer.put(accessService);
        byteBuffer.put(args);
        byteBuffer.flip();
        return byteBuffer;
    }
}
