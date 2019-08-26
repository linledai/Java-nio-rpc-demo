package com.dll.sockets.service.impl;

import com.dll.sockets.annotation.LogAspect;
import com.dll.sockets.annotation.RpcService;
import com.dll.sockets.service.Service;

@RpcService
public class MyService implements Service {

    @Override
    @LogAspect
    public String echo() {
        return "hello, my friends!";
    }
}
