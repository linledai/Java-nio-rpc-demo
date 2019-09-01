package com.dll.sockets.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class BaseInvocationHandler implements InvocationHandler {

    private Object proxyBean;
    private InvocationHandler annotationBean;
    private Method proxyMethod;

    public BaseInvocationHandler(InvocationHandler annotationBean, Object proxyBean, Method proxyMethod) {
        this.proxyBean = proxyBean;
        this.annotationBean = annotationBean;
        this.proxyMethod = proxyMethod;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (proxyMethod.equals(method)) {
            return annotationBean.invoke(proxyBean, method, args);
        } else {
            return method.invoke(proxyBean, args);
        }
    }
}
