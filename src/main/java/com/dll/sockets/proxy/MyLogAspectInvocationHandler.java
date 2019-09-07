package com.dll.sockets.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MyLogAspectInvocationHandler implements InvocationHandler {

    private static Logger logger = LoggerFactory.getLogger(MyLogAspectInvocationHandler.class);

    public Object around(Object proxy, Method method, Object[] args) throws Throwable {
        logger.debug("around before");
        Object object = method.invoke(proxy, args);
        logger.debug("around end");
        return object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return around(proxy, method, args);
    }

}
