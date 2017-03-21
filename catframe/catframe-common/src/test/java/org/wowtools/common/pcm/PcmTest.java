package org.wowtools.common.pcm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.wowtools.common.pcm.ltp.LtpPcmTask;
import org.wowtools.common.pcm.ltp.LtpProducer;
import org.wowtools.common.utils.AsyncTaskUtil;

public class PcmTest {

    private static final Random r = new Random(233);

    private static class TestProducer implements LtpProducer<Integer> {
        String name;
        int i = 0;
        int surplus = 5;

        @Override
        public Integer produce() {
            try {
                Thread.sleep(100 + r.nextInt(100));
            } catch (InterruptedException e) {
            }
            i++;
            surplus--;
            System.out.println(name + "生产" + i);
            return i;
        }

        @Override
        public boolean isFinish() {
            return surplus == 0;
        }
    }

    private static class TestCustomer implements Customer<Integer> {
        String name;

        @Override
        public void consume(Integer obj) {
            try {
                Thread.sleep(200 + r.nextInt(100));
            } catch (InterruptedException e) {
            }
            System.out.println(name + "消费" + obj);
        }
    }

    public static void main(String[] args) {
        Collection<LtpProducer<Integer>> producers = new ArrayList<>(2);
        TestProducer p1 = new TestProducer();
        p1.i = 100;
        p1.name = "p1";
        producers.add(p1);

        TestProducer p2 = new TestProducer();
        p2.i = 200;
        p2.name = "p2";
        producers.add(p2);

        Collection<Customer<Integer>> customers = new ArrayList<>(2);
        TestCustomer c1 = new TestCustomer();
        c1.name = "c1";
        customers.add(c1);

        TestCustomer c2 = new TestCustomer();
        c2.name = "c2";
        customers.add(c2);

        LtpPcmTask<Integer> task = new LtpPcmTask<>(producers, customers, 5, 1000);
        task.startTask(true);
        System.out.println("end");
        AsyncTaskUtil.shutdown();
    }

}
