package com.dll.sockets.server;

import com.dll.sockets.base.ShutdownNode;
import com.dll.sockets.context.Context;
import com.dll.sockets.message.ByteBufferMessage;
import com.dll.sockets.message.RequestMessage;
import com.dll.sockets.message.ResponseMessage;
import com.dll.sockets.message.SerializableRequestMessage;
import com.dll.sockets.protocol.BusHandler;
import com.dll.sockets.protocol.SerializeProtocol;
import com.dll.sockets.protocol.TypeLengthContentProtocol;
import com.dll.sockets.utils.ClassUtils;
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
        ByteBufferMessage byteBufferMessage = this.getRequestMessage();
        final byte[] token = byteBufferMessage.getToken();
        String serviceName;
        String methodName;
        Object[] args = null;
        if (byteBufferMessage instanceof RequestMessage) {
            RequestMessage requestMessage = (RequestMessage) byteBufferMessage;
            byte[] accessService = requestMessage.getAccessService();
            serviceName = new String(accessService);
            byte[] method = requestMessage.getMethod();
            methodName = new String(method);
        } else {
            SerializableRequestMessage requestMessage = (SerializableRequestMessage) byteBufferMessage;
            serviceName = requestMessage.getAccessService();
            methodName = requestMessage.getMethodName();
            args = requestMessage.getArgs();
        }
        Object bean = Context.getBean(serviceName);
        Class clazz;
        try {
            clazz = bean.getClass().getClassLoader().loadClass(serviceName);
        } catch (ClassNotFoundException e) {
            logger.error("", e);
            clazz = bean.getClass();
        }
        Method method = clazz.getMethod(methodName, ClassUtils.getArgsClassArray(args));
        Object invoke;
        if (args != null) {
            invoke = method.invoke(bean, args);
        } else {
            invoke = method.invoke(bean);
        }
        ResponseMessage responseMessage = TypeLengthContentProtocol.defaultProtocol().generateReturnMessage(token, SerializeProtocol.serializeObject(invoke));
        synchronized (this.getSocketChannel()) {
            this.getSocketChannel().write(responseMessage.toSendByteBuffer());
        }
    }
}
