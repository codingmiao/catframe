package org.wowtools.common.pcm;

/**
 * 消费者
 * 
 * @author liuyu
 * @date 2016年12月6日
 * @param <T>
 */
public interface Customer<T> {

	/**
	 * 处理(消费)一条数据
	 * 
	 * @param obj
	 */
	public void consume(T obj);
}
