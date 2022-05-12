import miniDubbo.client.RPCClient;
import miniDubbo.core.IMyTestService;
import miniDubbo.core.MyTestService1;
import miniDubbo.registry.EtcdRegistry;
import miniDubbo.registry.IRegistry;
import miniDubbo.server.RPCServer;

public class main
{
    public static void main(String[] args)
    {
        try
        {
/*            String _ip;
            String _port;
            Scanner sc = new Scanner(System.in);
            System.out.println("please enter ipAddress:");
            _ip = sc.nextLine();
            _port = sc.nextLine();

            String address = "http://" + _ip  + ":"+_port;*/
            //Syst

            System.out.println("nmsl");
            IRegistry registry = new EtcdRegistry("http://localhost:2379");//new EtcdRegistry("http://106.55.191.226:2379");
/*            try
            {
                registry = new EtcdRegistry(address);
            }
            catch (Exception e)
            {
                registry = new EtcdRegistry("http://127.0.0.1:2379");
            }*/

            RPCServer server = new RPCServer(registry)
                    .Port(2022)
                    .ExposeService(IMyTestService.class, new MyTestService1());
            server.Run();

            RPCClient client = new RPCClient(registry);
            IMyTestService _testService = client.creat(IMyTestService.class);
            String str = (String) _testService.TestFunc("Lr");
            System.out.println(str);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
