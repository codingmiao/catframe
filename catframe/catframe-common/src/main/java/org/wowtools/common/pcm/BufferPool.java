package org.wowtools.common.pcm;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;


/**
 * 生产者消费者队列的缓冲池，认为生产者产生的数据是无限的
 *
 * @param <T>
 * @author liuyu
 */
public class BufferPool<T> {
    protected final BlockingQueue<T> queue;

    /**
     * 构造一个指定容量的缓冲池，试图向已满队列中放入元素会导致操作受阻塞
     *
     * @param capacity 缓冲池容量
     */
    public BufferPool(int capacity) {
        queue = new ArrayBlockingQueue<>(capacity);
    }

    /**
     * 构造一个无限容量的缓冲池
     */
    public BufferPool() {
        queue = new LinkedTransferQueue<>();
    }

    /**
     * 放入数据
     *
     * @param obj
     */
    public void add(T obj) {
        try {
            queue.put(obj);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 取出数据
     *
     * @return
     */
    public T take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void clear(){
        queue.clear();
    }
}
