package miniDubbo.protocol;

import io.protostuff.*;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SerializationUntil
{
    private static Map<Class<?>, Schema<?>> cacheForSchemas = new ConcurrentHashMap<>();
    private static Objenesis objenesis = new ObjenesisStd(true);

    private static <T>Schema<T> getSchema(Class<T> type)
    {
        //先检查缓存里有没有;
        Schema<T> schema = (Schema<T>) cacheForSchemas.get(type);
        if(schema == null)  //未命中
        {
            schema = RuntimeSchema.createFrom(type);
            cacheForSchemas.put(type, schema);
        }
        return schema;
    }


    public static <T> byte[] Serialize(T message)
    {
        Class<T> type = (Class<T>)message.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

        try
        {
            Schema<T> schema = getSchema(type);
            return ProtobufIOUtil.toByteArray(message, schema, buffer);
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e.getMessage(), e);
        }
        finally
        {
            buffer.clear();
        }
    }

    public static <T> T Deserialize(byte[] data, Class<T> type)
    {
        try
        {
            T message = (T)objenesis.newInstance(type);
            Schema<T> schema = getSchema(type);
            ProtobufIOUtil.mergeFrom(data, message, schema);

            return message;
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
