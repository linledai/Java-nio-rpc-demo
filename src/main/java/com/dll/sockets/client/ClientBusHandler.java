package com.dll.sockets.client;

import com.dll.sockets.protocol.ReturnBusHandler;
import com.dll.sockets.protocol.SerializeProtocol;

import java.nio.channels.SocketChannel;

public class ClientBusHandler extends ReturnBusHandler {

    public ClientBusHandler(SocketChannel socketChannel, byte[] msg) {
        super(socketChannel, msg);
    }

    @Override
    protected void dealMsg() {
        final byte[] token = this.getMessage().getToken();
        Object object = SerializeProtocol.deSerializeObject(this.getMessage().getObject());
        Client.fillResult(token, object);
    }
}
