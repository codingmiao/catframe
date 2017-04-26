package org.wowtools.common.utils;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;


public class AsyncTaskUtilTest extends TestCase {
    public void testExecuteAsyncTasksAndReturn() throws Exception {
        int n = 10;
        AtomicInteger nn = new AtomicInteger(0);
        List<Runnable> tasks = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            tasks.add(() -> {
                try {
                    Thread.sleep(200);
                    nn.addAndGet(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        AsyncTaskUtil.executeAsyncTasks(tasks, true);
        assertEquals(n, nn.get());
    }

    public void testExecuteAsyncTasks() throws Exception {
        int n = 10;
        Random r = new Random(233);
        List<Callable<Integer>> tasks = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int id = i;
            int sleepTime = 100 + r.nextInt(100);
            tasks.add(() -> {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return id;
            });
        }
        List<Integer> res = AsyncTaskUtil.executeAsyncTasksAndReturn(tasks);
        Iterator<Integer> iterator = res.iterator();
        for (int i = 0; i < n; i++) {
            assertEquals(i, iterator.next().intValue());
        }
    }

}