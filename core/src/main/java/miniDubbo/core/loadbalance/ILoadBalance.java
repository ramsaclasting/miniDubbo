package miniDubbo.core.loadbalance;


import miniDubbo.coco.Adaptive;
import miniDubbo.coco.Extension;

import java.util.Map;

@Extension(defaultValue = "random")
public interface ILoadBalance
{
    int select(@Adaptive("loadbalance")
               Map<String, String>config, int amount) throws Exception;
}
