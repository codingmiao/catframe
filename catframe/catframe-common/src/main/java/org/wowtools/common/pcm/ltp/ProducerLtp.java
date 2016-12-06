package org.wowtools.common.pcm.ltp;

/**
 * 生产者，生产有限的数据
 * 
 * @author liuyu
 * @date 2016年12月6日
 * @param <T>
 */
public interface ProducerLtp<T> {

	/**
	 * 生产一条数据，若返回null，则表示生产已完成
	 * 
	 * @return
	 */
	public T produce();
	
	/**
	 * 是否已完成生产
	 * @return
	 */
	public boolean isFinish();
}
