package com.dll.sockets.base;

public interface NodeContext {

    Class getClassByResponseToken(String token);

    void addClassForResponseToken(String token, Class clazz);

    void removeResponseToken(String token);
}
