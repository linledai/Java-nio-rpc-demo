package com.dll.sockets.server;

import com.dll.sockets.context.Context;
import com.dll.sockets.message.ResponseMessage;
import com.dll.sockets.protocol.BusHandler;
import com.dll.sockets.protocol.SerializeProtocol;
import com.dll.sockets.protocol.TypeLengthContentProtocol;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;

public class ServerBusHandler extends BusHandler {

    public ServerBusHandler(SocketChannel socketChannel, byte[] msg) {
        super(socketChannel, msg);
    }

    @Override
    protected void dealMsg() {
        final byte[] token = this.getRequestMessage().getToken();
        byte[] object = null;
        byte[] accessService = this.getRequestMessage().getAccessService();
        byte[] methodName = this.getRequestMessage().getMethod();
        Object bean = Context.getBean(new String(accessService));
        Class clazz = bean.getClass();
        Method method;
        try {
            method = clazz.getMethod(new String(methodName));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }
        Object invoke = null;
        try {
            invoke = method.invoke(bean);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        ResponseMessage responseMessage = TypeLengthContentProtocol.defaultProtocol().generateReturnMessage(token, SerializeProtocol.serializeObject(invoke));
        try {
            this.getSocketChannel().write(responseMessage.toSendByteBuffer());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
