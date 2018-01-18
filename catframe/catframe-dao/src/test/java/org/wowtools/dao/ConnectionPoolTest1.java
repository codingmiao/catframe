package org.wowtools.dao;

import org.h2.tools.Server;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
public class ConnectionPoolTest1 {
    @Test
    public void getName() throws Exception {
        Server tcpServer = Server
                .createTcpServer(new String[]{"-tcpPort", "6999", "-tcpAllowOthers"});
        tcpServer.start();

        ConnectionPool dbPool = ConnectionPool.getOrInitInstance(ConnectionPoolTest1.class,"jdbccfg.json");
        int i = SqlUtil.queryBaseObjectWithJdbc(dbPool.getConnection(),"select 1");
        assertEquals(1,i);
    }

}
