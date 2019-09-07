package com.dll.sockets.protocol.serialize;

import com.dll.sockets.protocol.SerializeHandler;

public class SerializeFactory {

    private static volatile SerializeHandler serializeHandler;

    public static SerializeHandler buildSerializeHandler() {
        if (serializeHandler == null) {
            synchronized (SerializeFactory.class) {
                if (serializeHandler == null) {
                    serializeHandler = new FastJsonSerializeProtocol();
                }
            }
        }
        return serializeHandler;
    }
}
