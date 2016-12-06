package org.wowtools.common.pcm.ltp;

import java.util.concurrent.TimeUnit;

import org.wowtools.common.pcm.BufferPool;

/**
 * 生产者消费者队列的缓冲池，认为生产者产生的数据是有限的
 * 
 * @author liuyu
 * @date 2016年12月6日
 * @param <T>
 */
public class BufferPoolLtp<T> extends BufferPool<T> {
	private final long timeoutMilliSecones;
	
	public BufferPoolLtp(int capacity,long timeoutMilliSecones){
		super(capacity);
		this.timeoutMilliSecones = timeoutMilliSecones;
	}
	
	public BufferPoolLtp(long timeoutMilliSecones){
		super();
		this.timeoutMilliSecones = timeoutMilliSecones;
	}
	
	/**
	 * 取一条数据，若等待超时则返回null
	 */
	@Override
	public T take() {
		try {
			return queue.poll(timeoutMilliSecones, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
}
