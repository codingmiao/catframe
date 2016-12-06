package org.wowtools.common.pcm;

/**
 * 生产者
 * 
 * @author liuyu
 * @date 2016年12月6日
 * @param <T>
 */
public interface Producer<T> {

	/**
	 * 生产一条数据
	 * 
	 * @return
	 */
	public T produce();
}
