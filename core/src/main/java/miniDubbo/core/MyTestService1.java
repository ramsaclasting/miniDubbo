package miniDubbo.core;

public class MyTestService1 implements IMyTestService

{
    @Override
    public Object TestFunc(Object paras) throws Exception
    {
        String str  = "Hello World! " + (String)paras;
        System.out.println(str);
        return str;
    }
}
