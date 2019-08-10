package com.dll.sockets.server;

import com.dll.sockets.message.Message;
import com.dll.sockets.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusHandler implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(BusHandler.class);
    private byte[] data;
    private Message message;

    public BusHandler(byte[] msg) {
        this.data = msg;
        this.message = Protocol.defaultProtocol().parse(msg);
    }

    @Override
    public void run() {
        dealMsg();
    }

    private void dealMsg() {
        logger.info(("token：" + new String(message.getToken()) + ".accessService:" + new String(message.getAccessService()) + ".args:" + new String(message.getArgs())));
    }
}
