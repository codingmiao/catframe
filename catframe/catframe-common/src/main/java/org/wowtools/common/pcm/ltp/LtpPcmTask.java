package org.wowtools.common.pcm.ltp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wowtools.common.pcm.Customer;
import org.wowtools.common.utils.AsyncTaskUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 构造一个生产者-消费者队列任务,生产者生产的数据是有限的，生产者、消费者可以是一对一、一对多、多对多、多对一的
 *
 * @author liuyu
 * @date 2016年12月6日
 */
public class LtpPcmTask<T> {
    private static final Logger log = LoggerFactory.getLogger(LtpPcmTask.class);
    private final Collection<LtpProducer<T>> producers;
    private final Collection<Customer<T>> customers;
    private final LtpBufferPool<T> bufferPool;
    private final AtomicBoolean allFinish = new AtomicBoolean(false);
    private final long waitMilliSecones;

    /**
     * 构造一个指定容量缓冲池的任务，试图向已满队列中放入元素会导致操作受阻塞
     *
     * @param producers        生产者
     * @param customers        消费者
     * @param capacity         缓冲池最大限制
     * @param waitMilliSecones 消费者等待生产者毫秒数，等待超时后重试，直至生产者宣告生产结束
     */
    public LtpPcmTask(Collection<LtpProducer<T>> producers, Collection<Customer<T>> customers, int capacity, long waitMilliSecones) {
        this.producers = producers;
        this.customers = customers;
        this.bufferPool = new LtpBufferPool<>(capacity, waitMilliSecones);
        this.waitMilliSecones = waitMilliSecones;
    }

    /**
     * 构造一个指定容量缓冲池的任务，试图向已满队列中放入元素会导致操作受阻塞
     *
     * @param producer         生产者
     * @param customers        消费者
     * @param capacity         缓冲池最大限制
     * @param waitMilliSecones 消费者等待生产者毫秒数，等待超时后重试，直至生产者宣告生产结束
     */
    public LtpPcmTask(LtpProducer<T> producer, Collection<Customer<T>> customers, int capacity, long waitMilliSecones) {
        producers = new ArrayList<>(1);
        producers.add(producer);
        this.customers = customers;
        this.bufferPool = new LtpBufferPool<>(capacity, waitMilliSecones);
        this.waitMilliSecones = waitMilliSecones;
    }

    /**
     * 构造一个指定容量缓冲池的任务，试图向已满队列中放入元素会导致操作受阻塞
     *
     * @param producers        生产者
     * @param customer         消费者
     * @param capacity         缓冲池最大限制
     * @param waitMilliSecones 消费者等待生产者毫秒数，等待超时后重试，直至生产者宣告生产结束
     */
    public LtpPcmTask(Collection<LtpProducer<T>> producers, Customer<T> customer, int capacity, long waitMilliSecones) {
        this.producers = producers;
        customers = new ArrayList<>(1);
        customers.add(customer);
        this.bufferPool = new LtpBufferPool<>(capacity, waitMilliSecones);
        this.waitMilliSecones = waitMilliSecones;
    }

    /**
     * 构造一个指定容量缓冲池的任务，试图向已满队列中放入元素会导致操作受阻塞
     *
     * @param producer         生产者
     * @param customer         消费者
     * @param capacity         缓冲池最大限制
     * @param waitMilliSecones 消费者等待生产者毫秒数，等待超时后重试，直至生产者宣告生产结束
     */
    public LtpPcmTask(LtpProducer<T> producer, Customer<T> customer, int capacity, long waitMilliSecones) {
        producers = new ArrayList<>(1);
        producers.add(producer);
        customers = new ArrayList<>(1);
        customers.add(customer);
        this.bufferPool = new LtpBufferPool<>(capacity, waitMilliSecones);
        this.waitMilliSecones = waitMilliSecones;
    }

    private static class ExceptionCell {
        Object o;
    }

    /**
     * 开始任务
     *
     * @param wait 是否等待任务结束(生产者生产完成，消费者消费完成)
     */
    public void startTask(boolean wait) {
        ExceptionCell exceptionCell = new ExceptionCell();
        //启动生产者
        for (LtpProducer<T> producer : producers) {
            AsyncTaskUtil.execute(() -> {
                try {
                    while (!producer.isFinish() && exceptionCell.o == null) {
                        T obj = producer.produce();
                        if (null == obj) {
                            break;
                        }
                        bufferPool.add(obj);
                    }
                } catch (Exception e) {
                    log.warn("生产者执行异常", e);
                    exceptionCell.o = producer;
                }
            });
        }
        //启动消费者
        final Semaphore semp = new Semaphore(0);
        for (Customer<T> customer : customers) {
            AsyncTaskUtil.execute(() -> {
                try {
                    while (true) {
                        T obj = bufferPool.take();
                        if (null != obj) {
                            customer.consume(obj);
                        } else {
                            //检查是否有异常发生
                            if (exceptionCell.o == null) {
                                //若缓冲池为空，检查是否所有生产者都已生产完成
                                if (isAllFinish()) {
                                    break;//为空则结束
                                }
                            } else {
                                //异常的话直接结束
                                bufferPool.clear();//尽量防止生产者阻塞
                                break;
                            }

                        }
                    }
                } catch (Exception e) {
                    log.warn("消费者执行异常", e);
                    exceptionCell.o = customer;
                }
                semp.release();
            });
        }
        if (wait) {
            boolean finish;
            do {
                try {
                    finish = semp.tryAcquire(customers.size(), waitMilliSecones, TimeUnit.MILLISECONDS);
                    if (!finish && exceptionCell.o != null) {
                        log.warn(exceptionCell.o.getClass() + "执行异常，退出");
                        break;
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (!finish);
        }

    }

    private boolean isAllFinish() {
        if (allFinish.get()) {
            return true;
        }
        for (LtpProducer<T> producer : producers) {
            if (!producer.isFinish()) {
                break;
            }
        }
        allFinish.set(true);
        return true;
    }
}
