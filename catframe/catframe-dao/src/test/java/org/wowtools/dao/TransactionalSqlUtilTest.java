package org.wowtools.dao;

import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author liuyu
 * @date 2017/5/16
 */
public class TransactionalSqlUtilTest {
    private JdbcConnectionPool connectionPool = JdbcConnectionPool.create("jdbc:h2:mem:test;MVCC=TRUE",
            "sa", "sa");

    private Connection getConn() {
        try {
            return connectionPool.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void init() {
        SqlUtil.executeUpdate(getConn(), "CREATE TABLE TEST1(ID VARCHAR(255))");
        SqlUtil.executeUpdate(getConn(), "CREATE TABLE TEST2(ID VARCHAR(255))");
    }

    @Test
    public void testCommit() {
        TransactionalSqlUtil tu = new TransactionalSqlUtil(getConn());

        tu.executeUpdate("insert into test1 values(?)", "test1.1");
        Collection<Object[]> params = new ArrayList<>(2);
        params.add(new Object[]{"test1.1"});
        params.add(new Object[]{"test1.2"});
        tu.batchUpdate("insert into test2 values(?)", params);
        tu.commit();

        long n1 = SqlUtil.queryBaseObjectWithJdbc(getConn(),
                "select count(*) from test1 where id = ?", "test1.1");
        assertEquals(1, n1);
        long n2 = SqlUtil.queryBaseObjectWithJdbc(getConn(),
                "select count(*) from test2 where id in(?,?)", "test1.1", "test1.2");
        assertEquals(2, n2);
    }

    @Test
    public void testRollBack() {
        TransactionalSqlUtil tu = new TransactionalSqlUtil(getConn());

        try {
            tu.executeUpdate("insert into test1 values(?)", "test2.1");
            Collection<Object[]> params = new ArrayList<>(2);
            params.add(new Object[]{"test2.1"});
            params.add(new Object[]{"test2.2"});
            tu.batchUpdate("insert into test2 values(?)", params);
            throw new RuntimeException();
        } catch (Exception e) {
            tu.rollback();
        }

        long n1 = SqlUtil.queryBaseObjectWithJdbc(getConn(),
                "select count(*) from test1 where id = ?", "test2.1");
        assertEquals(0, n1);
        long n2 = SqlUtil.queryBaseObjectWithJdbc(getConn(),
                "select count(*) from test2 where id in(?,?)", "test2.1", "test2.2");
        assertEquals(0, n2);
    }
    @Test
    public void testRollBackSavePoint() {
        TransactionalSqlUtil tu = new TransactionalSqlUtil(getConn());

        try {
            tu.executeUpdate("insert into test1 values(?)", "test3.1");
            tu.setSavepoint();
            Collection<Object[]> params = new ArrayList<>(2);
            params.add(new Object[]{"test3.1"});
            params.add(new Object[]{"test3.2"});
            tu.batchUpdate("insert into test2 values(?)", params);
            throw new RuntimeException();
        } catch (Exception e) {
            tu.rollbackToLastSavePoint();
            tu.commit();
        }

        long n1 = SqlUtil.queryBaseObjectWithJdbc(getConn(),
                "select count(*) from test1 where id = ?", "test3.1");
        assertEquals(1,n1);
        long n2 = SqlUtil.queryBaseObjectWithJdbc(getConn(),
                "select count(*) from test2 where id in(?,?)", "test3.1", "test3.2");
        assertEquals(0,n2);
    }
}
