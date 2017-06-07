package org.wowtools.dao;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

/**
 * @author liuyu
 * @date 2017/6/7
 */
public class ConnectionPoolTest {
    public static void main(String[] args)throws Exception {
        JdbcConnectionPool connectionPool = JdbcConnectionPool.create("jdbc:h2:mem:test;MVCC=TRUE",
                "sa", "sa");
        Server tcpServer = Server
                .createTcpServer(new String[]{"-tcpPort", "6999", "-tcpAllowOthers"});
        tcpServer.start();

        ConnectionPool dbPool = ConnectionPool.getOrInitInstance(ConnectionPoolTest.class,"jdbccfg.json");
        int i = SqlUtil.queryBaseObjectWithJdbc(dbPool.getConnection(),"select 1");
        System.out.println(i);
        System.exit(0);
    }
}
