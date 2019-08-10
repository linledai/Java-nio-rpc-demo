package com.dll.sockets.protocol;

import com.dll.sockets.message.Message;

import java.util.UUID;

public class Protocol {

    public static Protocol defaultProtocol() {
        return new Protocol();
    }

    public byte[] intToByteArray(int data) {
        byte[] result = new byte[4];
        result[0] = (byte) ((data & 0xFF000000) >> 24);
        result[1] = (byte) ((data & 0x00FF0000) >> 16);
        result[2] = (byte) ((data & 0x0000FF00) >> 8);
        result[3] = (byte) (data & 0x000000FF);
        return result;
    }

    public int byteArrayToInt(byte[] data) {
        return (data[0] << 24) + (data[1] << 16) +(data[2] << 8) +data[3];
    }

    public Message parse(byte[] msg) {
        Message message = new Message();
        byte[] token = new byte[10];
        System.arraycopy(msg, 0, token, 0, 10);
        byte[] lengthArr = new byte[4];
        System.arraycopy(msg, 10, lengthArr, 0, 4);
        Integer length = byteArrayToInt(lengthArr);
        message.setToken(token);
        byte[] accessService = new byte[length];
        System.arraycopy(msg, 14, accessService, 0, length);
        message.setAccessService(accessService);
        byte[] args = new byte[msg.length - 14 - length];
        System.arraycopy(msg, 14 + length, args, 0, msg.length - 14 - length);
        message.setArgs(args);
        return message;
    }


    public Message generateMessage(Class clazz, String argString) {
        Message message = new Message();
        byte[] token = new byte[10];
        System.arraycopy(UUID.randomUUID().toString().getBytes(), 0,  token, 0, 10);
        byte[] lengthArr = new byte[4];
        String accessServiceIntface = clazz.getPackage().toString() +  "." + clazz.getName();
        int accessServiceIntfaceLength = accessServiceIntface.getBytes().length;
        System.arraycopy(intToByteArray(accessServiceIntfaceLength), 0, lengthArr, 0, 4);
        Integer length = accessServiceIntfaceLength;
        message.setToken(token);
        byte[] accessService = new byte[length];
        System.arraycopy(accessServiceIntface.getBytes(), 0, accessService, 0, length);
        message.setAccessService(accessService);
        int argsLength = argString.getBytes().length;
        byte[] args = new byte[argsLength];
        System.arraycopy(argString.getBytes(), 0, args, 0, argsLength);
        message.setArgs(args);
        return message;
    }
}
