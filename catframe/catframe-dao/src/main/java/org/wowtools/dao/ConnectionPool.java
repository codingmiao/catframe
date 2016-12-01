package org.wowtools.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.JSONObject;
import org.wowtools.common.utils.ResourcesReader;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 数据库连接池
 * @author liuyu
 * @date 2016年8月8日
 */
public class ConnectionPool {

	private static final HashMap<String, ConnectionPool> instances = new HashMap<String, ConnectionPool>(1);

	private ComboPooledDataSource dataSource;

	private String dataSourceName;

	public synchronized static ConnectionPool getOrInitInstance(String cfgPath) {
		String cfg = ResourcesReader.read(ConnectionPool.class, cfgPath);
		JSONObject jsonCfg = new JSONObject(cfg);
		return getOrInitInstance(jsonCfg);
	}

	public synchronized static ConnectionPool getOrInitInstance(JSONObject jsonCfg) {
		String dataSourceName = jsonCfg.getString("dataSourceName");
		ConnectionPool old = instances.get(dataSourceName);
		if (null != old) {
			return old;
		}
		return new ConnectionPool(jsonCfg, dataSourceName);
	}

	/**
	 * 
	 * @param jsonCfg
	 *            json格式的配置信息
	 */
	private ConnectionPool(JSONObject jsonCfg, String dataSourceName) {
		try {
			dataSource = new ComboPooledDataSource();
			dataSource.setDataSourceName(dataSourceName);
			dataSource.setUser(jsonCfg.getString("user"));
			dataSource.setPassword(jsonCfg.getString("password"));
			dataSource.setJdbcUrl(jsonCfg.getString("jdbcUrl"));
			dataSource.setDriverClass(jsonCfg.getString("driverClass"));
			dataSource.setMinPoolSize(jsonCfg.getInt("minPoolSize"));
			dataSource.setMaxPoolSize(jsonCfg.getInt("maxPoolSize"));

			try {
				dataSource.setInitialPoolSize(jsonCfg.getInt("initialPoolSize"));
			} catch (Exception e) {
			}
			try {
				dataSource.setMaxStatements(jsonCfg.getInt("maxStatements"));
			} catch (Exception e) {
			}
			try {
				dataSource.setMaxIdleTime(jsonCfg.getInt("maxIdleTime"));
			} catch (Exception e) {
			}
		} catch (Exception e) {
			throw new DaoRuntimeException("初始化JdbcUtil异常", e);
		}
	}

	public Connection getConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException e) {
			throw new DaoRuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConnectionPool) {
			ConnectionPool other = (ConnectionPool) obj;
			return dataSourceName.equals(other.dataSource);
		}
		return super.equals(obj);
	}

}
