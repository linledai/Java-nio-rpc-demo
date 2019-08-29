package com.dll.sockets.service;

import com.dll.sockets.client.Client;

import java.lang.reflect.Proxy;

public class ServiceBean<T> {

    private Client client;
    private Class<?> proxyInterface;

    public ServiceBean(Client client, Class<T> proxyInterface) {
        this.client = client;
        this.proxyInterface = proxyInterface;
    }

    public T getServiceBean() {
        return (T) Proxy.newProxyInstance(client.getClass().getClassLoader(), new Class[]{proxyInterface}, client.getInvocationHandler());
    }

    public Class<?> getProxyInterface() {
        return proxyInterface;
    }
}
