package com.dll.sockets.client;

import com.dll.sockets.base.ShutdownNode;
import com.dll.sockets.protocol.ReturnBusHandler;
import com.dll.sockets.protocol.SerializeProtocol;

import java.nio.channels.SocketChannel;

public class ClientBusHandler extends ReturnBusHandler {

    public ClientBusHandler(ShutdownNode node, SocketChannel socketChannel, byte[] msg) {
        super(node, socketChannel, msg);
    }

    @Override
    protected void dealMsg() {
        final byte[] token = this.getMessage().getToken();
        Object object = SerializeProtocol.deSerializeObject(this.getMessage().getObject());
        ((Client) this.getNode()).fillResult(token, object);
    }
}
