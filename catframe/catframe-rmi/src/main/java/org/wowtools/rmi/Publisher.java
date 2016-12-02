package org.wowtools.rmi;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * 发布服务端service
 * 
 * @author liuyu
 * @date 2016年12月1日
 */
public class Publisher {
	private final String url;
	public Publisher(int port) {
		this("localhost",port);
	}
	
	public Publisher(String localUrl,int port){
		try {
			LocateRegistry.createRegistry(port);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		url = "rmi://"+localUrl+":"+port+"/";
	}

	/**
	 * 发布一个service
	 * @param name 服务名
	 * @param servie 服务实现对象
	 */
	public void publish(String name, Remote servie) {
		try {
			Naming.rebind(url+name, servie);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
