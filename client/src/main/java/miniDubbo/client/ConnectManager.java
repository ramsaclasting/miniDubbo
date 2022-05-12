package miniDubbo.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import miniDubbo.coco.ExtensionLoader;
import miniDubbo.core.EndPoint;
import miniDubbo.core.MyConfig;
import miniDubbo.core.loadbalance.ILoadBalance;
import miniDubbo.registry.ICallBackEvent;
import miniDubbo.registry.IRegistry;
import miniDubbo.registry.RegistryEvent;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectManager implements IConnectManager, ICallBackEvent
{
    private IRegistry registry;

    //nio无阻塞线程池
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    //线程安全型整数
    private AtomicInteger roundRobin = new AtomicInteger(0);

    //内部记录表
    private Map<String, List<ChannelWrapper>> channelsByService = new LinkedHashMap<>();
    //自定义通道channel包装类
    private static class ChannelWrapper
    {
        private EndPoint endPoint;
        private Channel channel;

        public ChannelWrapper(EndPoint endPoint, Channel channel)
        {
            this.endPoint = endPoint;
            this.channel = channel;
        }

        public EndPoint getEndPoint()
        {
            return endPoint;
        }

        public Channel getChannel()
        {
            return channel;
        }

        @Override
        public String toString()
        {
            return endPoint.getHost() + ":" + endPoint.getPort();
        }
    }


    //构造函数
    public ConnectManager(IRegistry registry)
    {
        this.registry = registry;
        this.registry.Watch(this);
    }


    //获取服务管道
    @Override
    public Channel getChannel(String serviceName) throws Exception
    {
        //如果发现服务
        if(! channelsByService.containsKey(serviceName))
        {
            //找服务对应的端口
            List<EndPoint> endPoints = registry.Search(serviceName);
            List<ChannelWrapper> channels = new ArrayList<>();
            for(EndPoint endPoint: endPoints)
            {
                channels.add(connect(endPoint.getHost(), endPoint.getPort()));
            }
            channelsByService.put(serviceName, channels);
        }

        //获取改服务对应的管道数量
        int size  = channelsByService.get(serviceName).size();
        ILoadBalance loadBalance = ExtensionLoader.getExtensionLoader(ILoadBalance.class).getAdaptiveInstance();

        if(size == 0)
        {
            System.out.println("No providers available for this service: " + serviceName);
        }

        //读取均衡负载的配置文件
        String config_loadbalance = MyConfig.get("minidubbo.loadbalance");
        Map<String, String > map_loadbalance = new LinkedHashMap<>();
        map_loadbalance.put("loadbalance", config_loadbalance);

        System.out.println("\n"+ config_loadbalance+"\n");
        //通过均衡负载选出一个服务的index
        int index = loadBalance.select(map_loadbalance, size);
        ChannelWrapper channelWrapper = channelsByService.get(serviceName).get(index);
        System.out.println("LoadBalance: " + loadBalance +";\n"
                + "Selected endPoint: " + channelWrapper.toString());

        //返回对应的服务管道
        return channelWrapper.getChannel();
    }

    //连接端口和地址，返回一个新建的服务管道
    private ChannelWrapper connect(String host, int port) throws Exception
    {
        //引导类，客户端的Bootstrap只需要一个channel由于与服务端通信即可
        Bootstrap bootstrap = new Bootstrap()
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new RPCClientInitializer());

        Channel channel = bootstrap.connect(host, port).sync().channel();
        ChannelWrapper channelWrapper = new ChannelWrapper(new EndPoint(host, port), channel);

        return channelWrapper;
    }

    @Override
    public void execute(RegistryEvent event)
    {
        //如果注册的服务被删除
        if(event.getEventType() == RegistryEvent.EventType.DELETE)
        {
            // 服务序列串:   /minidubbo/bytebuddy.IMyTestService/192.168.41.215:2022

            String _str = event.getTuple().getKey();
            String[] _temp = _str.split("/");
            String serviceName = _temp[2];    // bytebuddy.IMyTestService
            String endPoint_str = _temp[3];   // 192.168.41.215:2022

            String host = endPoint_str.split(":")[0];   //192.168.41.215:
            int port = Integer.valueOf(endPoint_str.split(":")[1]);    //2022

            Iterator<ChannelWrapper> _iterator = channelsByService.get(serviceName).iterator();
            while (_iterator.hasNext())
            {
                EndPoint endPoint = _iterator.next().getEndPoint();
                if(endPoint.getHost().equals(host) &&
                        (endPoint.getPort() == port))
                {
                    _iterator.remove();
                }
            }
        }


        //EvenType == PUT,
        // 添加服务
        if(event.getEventType() == RegistryEvent.EventType.PUT)
        {
            String _str = event.getTuple().getKey();
            String[] _temp = _str.split("/");
            String serviceName = _temp[2];    // bytebuddy.IMyTestService
            String endPoint_str = _temp[3];   // 192.168.41.215:2022

            String host = endPoint_str.split(":")[0];   //192.168.41.215:
            int port = Integer.valueOf(endPoint_str.split(":")[1]);    //2022

            try
            {
                channelsByService.get(serviceName).add(connect(host, port));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
