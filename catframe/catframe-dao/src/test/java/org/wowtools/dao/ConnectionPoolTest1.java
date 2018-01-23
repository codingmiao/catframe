package org.wowtools.dao;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectionPoolTest1 {
    @Before
    public void init(){
        TcpServerCell.init();
    }

    @Test
    public void getName() throws Exception {
        ConnectionPool dbPool = ConnectionPool.getOrInitInstance(ConnectionPoolTest1.class, "jdbccfg.json");
        int i = SqlUtil.queryBaseObjectWithJdbc(dbPool.getConnection(), "select 1");
        assertEquals(1, i);
    }

}
