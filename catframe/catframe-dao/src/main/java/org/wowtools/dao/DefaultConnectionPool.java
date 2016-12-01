package org.wowtools.dao;

import java.sql.Connection;

/**
 * 默认的连接池,默认连接池配置文件为resources\jdbccfg-default.json
 * @author liuyu
 * @date 2016年8月8日
 */
public class DefaultConnectionPool {
	private static final ConnectionPool defaultInstance = ConnectionPool.getOrInitInstance("jdbccfg-default.json");

	public static ConnectionPool getDefaultInstance() {
		return defaultInstance;
	}
	public static Connection getConnection() {
		return defaultInstance.getConnection();
	}
}
