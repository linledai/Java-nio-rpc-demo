package com.dll.sockets.proxy;

import com.dll.sockets.client.Client;
import com.dll.sockets.context.Context;
import com.dll.sockets.utils.LoggerUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TimeOutInvocationHandler implements InvocationHandler {

    private Integer timeoutSeconds = 10000;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Client client = (Client) Context.getBean("client");
        Future<Object> invoke = client.invoke(proxy.getClass().getInterfaces()[0], method.getName());
        try {
            return invoke.get(timeoutSeconds, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            LoggerUtils.error(ex);
            Thread.currentThread().interrupt();
        }
        return null;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
