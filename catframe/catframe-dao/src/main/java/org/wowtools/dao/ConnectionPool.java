package org.wowtools.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wowtools.common.utils.ResourcesReader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

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
 * 	    "connectionTimeout":200000,//可选，连接超时
 * 	    "prepStmtCacheSize":256,//可选，prepStmtCacheSize
 * 	    "prepStmtCacheSqlLimit":1024,//可选，prepStmtCacheSqlLimit
 * 	    "cachePrepStmts":"true",//可选，开启cachePrepStmts
 *    }
 * </pre>
 *
 * @author liuyu
 * @date 2016年8月8日
 */
public class ConnectionPool {
    private static final Logger log = LoggerFactory.getLogger(ConnectionPool.class);
    private static final HashMap<String, ConnectionPool> instances = new HashMap<String, ConnectionPool>(1);

    private final DataSource dataSource;

    private final String dataSourceName;

    /**
     * 根据配置文件中的信息，构造一个连接池
     *
     * @param cfgPath 配置文件绝对路径
     * @return ConnectionPool
     */
    public static ConnectionPool getOrInitInstance(String cfgPath) {
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
    public static ConnectionPool getOrInitInstance(Class<?> clazz, String cfgPath) {
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
        ConnectionPool pool = instances.get(dataSourceName);
        if (null == pool) {
            pool = new ConnectionPool(jsonCfg, dataSourceName);
            instances.put(dataSourceName, pool);
        }
        return pool;
    }

    /**
     * @param jsonCfg json格式的配置信息
     */
    private ConnectionPool(JSONObject jsonCfg, String dataSourceName) {
        try {
            HikariConfig config = new HikariConfig();
            try {
                config.setJdbcUrl(jsonCfg.getString("jdbcUrl"));
            } catch (Exception e) {
                throw new RuntimeException("未设置jdbcUrl参数");
            }
            try {
                config.setDriverClassName(jsonCfg.getString("driverClass"));
            } catch (Exception e) {
                throw new RuntimeException("未设置driverClass参数");
            }
            try {
                config.setUsername(jsonCfg.getString("user"));
            } catch (Exception e) {
                log.warn("未设置user参数");
            }
            try {
                config.setPassword(jsonCfg.getString("password"));
            } catch (Exception e) {
                log.warn("未设置password参数");
            }
            try {
                config.setMinimumIdle(jsonCfg.getInt("minPoolSize"));
            } catch (Exception e) {
            }
            try {
                config.setMaximumPoolSize(jsonCfg.getInt("maxPoolSize"));
            } catch (Exception e) {
            }

            try {
                config.addDataSourceProperty("cachePrepStmts", jsonCfg.get("cachePrepStmts"));
            } catch (Exception e) {
            }
            try {
                config.addDataSourceProperty("prepStmtCacheSize", jsonCfg.get("prepStmtCacheSize"));
            } catch (Exception e) {
            }
            try {
                config.addDataSourceProperty("prepStmtCacheSqlLimit", jsonCfg.get("prepStmtCacheSqlLimit"));
            } catch (Exception e) {
            }
            try {
                config.setConnectionTimeout(jsonCfg.getLong("connectionTimeout"));
            } catch (Exception e) {
            }
            this.dataSourceName = dataSourceName;
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            throw new DaoRuntimeException("初始化ConnectionPool异常", e);
        }
    }

    /**
     * 获得一个数据库连接
     *
     * @return
     */
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
     * 获得连接池名称
     *
     * @return
     */
    public String getName() {
        return dataSourceName;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
