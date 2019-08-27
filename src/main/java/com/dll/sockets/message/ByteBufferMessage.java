package com.dll.sockets.message;

import java.io.Serializable;
import java.nio.ByteBuffer;

public interface ByteBufferMessage extends Serializable {
    ByteBuffer toSendByteBuffer();

    byte[] getToken();
}
