package miniDubbo.client;

//注：Future 是异步计算结果的容器接口，它提供了下面这些功能：
//在等待异步计算完成时检查计算结果是否完成
//在异步计算完成后获取计算结果
//在异步计算完成前取消
//Future 可以用于耗时的异步计算任务。
// 例如我们把 Runnable 接口或 Callable 接口的实现类提交到线程池时，
// 线程池会返回一个 FutureTask 对象

import miniDubbo.protocol.RPCResponse;

import java.util.concurrent.*;

public class RPCFuture implements Future<Object>
{
    //countDownLatch类使一个线程等待其他线程各自执行完毕后再执行
    //是通过一个计数器来实现的，计数器的初始值是线程的数量。每当一个线程执行完毕后，计数器的值就-1，
    // 当计数器的值为0时，表示所有线程都执行完毕，然后在闭锁上等待的线程就可以恢复工作了
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private RPCResponse response;

    public void done(RPCResponse response)
    {
        this.response = response;

        //将count的值减1
        countDownLatch.countDown();
    }

    //取消执行本次任务
    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        return false;
    }

    //如果此次任务在正常完成前被取消，则返回true
    @Override
    public boolean isCancelled()
    {
        return false;
    }

    //如果任务已完成，则返回true
    @Override
    public boolean isDone()
    {
        return false;
    }

    //等待计算完成，然后检索其结果
    @Override
    public Object get() throws InterruptedException, ExecutionException
    {
        //调用await()方法的线程会被挂起，等待count为0才会继续执行(最多等待unit个时间)
        boolean statue = countDownLatch.await(5, TimeUnit.SECONDS);
        return response.getResult();
    }

    //在给定的最多unit个时间内完成，然后检索其结果
    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
    {
        boolean statue = countDownLatch.await(timeout, unit);
        return response.getResult();
    }

}
