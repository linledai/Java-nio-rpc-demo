package com.dll.sockets.context;

import com.dll.sockets.annotation.RpcService;
import com.dll.sockets.proxy.BaseInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Context {

    public static Map<String, Object> beanMap = new HashMap<>();
    public static Map<Class, Object> annotationMap = new HashMap<>();

    public static void register(String serviceName, Object bean) {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            for (Method method : bean.getClass().getDeclaredMethods()) {
                for (Class annotationClass : Context.getAnnotationClasses()) {
                    if (method.isAnnotationPresent(annotationClass)) {
                        InvocationHandler annotationBean = (InvocationHandler) Context.getAnnotationBean(annotationClass);
                        bean = Proxy.newProxyInstance(bean.getClass().getClassLoader(), bean.getClass().getInterfaces(), new BaseInvocationHandler(annotationBean, bean));
                    }
                }
            }
        }
        beanMap.put(serviceName, bean);
    }

    public static void registerAnnotation(Class annotation, Object bean) {
        annotationMap.put(annotation, bean);
    }

    public static Object getBean(String name) {
        return beanMap.get(name);
    }

    public static Object getAnnotationBean(Class annotation) {
        return annotationMap.get(annotation);
    }

    public static Set<Class> getAnnotationClasses() {
        return annotationMap.keySet();
    }
}
