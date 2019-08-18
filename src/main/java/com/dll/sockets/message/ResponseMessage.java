package com.dll.sockets.message;

public class ResponseMessage {

    private byte[] token;

    private byte[] data;


    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
