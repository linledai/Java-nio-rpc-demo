package com.dll.sockets.protocol;

import java.io.*;

public class SerializeProtocol {


    public static byte[] serializeObject(Object object) {
        ByteArrayOutputStream out;
        ObjectOutputStream objectOutputStream;
//        if (object instanceof String) {
//            object = Base64.getEncoder().encode(((String) object).getBytes());
//        }
        try {
            out = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            byte[] bytes = out.toByteArray();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Object deSerializeObject(byte[] object) {
        ByteArrayInputStream in = new ByteArrayInputStream(object);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(in);
            String objectBytes = (String) objectInputStream.readObject();
            return objectBytes;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
