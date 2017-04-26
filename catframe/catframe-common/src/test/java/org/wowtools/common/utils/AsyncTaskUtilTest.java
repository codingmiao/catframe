package org.wowtools.common.utils;

import java.util.ArrayList;
import java.util.List;

public class AsyncTaskUtilTest {
    public static void testExecuteAsynTasks() {
        int n = 10;
        List<Runnable> tasks = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int id = i;
            tasks.add(() -> {
                try {
                    Thread.sleep(2000);
                    System.out.println("execute task "+id);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        AsyncTaskUtil.executeAsynTasks(tasks, true);
        System.out.println("end");
    }

    public static void main(String[] args) {
        testExecuteAsynTasks();
        System.exit(0);
    }

}