package com.dll.sockets.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtils {

    private static Logger logger = LoggerFactory.getLogger(LoggerUtils.class);

    public static void error(Exception ex) {
        logger.error("", ex);
    }
}
