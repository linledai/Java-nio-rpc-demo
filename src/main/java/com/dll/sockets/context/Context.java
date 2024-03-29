package com.dll.sockets.context;

import com.dll.sockets.annotation.RpcService;
import com.dll.sockets.proxy.BaseInvocationHandler;
import com.dll.sockets.service.ServiceBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Context {

    private static Map<String, Object> beanMap = new HashMap<>();
    private static Map<Class, Object> annotationMap = new HashMap<>();

    public static void register(String serviceName, Object bean) {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            Class<?> interfaceClass = bean.getClass().getInterfaces()[0];
            for (Method method : interfaceClass.getDeclaredMethods()) {
                for (Class annotationClass : Context.getAnnotationClasses()) {
                    if (method.isAnnotationPresent(annotationClass)) {
                        InvocationHandler annotationBean = (InvocationHandler) Context.getAnnotationBean(annotationClass);
                        bean = Proxy.newProxyInstance(bean.getClass().getClassLoader(), bean.getClass().getInterfaces(), new BaseInvocationHandler(annotationBean, bean, method));
                    }
                }
            }
        }
        beanMap.put(serviceName, bean);
    }

    public static void deRegister(String serviceName) {
        beanMap.remove(serviceName);
    }

    public static void registerAnnotation(Class annotation, Object bean) {
        annotationMap.put(annotation, bean);
    }

    public static Object getBean(String name) {
        Object object = beanMap.get(name);
        if (object instanceof ServiceBean) {
            return ((ServiceBean) object).getServiceBean();
        }
        return object;
    }

    public static Object getAnnotationBean(Class annotation) {
        return annotationMap.get(annotation);
    }

    public static Set<Class> getAnnotationClasses() {
        return annotationMap.keySet();
    }
}
