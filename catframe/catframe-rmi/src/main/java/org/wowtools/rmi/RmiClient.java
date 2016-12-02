package org.wowtools.rmi;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class RmiClient {

	
	/**
	 * 获取一个远程service实例
	 * @param ip
	 * @param port
	 * @param name
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getService(String ip,int port,String name){
		String url = "rmi://"+ip+":"+port+"/"+name;
		try {
			return (T) Naming.lookup(url);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			throw new RuntimeException(e);
		}
	}
}
