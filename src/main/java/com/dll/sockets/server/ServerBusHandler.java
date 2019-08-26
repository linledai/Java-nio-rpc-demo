package com.dll.sockets.server;

import com.dll.sockets.base.ShutdownNode;
import com.dll.sockets.context.Context;
import com.dll.sockets.message.ResponseMessage;
import com.dll.sockets.protocol.BusHandler;
import com.dll.sockets.protocol.SerializeProtocol;
import com.dll.sockets.protocol.TypeLengthContentProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;

public class ServerBusHandler extends BusHandler {

    private static Logger logger = LoggerFactory.getLogger(ServerBusHandler.class);

    public ServerBusHandler(ShutdownNode node, SocketChannel socketChannel, byte[] msg) {
        super(node, socketChannel, msg);
    }

    @Override
    protected void dealMsg() throws Throwable {
        final byte[] token = this.getRequestMessage().getToken();
        byte[] accessService = this.getRequestMessage().getAccessService();
        byte[] methodName = this.getRequestMessage().getMethod();
        String serviceName = new String(accessService);
        Object bean = Context.getBean(serviceName);
        Class clazz;
        try {
            clazz = bean.getClass().getClassLoader().loadClass(serviceName);
        } catch (ClassNotFoundException e) {
            logger.error("", e);
            clazz = bean.getClass();
        }
        Method method = clazz.getMethod(new String(methodName));
        Object invoke = method.invoke(bean);
        ResponseMessage responseMessage = TypeLengthContentProtocol.defaultProtocol().generateReturnMessage(token, SerializeProtocol.serializeObject(invoke));
        synchronized (this.getSocketChannel()) {
            this.getSocketChannel().write(responseMessage.toSendByteBuffer());
        }
    }
}
