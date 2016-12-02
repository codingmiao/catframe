package org.wowtools.rmi;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

/**
 * 发布服务端service
 * 
 * @author liuyu
 * @date 2016年12月1日
 */
public class RmiPublisher {
	public static final String ZK_REGISTRY_PATH = "/catRmiService";

	private final String url;
	private ZookeeperUtil zku;

	public RmiPublisher(String localUrl, int port, String zkUrl, Integer zkSessionTimeOut) {
		this(localUrl, port);
		zku = new ZookeeperUtil(zkUrl, zkSessionTimeOut);
	}

	public RmiPublisher(int port) {
		this("localhost", port);
	}

	public RmiPublisher(String localUrl, int port) {
		try {
			LocateRegistry.createRegistry(port);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		url = localUrl + ":" + port;
	}

	/**
	 * 发布一个service
	 * 
	 * @param name
	 *            服务名
	 * @param servie
	 *            服务实现对象
	 */
	public void publish(String name, Remote servie) {
		try {
			Naming.rebind("rmi://" + url +"/"+ name, servie);
			if (null != zku) {
				try {
					zku.createNode(ZK_REGISTRY_PATH + "/" + name, url.getBytes(), CreateMode.PERSISTENT);
				} catch (Exception e) {
					if(e.getMessage().indexOf("NodeExists")<0){
						throw e;
					}
				}
				zku.createNode(ZK_REGISTRY_PATH + "/" + name + "/s", url.getBytes(), CreateMode.EPHEMERAL_SEQUENTIAL);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
