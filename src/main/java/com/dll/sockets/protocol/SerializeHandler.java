package com.dll.sockets.protocol;

public interface SerializeHandler {

    byte[] serializeObject(Object object);

    Object deSerializeObject(byte[] object, Class clazz);
}
