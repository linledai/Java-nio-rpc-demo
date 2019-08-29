package com.dll.sockets.protocol;

import com.dll.sockets.message.RequestMessage;
import com.dll.sockets.message.ResponseMessage;
import com.dll.sockets.message.SerializableRequestMessage;

import java.util.UUID;

public class TypeLengthContentProtocol {

    public static TypeLengthContentProtocol defaultProtocol() {
        return new TypeLengthContentProtocol();
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

    public RequestMessage parseSendMessage(byte[] msg) {
        RequestMessage requestMessage = new RequestMessage();
        byte[] token = new byte[10];
        System.arraycopy(msg, 0, token, 0, 10);
        byte[] lengthArr = new byte[4];
        System.arraycopy(msg, 10, lengthArr, 0, 4);
        Integer length = byteArrayToInt(lengthArr);
        requestMessage.setToken(token);
        byte[] accessService = new byte[length];
        System.arraycopy(msg, 14, accessService, 0, length);
        requestMessage.setAccessService(accessService);
        byte[] args = new byte[msg.length - 14 - length];
        System.arraycopy(msg, 14 + length, args, 0, msg.length - 14 - length);
        requestMessage.setMethod(args);
        return requestMessage;
    }

    public RequestMessage generateSendMessage(Class clazz, String method) {
        RequestMessage requestMessage = new RequestMessage();
        byte[] token = generateToken();
        requestMessage.setToken(token);
        byte[] accessService = getAccessService(clazz);
        requestMessage.setAccessService(accessService);
        byte[] methodBytes = getAccessMethod(method);
        requestMessage.setMethod(methodBytes);
        return requestMessage;
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

    public ResponseMessage generateReturnMessage(byte[] token, byte[] object) {
        ResponseMessage message = new ResponseMessage();
        message.setToken(token);
        message.setObject(object);
        return message;
    }

    public ResponseMessage parseReturnMessage(byte[] msg) {
        ResponseMessage message = new ResponseMessage();
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

    public SerializableRequestMessage generateRequestMessagePackage(Class clazz, String method, Object[] args) {
        SerializableRequestMessage requestMessage = new SerializableRequestMessage();
        byte[] token = generateToken();
        requestMessage.setToken(token);
        requestMessage.setAccessService(clazz.getName());
        requestMessage.setMethodName(method);
        requestMessage.setArgs(args);
        return requestMessage;
    }

    public SerializableRequestMessage parseRequestMessagePackage(byte[] msg) {
        SerializableRequestMessage serializeRequestMessage = (SerializableRequestMessage) SerializeProtocol.deSerializeObject(msg);
        return serializeRequestMessage;
    }
}
