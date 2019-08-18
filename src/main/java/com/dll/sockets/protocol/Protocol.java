package com.dll.sockets.protocol;

import com.dll.sockets.message.Message;
import com.dll.sockets.message.ReturnMessage;

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

    public Message parseSendMessage(byte[] msg) {
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
        message.setMethod(args);
        return message;
    }

    public Message generateSendMessage(Class clazz, String method) {
        Message message = new Message();
        byte[] token = generateToken();
        message.setToken(token);
        byte[] accessService = getAccessService(clazz);
        message.setAccessService(accessService);
        byte[] methodBytes = getAccessMethod(method);
        message.setMethod(methodBytes);
        return message;
    }

    private byte[] getAccessMethod(String method) {
        int argsLength = method.getBytes().length;
        byte[] args = new byte[argsLength];
        System.arraycopy(method.getBytes(), 0, args, 0, argsLength);
        return args;
    }

    private byte[] getAccessService(Class clazz) {
        String accessServiceIntface = clazz.getName();
        int accessServiceIntfaceLength = accessServiceIntface.getBytes().length;
        byte[] accessService = new byte[accessServiceIntfaceLength];
        System.arraycopy(accessServiceIntface.getBytes(), 0, accessService, 0, accessServiceIntfaceLength);
        return accessService;
    }

    private byte[] generateToken() {
        byte[] token = new byte[10];
        System.arraycopy(UUID.randomUUID().toString().getBytes(), 0,  token, 0, 10);
        return token;
    }


    public ReturnMessage generateReturnMessage(byte[] token, byte[] object) {
        ReturnMessage message = new ReturnMessage();
        message.setToken(token);
        message.setObject(object);
        return message;
    }

    public ReturnMessage parseReturnMessage(byte[] msg) {
        ReturnMessage message = new ReturnMessage();
        byte[] token = new byte[10];
        System.arraycopy(msg, 0, token, 0, 10);
        byte[] lengthArr = new byte[4];
        System.arraycopy(msg, 10, lengthArr, 0, 4);
        Integer length = byteArrayToInt(lengthArr);
        message.setToken(token);
        byte[] object = new byte[length];
        System.arraycopy(msg, 14, object, 0, length);
        message.setObject(object);
        return message;
    }
}
