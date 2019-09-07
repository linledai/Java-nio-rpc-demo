package com.dll.sockets.protocol.serialize;

import com.dll.sockets.protocol.SerializeHandler;
import com.dll.sockets.utils.LoggerUtils;

import java.io.*;

public class JavaSerializeProtocol implements SerializeHandler {

    public byte[] serializeObject(Object object) {
        ByteArrayOutputStream out;
        ObjectOutputStream objectOutputStream;
        try {
            out = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            byte[] bytes = out.toByteArray();
            return bytes;
        } catch (IOException e) {
            LoggerUtils.error(e);
            return null;
        }
    }

    public Object deSerializeObject(byte[] object, Class clazz) {
        ByteArrayInputStream in = new ByteArrayInputStream(object);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(in);
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
