package org.wowtools.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class RmiClient {

	public static class ZkServiceGetter<T> {
		private static final HashMap<String, ZookeeperUtil> zkuMap = new HashMap<>();
		private static final Random r = new Random();
		private final String zkPath;
		private final String name;
		private String[] serviceUrls;

		private ZkServiceGetter(String zkUrl, String name) {
			this.name = name;
			ZookeeperUtil zku;
			synchronized (zkuMap) {
				zku = zkuMap.get(zkUrl);
				if(null==zku){
					zku = new ZookeeperUtil(zkUrl, null);
					zkuMap.put(zkUrl, zku);
				}
			}
			final ZookeeperUtil zku1 = zku;
			Watcher watcher = new Watcher() {
				@Override
				public void process(WatchedEvent event) {
					if (event.getType() == Event.EventType.NodeChildrenChanged) {
						List<String> nodeList = zku1.watchChildren(zkPath, this);
						serviceUrls = toServiceUrls(zku1.getZk(), nodeList);
					}
				}

			};
			zkPath = RmiPublisher.ZK_REGISTRY_PATH + "/" + name;
			List<String> nodeList = zku.watchChildren(zkPath, watcher);
			serviceUrls = toServiceUrls(zku.getZk(), nodeList);
		}

		private String[] toServiceUrls(ZooKeeper zk, List<String> nodeList) {
			try {
				String[] urls = new String[nodeList.size()];
				int i = 0;
				for (String node : nodeList) {
					byte[] data = zk.getData(zkPath + "/" + node, false, null);
					urls[i] = new String(data);
					i++;
				}
				return urls;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public T getService() {
			if (serviceUrls == null || serviceUrls.length == 0) {
				throw new RuntimeException("暂无可用服务:" + zkPath);
			}
			String url = serviceUrls[r.nextInt(serviceUrls.length)];
			return RmiClient.getService(url, name);
		}
	}

	/**
	 * 获取一个远程service实例
	 * 
	 * @param serviceUrl
	 *            远程service ip:url
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getService(String serviceUrl, String name) {
		String url = "rmi://" + serviceUrl + "/" + name;
		try {
			return (T) Naming.lookup(url);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> ZkServiceGetter<T> getServiceGetter(String zkUrl, String name){
		ZkServiceGetter<T> getter = new ZkServiceGetter<>(zkUrl, name);
		return getter;
	}
}
