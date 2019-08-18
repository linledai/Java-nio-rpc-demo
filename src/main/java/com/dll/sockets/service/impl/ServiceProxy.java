package com.dll.sockets.service.impl;

import com.dll.sockets.client.Client;
import com.dll.sockets.context.Context;
import com.dll.sockets.exception.ServiceInvokeException;
import com.dll.sockets.service.Service;
import com.dll.sockets.utils.ClassUtils;
import com.dll.sockets.utils.LoggerUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ServiceProxy implements Service {

    @Override
    public String echo() {
        Client client = (Client) Context.getBean("client");
        Future<Object> objectFuture = client.invoke(Service.class, ClassUtils.getCurrentMethodName());
        try {
            return (String) objectFuture.get();
        } catch (InterruptedException | ExecutionException ex) {
            LoggerUtils.error(ex);
            throw new ServiceInvokeException();
        }
    }
}
