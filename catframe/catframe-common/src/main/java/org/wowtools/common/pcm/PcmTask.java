package org.wowtools.common.pcm;


/**
 * 构造一个生产者-消费者队列任务,生产者生产的数据是无限的
 * 
 * @author liuyu
 * @date 2016年12月6日
 */
public class PcmTask<T> {
	private final Producer<T> producer;
	private final Customer<T> customer;
	private final BufferPool<T> bufferPool;

	/**
	 * 构造一个指定容量缓冲池的任务，试图向已满队列中放入元素会导致操作受阻塞
	 * 
	 * @param producer
	 * @param customer
	 * @param capacity
	 */
	public PcmTask(Producer<T> producer, Customer<T> customer, int capacity) {
		this.producer = producer;
		this.customer = customer;
		this.bufferPool = new BufferPool<>(capacity);
	}

	/**
	 * 构造一个无限容量缓冲池的任务，若生产者速度比消费者快，可能会导致内存撑满
	 * 
	 * @param producer
	 * @param customer
	 */
	public PcmTask(Producer<T> producer, Customer<T> customer) {
		this.producer = producer;
		this.customer = customer;
		this.bufferPool = new BufferPool<>();
	}

	/**
	 * 开始任务
	 */
	public void startTask() {
		// 启动生产者
		new Thread(() -> {
			while (true) {
				T obj = producer.produce();
				bufferPool.add(obj);
			}
		}).start();
		// 启动消费者
		new Thread(() -> {
			while (true) {
				T obj = bufferPool.take();
				customer.consume(obj);
			}
		}).start();
	}
}
