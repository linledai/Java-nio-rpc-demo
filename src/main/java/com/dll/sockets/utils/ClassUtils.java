package com.dll.sockets.utils;

public class ClassUtils {

    public static String getCurrentMethodName() {
        int level = 1;
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[level].getMethodName();
    }
}
