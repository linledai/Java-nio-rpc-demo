package com.dll.sockets.client;

import com.dll.sockets.base.ShutdownNode;
import com.dll.sockets.protocol.ReturnBusHandler;
import com.dll.sockets.protocol.serialize.SerializeFactory;

import java.nio.channels.SocketChannel;

public class ClientBusHandler extends ReturnBusHandler {

    public ClientBusHandler(ShutdownNode node, SocketChannel socketChannel, byte[] msg) {
        super(node, socketChannel, msg);
    }

    @Override
    protected void dealMsg() {
        final byte[] token = this.getMessage().getToken();
        Object object = SerializeFactory.buildSerializeHandler().deSerializeObject(this.getMessage().getObject(), getNode().getClassByResponseToken(new String(token)));
        ((Client) this.getNode()).fillResult(token, object);
    }
}
