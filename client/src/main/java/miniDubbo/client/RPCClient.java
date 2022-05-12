package miniDubbo.client;

import miniDubbo.registry.IRegistry;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;

import java.util.LinkedHashMap;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;

public class RPCClient implements IRPCClient
{
    private IRegistry registry;
    private Map<String, Object> ClassToProxy = new LinkedHashMap<>();
    private ConnectManager connectManager;

    public RPCClient(IRegistry registry)
    {
        this.registry = registry;
        this.connectManager = new ConnectManager(registry);
        //this.registry.Watch();
    }

    @Override
    public <T> T creat(Class<T> type) throws Exception
    {
        //如果未包含该类型，则新建类型并加入map中
        if( !ClassToProxy.containsKey(type.getName()))
        {
            //使用ByteBuddy在运行时动态创建java类
            T proxy = new ByteBuddy()
                    //.subclass() :  表示此新类型从type继承来
                    .subclass(type)

                    //.method(XX) 筛选器，选中从XXX声明的方法
                    .method(isDeclaredBy(type))

                    //.intercept(XX) : 提供了上述method()筛选器选中的方法的实现
                    .intercept(MethodDelegation.to(new RPCInvokeInterceptor(connectManager)))

                    //.make() : 触发，此出生成一个新类型
                    .make()

                    //使用新生成的类之前，需要先将其加载到JVM中
                    .load(getClass().getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded()
                    .newInstance();

            ClassToProxy.put(type.getName(), proxy);
        }

        return (T)ClassToProxy.get(type.getName());
    }
}
