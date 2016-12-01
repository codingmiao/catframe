package org.wowtools.common.utils;

import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 弱引用缓存类，
 * 当某种数据需要被重复使用，此数据加载需要一定时间，但全部加载到内存中又太占资源时，可以考虑用此工具类。
 * 此类缓存了数据键值对在内存中，当缓存中没有对应的key、或key对应的值为null时，通过loadByKey从外部加载数据；
 * 缓存在GC时由JDK自行判断是否回收，也可调用clear方法手动清理
 * @author liuyu
 * @date 2016年7月27日
 * @param <K>
 * @param <V>
 */
public abstract class WeakCache<K,V> {
	
	private final WeakHashMap<K,V> cacheMap;
	
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	
	public WeakCache(){
		cacheMap = new WeakHashMap<>();
	}
	
	public WeakCache(int initCacheMapSize){
		cacheMap = new WeakHashMap<>(initCacheMapSize);
	}
	
	/**
	 * 当cache中没有key对应的值时，通过此方法从外部加载
	 * @param key
	 * @return
	 */
	protected abstract V loadByKey(K key);
	
	public V get(K key){
		lock.readLock().lock();
		V res = cacheMap.get(key);
		lock.readLock().unlock();
		if(null==res){
			res = loadByKey(key);
			final V v = res;
			lock.writeLock().lock();
			cacheMap.put(key, v);
			lock.writeLock().unlock();

		}
		return res;
	}
	
	public void clear(){
		lock.writeLock().lock();
		cacheMap.clear();
		lock.writeLock().unlock();
	}
	
}
