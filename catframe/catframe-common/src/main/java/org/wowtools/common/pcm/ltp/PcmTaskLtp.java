package org.wowtools.common.pcm.ltp;

import java.util.concurrent.Semaphore;

import org.wowtools.common.pcm.Customer;

/**
 * 构造一个生产者-消费者队列任务,生产者生产的数据是有限的
 * 
 * @author liuyu
 * @date 2016年12月6日
 */
public class PcmTaskLtp<T> {
	private final ProducerLtp<T> producer;
	private final Customer<T> customer;
	private final BufferPoolLtp<T> bufferPool;

	/**
	 * 构造一个指定容量缓冲池的任务，试图向已满队列中放入元素会导致操作受阻塞
	 * 
	 * @param producer
	 * @param customer
	 * @param capacity
	 * @param waitMilliSecones
	 *            消费者等待生产者毫秒数，等待超时后重试，直至生产者宣告生产结束
	 */
	public PcmTaskLtp(ProducerLtp<T> producer, Customer<T> customer, int capacity, long waitMilliSecones) {
		this.producer = producer;
		this.customer = customer;
		this.bufferPool = new BufferPoolLtp<>(capacity, waitMilliSecones);
	}

	/**
	 * 构造一个无限容量缓冲池的任务，若生产者速度比消费者快，可能会导致内存撑满
	 * 
	 * @param producer
	 * @param customer
	 * @param waitMilliSecones
	 *            消费者等待生产者毫秒数，等待超时后重试，直至生产者宣告生产结束
	 */
	public PcmTaskLtp(ProducerLtp<T> producer, Customer<T> customer, long waitMilliSecones) {
		this.producer = producer;
		this.customer = customer;
		this.bufferPool = new BufferPoolLtp<>(waitMilliSecones);
	}

	/**
	 * 开始任务
	 * 
	 * @param 是否等待任务结束(生产者生产完成，消费者消费完成)
	 */
	public void startTask(boolean wait) {
		final Semaphore semp = new Semaphore(0);
		// 启动生产者
		new Thread(() -> {
			while (!producer.isFinish()) {
				T obj = producer.produce();
				if (null == obj) {
					break;
				}
				bufferPool.add(obj);
			}
			semp.release();
		}).start();
		// 启动消费者
		new Thread(() -> {
			while (true) {
				T obj = bufferPool.take();
				if (null == obj) {
					if (producer.isFinish()) {
						break;
					} else {
						continue;
					}
				}
				customer.consume(obj);
			}
			semp.release();
		}).start();

		if (wait) {
			try {
				semp.acquire(2);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
