package com.dll.sockets.protocol.serialize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.dll.sockets.protocol.SerializeHandler;

public class FastJsonSerializeProtocol implements SerializeHandler {

    public byte[] serializeObject(Object object) {
        SerializeWriter serializeWriter = new SerializeWriter();
        JSONSerializer.write(serializeWriter, object);
        return serializeWriter.toBytes("utf-8");
    }

    public Object deSerializeObject(byte[] object, Class clazz) {
        return JSON.parseObject(new String(object), clazz);
    }
}
