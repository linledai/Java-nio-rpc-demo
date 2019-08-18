package com.dll.sockets.proxy;

import com.dll.sockets.client.Client;
import com.dll.sockets.context.Context;
import com.dll.sockets.utils.LoggerUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MyInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Client client = (Client) Context.getBean("client");
        try {
            return client.invokeDirect(proxy.getClass().getInterfaces()[0], method.getName());
        } catch (InterruptedException ex) {
            LoggerUtils.error(ex);
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
