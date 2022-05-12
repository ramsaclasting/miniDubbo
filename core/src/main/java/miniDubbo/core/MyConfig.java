package miniDubbo.core;


import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

//配置类
public class MyConfig
{
    private static Map<String, String> configs = new LinkedHashMap<>();
    private static boolean isInitialized = false;

    //从本地加载配置
    private static void load() throws Exception
    {
        //从此路径加载 /minidubbo.properties
        //Properties类，用于读取用户配置文件
        Properties properties = new Properties();
        InputStream inputStream = (MyConfig.class).getClassLoader().getResourceAsStream("minidubbo.properties");
        properties.load(inputStream);

        for (Map.Entry<Object, Object> entry: properties.entrySet())
        {
            //将读取的配置装入自定义的map=>configs中
            configs.put((String) entry.getKey(), (String) entry.getValue());
        }
    }


    //获取配置
    public static String get(String key)
    {
        //如果未初始化
        if( !isInitialized)
        {
            //则常视初始化
            try
            {
                load();
                isInitialized = true;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return configs.get(key);
    }
}
