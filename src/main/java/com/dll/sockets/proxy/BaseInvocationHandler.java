package com.dll.sockets.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class BaseInvocationHandler implements InvocationHandler {

    private Object proxyBean;
    private InvocationHandler annotationBean;

    public BaseInvocationHandler(InvocationHandler annotationBean, Object proxyBean) {
        this.proxyBean = proxyBean;
        this.annotationBean = annotationBean;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return annotationBean.invoke(proxyBean, method, args);
    }
}
