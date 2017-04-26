package org.wowtools.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
     * @return 结果list，list中的数据顺序与tasks对应
     */
    public static <T> List<T> executeAsyncTasksAndReturn(List<Callable<T>> tasks) {
        int n = tasks.size();
        int i = 0;
        Future<T>[] fs = new Future[n];
        for (Callable<T> task : tasks) {
            Future<T> f = pool.submit(task);
            fs[i] = f;
            i++;
        }
        ArrayList<T> res = new ArrayList<T>(n);
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
    public static void executeAsyncTasks(List<Runnable> tasks, boolean wait) {
        if (wait) {
            int n = tasks.size();
            Semaphore semaphore = new Semaphore(0);
            for (Runnable task : tasks) {
                pool.execute(() -> {
                    task.run();
                    semaphore.release();
                });
            }
            try {
                semaphore.acquire(n);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            for (Runnable task : tasks) {
                pool.execute(task);
            }
        }
    }

    /**
     * 获取线程池(CachedThreadPool)
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
