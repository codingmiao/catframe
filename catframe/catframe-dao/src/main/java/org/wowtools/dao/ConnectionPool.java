package org.wowtools.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.JSONObject;
import org.wowtools.common.utils.ResourcesReader;

import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * 数据库连接池，传入一个类似如下的json，构建一个连接池
 * <pre>
 *  {
 * 		"dataSourceName":"mysql",//名称，需保证唯一
 * 		"user":"root",//db用户名
 * 		"password":"",//db密码
 * 		"jdbcUrl":"jdbc:mysql://host:port/dbName?useUnicode=true&characterEncoding=UTF-8",//连接url
 * 		"driverClass":"com.mysql.jdbc.Driver",//驱动
 * 		"minPoolSize":0,//最小连接数
 * 		"maxPoolSize":1,//最大连接数
 * 		"initialPoolSize":1,//可选，初始连接数
 * 		"maxStatements":1,//可选，最大maxIdleTime
 * 		"maxIdleTime":1,//可选，maxIdleTime
 *    }
 * </pre>
 *
 * @author liuyu
 * @date 2016年8月8日
 */
public class ConnectionPool {

    private static final HashMap<String, ConnectionPool> instances = new HashMap<String, ConnectionPool>(1);

    private final ComboPooledDataSource dataSource;

    private final String dataSourceName;

    /**
     * 根据配置文件中的信息，构造一个连接池
     *
     * @param cfgPath 配置文件绝对路径
     * @return ConnectionPool
     */
    public synchronized static ConnectionPool getOrInitInstance(String cfgPath) {
        String cfg = ResourcesReader.readStr(cfgPath);
        JSONObject jsonCfg = new JSONObject(cfg);
        return getOrInitInstance(jsonCfg);
    }

    /**
     * 根据配置文件中的信息，构造一个连接池
     *
     * @param clazz   定位相对路径用的类
     * @param cfgPath 配置文件相对路径
     * @return
     */
    public synchronized static ConnectionPool getOrInitInstance(Class<?> clazz, String cfgPath) {
        String cfg = ResourcesReader.readStr(clazz, cfgPath);
        JSONObject jsonCfg = new JSONObject(cfg);
        return getOrInitInstance(jsonCfg);
    }

    /**
     * 根据传入的json信息，构造一个连接池
     *
     * @param jsonCfg json
     * @return ConnectionPool
     */
    public synchronized static ConnectionPool getOrInitInstance(JSONObject jsonCfg) {
        String dataSourceName = jsonCfg.getString("dataSourceName");
        ConnectionPool old = instances.get(dataSourceName);
        if (null != old) {
            return old;
        }
        return new ConnectionPool(jsonCfg, dataSourceName);
    }

    /**
     * @param jsonCfg json格式的配置信息
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
            this.dataSourceName = dataSourceName;
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

    /**
     * 获取DataSource
     *
     * @return
     */
    public ComboPooledDataSource getDataSource() {
        return dataSource;
    }

    /**
     * 获得连接池名称
     *
     * @return
     */
    public String getName() {
        return dataSourceName;
    }
}
