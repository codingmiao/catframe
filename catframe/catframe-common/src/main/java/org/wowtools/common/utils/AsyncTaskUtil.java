package org.wowtools.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 异步任务执行工具类
 *
 * @author liuyu
 * @date 2016年7月4日
 */
public class AsyncTaskUtil {
    private static final ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * 执行一个异步任务
     *
     * @param task
     */
    public static void execute(Runnable task) {
        pool.execute(task);
    }

    /**
     * 批量执行任务，并收集任务返回的数据，待所有任务执行完后一并返回
     *
     * @param tasks
     * @return
     */
    public static <T> List<T> executeAsynTasksAndReturn(List<Callable<T>> tasks) {
        ArrayList<Future<T>> fs = new ArrayList<>();
        for (Callable<T> task : tasks) {
            Future<T> f = pool.submit(task);
            fs.add(f);
        }
        ArrayList<T> res = new ArrayList<>();
        try {
            for (Future<T> f : fs) {
                T r = f.get();
                res.add(r);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    /**
     * 批量执行任务
     *
     * @param tasks
     * @param wait  是否等待所有任务执行完毕
     */
    public static void executeAsynTasks(List<Runnable> tasks, boolean wait) {
        if (!wait) {
            for (Runnable task : tasks) {
                pool.execute(task);
            }
            return;
        }
        ArrayList<Future<?>> fs = new ArrayList<>();
        for (Runnable task : tasks) {
            Future<?> f = pool.submit(task);
            fs.add(f);
        }
        try {
            for (Future<?> f : fs) {
                f.get();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取线程池
     *
     * @return
     */
    public static ExecutorService getThreadPool() {
        return pool;
    }


    @Override
    protected void finalize() throws Throwable {
        while (true) {
            try {
                pool.shutdown();
            } catch (Exception e) {
                Thread.sleep(1000);
            }
            break;
        }
        super.finalize();
    }

    public static void shutdown() {
        pool.shutdown();
    }
}
