package miniDubbo.registry;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.Lease;
import com.coreos.jetcd.Watch;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.PutOption;
import com.coreos.jetcd.options.WatchOption;
import com.coreos.jetcd.watch.WatchEvent;
import core.EndPoint;
import core.IPHelper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;


public class EtcdRegistry implements IRegistry
{
    //默认地址
    private String registryAddress =  "http://127.0.0.1:2379";
    private final String rootPath = "minidubbo";
    private Lease lease;
    private KV kv;
    private Watch watch;
    private long leaseID;

    private Map<String , List<EndPoint>> endointsByService = new LinkedHashMap<>();
    private ICallBackEvent callBackEvent;

    public EtcdRegistry(String registryAddress) throws Exception
    {
        this.registryAddress = registryAddress;
        Client client = Client.builder().endpoints(registryAddress).build();

        this.lease = client.getLeaseClient();
        this.kv = client.getKVClient();
        this.watch = client.getWatchClient();
        this.leaseID = lease.grant(30).get().getID();
    }

    // 向ETCD中注册服务
    @Override
    public void Register(String serviceName, int port) throws Exception
    {
        //服务注册的key:  /minidubbo/包路径/ip地址:端口号
        String serviceKey = MessageFormat.format(
                "/{0}/{1}/{2}:{3}",
                rootPath,
                serviceName,
                IPHelper.GetHostIP(),
                String.valueOf(port)
        );

        //将字符串转为客户端所需的ByteSequence实例
        ByteSequence key = ByteSequence.fromString(serviceKey);
        ByteSequence value = ByteSequence.fromString("");

        kv.put(key, value, PutOption.newBuilder().withLeaseId(leaseID).build()).get();
        System.out.println("Register a new service at: " + serviceKey);
    }


    //取消注册
    @Override
    public void UnRegister(String serviceName)
    {
        return;
    }

    @Override
    public List<EndPoint> Search(String serviceName) throws Exception
    {
        if(endointsByService.containsKey(serviceName))
        {
            return endointsByService.get(serviceName);
        }

        //没找到则添加
        //将字符串转为客户端所需的ByteSequence实例
        String strKey = MessageFormat.format("/{0}/{1}", rootPath, serviceName);
        ByteSequence key = ByteSequence.fromString(strKey);
        GetResponse response = kv.get(key, GetOption.newBuilder().withPrefix(key).build()).get();

        List<EndPoint> endPoints = new ArrayList<>();

        for (KeyValue kv: response.getKvs())
        {
            String _str = kv.getKey().toStringUtf8();
            int index = _str.lastIndexOf("/");

            String str_endPoint = _str.substring(index + 1, _str.length());

            String host = str_endPoint.split(":")[0];
            int port = Integer.valueOf(str_endPoint.split(":")[1].replace(",",""));

            //System.out.println(host);
            //System.out.println(port);

            endPoints.add(new EndPoint(host, port));
        }
        endointsByService.put(serviceName, endPoints);
        return endPoints;
    }

    //内部wacth函数
    private void _watch()
    {
        ByteSequence _path = ByteSequence.fromString("/" + rootPath);
        Watch.Watcher watcher = watch.watch(_path,
                WatchOption.newBuilder().withPrefix(_path).build());

        Executors.newSingleThreadExecutor().submit((Runnable) () ->
        {
            while (true)
            {
                try
                {
                    for(WatchEvent event: watcher.listen().getEvents())
                    {
                        System.out.println(event.getEventType());
                        System.out.println(event.getKeyValue().getKey().toStringUtf8());
                        System.out.println(event.getKeyValue().getValue().toStringUtf8());

                        //逆向解析serviceKey中的端口名和地址等信息
                        String serviceKey = event.getKeyValue().getKey().toStringUtf8();
                        String[] _temp = serviceKey.split("/");

                        String serviceName = _temp[2];
                        String endPoint = _temp[3];

                        String host = endPoint.split(":")[0];
                        int port = Integer.valueOf(endPoint.split(":")[1]);

                        endointsByService.get(serviceName).remove(new EndPoint(host, port));

                        if(callBackEvent != null)
                        {
                            RegistryEvent registryEvent = RegistryEvent
                                    .Builder()
                                    .eventType(RegistryEvent.EventType.valueOf(event.getEventType().toString()))
                                    .key(event.getKeyValue().getKey().toStringUtf8())
                                    .value(event.getKeyValue().getValue().toStringUtf8())
                                    .build();

                            callBackEvent.execute(registryEvent);
                        }
                    }
                }
                catch (Exception e)
                {

                }
            }
        });
    }

    @Override
    public void Watch(ICallBackEvent callbcak)
    {
        this.callBackEvent = callbcak;
        _watch();
    }

    //发送信息给Etcd，表明该host的服务可用
    @Override
    public void KeepAlive()
    {
        //吐槽，有点像大二javva那个scenebuildere写事件的代
        // 码，反正被C#秒了
        Executors.newSingleThreadExecutor().submit(
                ()->
                {
                    try
                    {
                        Lease.KeepAliveListener listener = lease.keepAlive(leaseID);
                        listener.listen();
                        System.out.println("Keep Alive lease: " + leaseID + ";" +
                                "HexFormat: "+ Long.toHexString(leaseID));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
        );
    }
}
