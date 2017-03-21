package org.wowtools.common.pcm.ltp;

/**
 * 生产者，生产有限的数据
 * 
 * @author liuyu
 * @date 2016年12月6日
 * @param <T>
 */
public interface LtpProducer<T> {

	/**
	 * 生产一条数据，若不需要再生产数据，则返回null
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
