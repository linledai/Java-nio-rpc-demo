package com.dll.sockets.service;

import com.dll.sockets.annotation.LogAspect;

public interface Service {

    String echo(Integer count);

    @LogAspect
    String echoTest();
}
