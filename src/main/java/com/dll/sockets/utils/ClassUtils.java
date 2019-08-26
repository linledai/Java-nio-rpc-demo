package com.dll.sockets.utils;

public class ClassUtils {

    public static String getCurrentMethodName() {
        int level = 1;
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        return stacks[level].getMethodName();
    }

    public static Class[] getArgsClassArray(Object[] args) {
        if (args == null) {
            return new Class[]{};
        }
        Class[] classes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            classes[i] = args[i].getClass();
        }
        return classes;
    }
}
