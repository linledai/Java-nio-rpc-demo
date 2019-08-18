package com.dll.sockets.service.impl;

import com.dll.sockets.annotation.LogAspect;
import com.dll.sockets.service.Service;

public class MyService implements Service {

    @Override
    @LogAspect
    public String echo() {
        return "hello, my friends!";
    }
}
