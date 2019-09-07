package com.dll.sockets.proxy;

import com.dll.sockets.client.DirectClient;
import com.dll.sockets.context.Context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DirectInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        DirectClient client = (DirectClient) Context.getBean("directClient");
        return client.invoke(proxy.getClass().getInterfaces()[0], method.getName(), args,
                method.getReturnType());
    }
}
