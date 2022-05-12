package miniDubbo.client;

import io.netty.channel.Channel;
import miniDubbo.protocol.RPCRequest;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;
import java.util.UUID;

public class RPCInvokeInterceptor
{
    private IConnectManager connectManager;

    public  RPCInvokeInterceptor(IConnectManager connectManager)
    {
        this.connectManager = connectManager;
    }

    @RuntimeType
    public Object intercept(@AllArguments Object[] args,
                            @Origin Method method) throws Exception
    {
        String className = method.getDeclaringClass().getName();
        System.out.println(className);

        //新建一个RPC服务调用请求
        RPCRequest request = new RPCRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(className);
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);

        //从connectManager获取一个新连接管道
        Channel channel = connectManager.getChannel(className);

        //通过此连接发送一个RPC请求
        ////////////
        RPCFuture future = new RPCFuture();
        //加入map池
        RPCMapRequestToFuture.put(request.getRequestId(), future);
        channel.writeAndFlush(request);

        Object result = null;
        //获取结果
        try
        {
            result = future.get();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
