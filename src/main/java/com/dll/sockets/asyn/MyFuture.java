package com.dll.sockets.asyn;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class MyFuture extends FutureTask {

    public MyFuture(Callable callable) {
        super(callable);
    }

    public MyFuture(Runnable runnable, Object result) {
        super(runnable, result);
    }
}
