package com.dll.sockets.proxy;

import com.dll.sockets.client.Client;
import com.dll.sockets.context.Context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DirectInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Client client = (Client) Context.getBean("client");
        return client.invokeDirect(proxy.getClass().getInterfaces()[0], method.getName());
    }
}
