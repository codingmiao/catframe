package org.wowtools.dao;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author liuyu
 * @date 2017/5/16
 */
public class SqlUtilTest {
    private static JdbcConnectionPool connectionPool = JdbcConnectionPool.create("jdbc:h2:mem:test;MVCC=TRUE",
            "sa", "sa");

    static {
        SqlUtil.executeUpdate(getConn(), "CREATE TABLE TEST1(ID VARCHAR(255))");
    }

    private static Connection getConn() {
        try {
            return connectionPool.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Collection<Object[]> params = new ArrayList<>(2);
        params.add(new Object[]{"test1.1"});
        params.add(new Object[]{"test1.2"});
        SqlUtil.batchUpdate(getConn(), "insert into test1 values(?)", params, true);
        Collection<String> res = SqlUtil.queryAndCollect(getConn(),
                (rs) -> rs.getString(1), null,
                "select * from test1");
        for (String s : res) {
            System.out.println(s);
        }
    }
}
