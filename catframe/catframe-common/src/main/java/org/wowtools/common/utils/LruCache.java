package org.wowtools.common.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;

/**
 * 最近最少使用原则LRU(Least recently used)失效的缓存
 * 
 * @author liuyu
 *
 */
public class LruCache {

	/**
	 * 构建一个Map对象作为缓存容器，当map中对象的容量超过其容量时，将利用LRU(Least recently used)算法淘汰掉不常使用的对象
	 * 
	 * @param capacity
	 *            最大容量
	 * @param concurrency
	 *            预计在运行中会有多少个线程同时对缓存进行更新，若此值大于1，则返回一个ConcurrentLinkedHashMap
	 * @return LRU map cache
	 */
	public static <K, V> Map<K, V> buildCache(final int capacity, int concurrency) {
		if (concurrency < 2) {
			return new LinkedHashMap<K, V>(capacity, 0.75f, true) {
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
					return size() > capacity;
				}
			};
		} else {
			return new ConcurrentLinkedHashMap.Builder<K, V>().weigher(Weighers.<V> singleton())
					.initialCapacity(capacity).maximumWeightedCapacity(capacity).concurrencyLevel(concurrency).build();
		}

	}

}
