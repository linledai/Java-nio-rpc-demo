package com.dll.sockets.proxy;

import com.dll.sockets.client.FutureClient;
import com.dll.sockets.context.Context;
import com.dll.sockets.utils.LoggerUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TimeOutInvocationHandler implements InvocationHandler {

    private Integer timeoutSeconds = 300000;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        FutureClient client = (FutureClient) Context.getBean("futureClient");
        Future<Object> invoke = client.invoke(proxy.getClass().getInterfaces()[0], method.getName(), args, method.getReturnType());
        try {
            return invoke.get(timeoutSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            LoggerUtils.error(ex);
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
