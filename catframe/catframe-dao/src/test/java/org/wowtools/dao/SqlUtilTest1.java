package org.wowtools.dao;

import org.h2.tools.Server;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @author liuyu
 * @date 2017/5/16
 */
public class SqlUtilTest1 {
    @Before
    public void init(){
        TcpServerCell.init();
    }

    @Test
    public void t() throws Exception {

        ConnectionPool dbPool = ConnectionPool.getOrInitInstance(ConnectionPoolTest1.class, "jdbccfg.json");
        SqlUtil.executeUpdate(dbPool.getConnection(), "CREATE TABLE TEST1(ID VARCHAR(200))");
        Collection<Object[]> params = new ArrayList<>(2);
        params.add(new Object[]{"test1.1"});
        params.add(new Object[]{"test1.2"});
        SqlUtil.batchUpdate(dbPool.getConnection(), "insert into test1 values(?)", params, true);
        Collection<String> res = SqlUtil.queryAndCollect(dbPool.getConnection(),
                (rs) -> rs.getString(1), null,
                "select * from test1");
        Iterator<String> iterator = res.iterator();
        assertEquals("test1.1", iterator.next());
        assertEquals("test1.2", iterator.next());
        assertFalse(iterator.hasNext());
    }
}
