package miniDubbo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import miniDubbo.registry.IRegistry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class RPCServer
{
    private String host = "127.0.0.1";
    private int port = 2022;
    private IRegistry registry;

    private Map<String, Object> handlerMap = new LinkedHashMap<>();

    public RPCServer(IRegistry registry)
    {
        this.registry = registry;
    }

    public RPCServer ExposeService(Class<?> type, Object handler) throws Exception
    {
        //记录已暴露的服务
        handlerMap.put(type.getName(), handler);
        //唤醒注册
        registry.KeepAlive();

        return this;
    }

    public RPCServer Port(int port)
    {
        this.port = port;
        return this;
    }

    public void Run() throws Exception
    {
        Executors.newSingleThreadExecutor().submit(
                () ->
                {
                    //接受请求
                    EventLoopGroup bossGroup = new NioEventLoopGroup();

                    //处理client的IO
                    EventLoopGroup workerGroup = new NioEventLoopGroup();

                    ServerBootstrap bootstrap = new ServerBootstrap()
                            .group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)

                            //childhandler设置每一个连接到服务端的socket(socketchannel)的handler(childhandler)，
                            // 通过创建一个EchoServerHandler类去处理。
                            // （利用channelhandler可以完成功能定制）
                            .childHandler(new RPCServerInitializer(handlerMap))
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    ChannelFuture future = null;
                    try
                    {
                        //绑定端口，netty中所有IO操作都是异步的，
                        // 它会立即返回，但不能保证完成操作
                        future = bootstrap.bind(port).sync();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }

                    for(String className: handlerMap.keySet())
                    {
                        try
                        {
                            //为map中的类进行注册服务
                            registry.Register(className, port);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }

                    try
                    {
                        //关闭端口
                        future.channel().closeFuture().sync();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
        );
    }
}
