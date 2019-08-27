package com.dll.sockets.message;

public enum MessageType {
    DEFAULT(9);

    private int value;

    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
