package com.dll.sockets.test;

import com.dll.sockets.context.Context;
import com.dll.sockets.server.Server;
import com.dll.sockets.service.impl.MyService;

public class ServerMain {

    public static void main(String[] args) {
        Context context = new Context();
        context.register("com.dll.sockets.service.Service", new MyService());
        new Server().start();
    }
}
