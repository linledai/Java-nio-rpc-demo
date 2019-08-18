package com.dll.sockets.proxy;

import com.dll.sockets.client.Client;
import com.dll.sockets.context.Context;
import com.dll.sockets.utils.LoggerUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MyInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Client client = (Client) Context.getBean("client");
        Future<Object> objectFuture = client.invoke(proxy.getClass().getInterfaces()[0], method.getName());
        try {
            return objectFuture.get();
        } catch (InterruptedException | ExecutionException ex) {
            LoggerUtils.error(ex);
            return null;
        }
    }
}
