package org.wowtools.rmi;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class ZookeeperUtil {

	private final ZooKeeper zk;

	public ZookeeperUtil(String zkUrl, Integer zkSessionTimeOut) {
		if (null == zkSessionTimeOut) {
			zkSessionTimeOut = 5000;
		}
		zk = connectServer(zkUrl, zkSessionTimeOut);
	}

	/**
	 * 连接 ZooKeeper 服务器
	 * 
	 * @return
	 */
	private ZooKeeper connectServer(String zkUrl, int zkSessionTimeOut) {
		try {
			Watcher watcher = new Watcher() {
				@Override
				public void process(WatchedEvent arg0) {

				}
			};
			return new ZooKeeper(zkUrl, zkSessionTimeOut, watcher);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 创建一个 ZNode
	 * 
	 * @param path
	 * @param data
	 */
	public void createNode(String path, byte[] data, CreateMode createMode) {
		try {
			zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 监视 一个节点下Children的变化
	 * 
	 * @param path
	 * @param watcher
	 */
	public List<String> watchChildren(String path, Watcher watcher) {
		try {
			return zk.getChildren(path, watcher);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ZooKeeper getZk() {
		return zk;
	}

}
