package org.wowtools.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.*;
import java.util.Collection;
import java.util.Date;

/**
 * 直接用sql操作数据的工具类
 *
 * @author liuyu
 */
public class SqlUtil {
    private static final Logger log = LoggerFactory.getLogger(SqlUtil.class);

    /**
     * ResultSet遍历器
     *
     * @author liuyu
     */
    public static abstract class JdbcResultVisitor {
        public void beforLoop(ResultSet rs) throws SQLException {

        }

        public abstract void visit(ResultSet rs) throws SQLException;

        public void afterLoop(ResultSet rs) throws SQLException {

        }
    }

    /**
     * 简化的ResultSet遍历器，只有visit部分,以便使用labmbda表达式
     *
     * @author liuyu
     */
    @FunctionalInterface
    public interface SimpleJdbcResultVisitor {

        void visit(ResultSet rs) throws SQLException;

    }

    private static String getLogSqlAndParams(String sql, Object[] paramValue) {
        StringBuilder sb = new StringBuilder();
        sb.append("SqlUtil executeUpdate:\t").append(sql);
        sb.append("\nparams:");
        for (Object p : paramValue) {
            sb.append(p == null ? "null" : sb.toString()).append("\t");
        }
        return sb.toString();
    }

    /**
     * 执行hql CUD命令
     *
     * @param sql        sql
     * @param paramValue 参数值
     * @return the number of entities updated or deleted
     */
    public static int executeUpdateByNativeQuery(EntityManager em, String sql, Object[] paramValue) {
        if (log.isDebugEnabled()) {
            log.debug(getLogSqlAndParams(sql, paramValue));
        }

        try {
            Query query = em.createNativeQuery(sql);
            int i = 1;
            for (Object p : paramValue) {
                query.setParameter(i, p);
                i++;
            }

            return query.executeUpdate();
        } catch (Exception e) {
            log.debug(getLogSqlAndParams(sql, paramValue));
            throw new RuntimeException("执行异常,sql:" + sql, e);
        }
    }

    /**
     * 执行sql CUD命令
     *
     * @param sql        sql
     * @param paramValue 参数值
     * @return the number of entities updated or deleted
     */
    public static int executeUpdate(Connection conn, String sql, Object... paramValue) {
        return executeUpdate(conn, sql, true, paramValue);
    }

    /**
     * 执行sql CUD命令
     *
     * @param sql        sql
     * @param paramValue 参数值
     * @return the number of entities updated or deleted
     */
    public static int executeUpdate(Connection conn, String sql, boolean closeConn, Object... paramValue) {
        if (log.isDebugEnabled()) {
            log.debug(getLogSqlAndParams(sql, paramValue));
        }

        PreparedStatement pstm = null;
        try {
            pstm = conn.prepareStatement(sql);
            int i = 1;
            for (Object arg : paramValue) {
                pstm.setObject(i, toDbObj(arg));
                i++;
            }
            return pstm.executeUpdate();
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg.indexOf("ORA-00939") < 0) {
                log.debug(getLogSqlAndParams(sql, paramValue));
                throw new RuntimeException("执行异常,msg:" + e.getMessage() + ",sql:" + sql, e);
            }
            return 1;
        } finally {
            closePreparedStatement(pstm);
            closeConnection(conn);
        }
    }

    private static Object toDbObj(Object o) {
        if (o instanceof Date) {
            Date t = (Date) o;
            return new Timestamp(t.getTime());
        }
        return o;
    }

    /**
     * 批量执行CUD命令
     *
     * @param sql
     * @param paramValues
     * @return an array of update counts containing one element for each
     * command in the batch.  The elements of the array are ordered according
     * to the order in which commands were added to the batch.
     */
    public static int[] batchUpdate(Connection conn, String sql, Collection<Object[]> paramValues, boolean closeConn) {
        log.debug("SqlUtil batchUpdate:\t{}\t params:{}", sql, paramValues.size());
        PreparedStatement pstm = null;
        try {
            conn.setAutoCommit(false);
            pstm = conn.prepareStatement(sql);
            for (Object[] args : paramValues) {
                int i = 1;
                for (Object arg : args) {
                    pstm.setObject(i, toDbObj(arg));
                    i++;
                }
                pstm.addBatch();
            }
            int[] res = pstm.executeBatch();
            conn.commit();
            return res;
        } catch (Exception e) {
            throw new RuntimeException("batchUpdate异常,sql:" + sql, e);
        } finally {
            closePreparedStatement(pstm);
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e1) {
                log.warn("conn.setAutoCommit(true)异常", e1);
            }
            closeConnection(conn);
        }
    }

    /**
     * 查询java基本数据类型
     *
     * @param conn conn
     * @param sql  sql
     * @param args 绑定参数
     * @param <T>  返回类型泛型
     * @return 结果
     */
    public static <T> T queryBaseObjectWithJdbc(Connection conn, final String sql, final Object... args) {
        class Visitor extends JdbcResultVisitor {
            private int n = 0;
            private T res = null;

            @SuppressWarnings("unchecked")
            @Override
            public void visit(ResultSet rs) throws SQLException {
                if (n == 1) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("查询结果不唯一:\t").append(sql);
                    sb.append("\nparams:");
                    for (Object p : args) {
                        sb.append(p == null ? "null" : sb.toString()).append("\t");
                    }
                    throw new RuntimeException(sb.toString());
                }
                res = (T) rs.getObject(1);
                n++;
            }

        }
        Visitor v = new Visitor();
        queryWithJdbc(conn, v, sql, args);
        return v.res;
    }

    /**
     * 查询，并用一个访问器来处理每次rs.next()后的工作
     *
     * @param conn            conn
     * @param simpleRsVisitor 结果访问器
     * @param sql             sql
     * @param args            绑定参数
     */
    public static void queryWithJdbc(Connection conn, SimpleJdbcResultVisitor simpleRsVisitor, String sql,
                                     Object... args) {
        JdbcResultVisitor rsVisitor = new JdbcResultVisitor() {
            @Override
            public void visit(ResultSet rs) throws SQLException {
                simpleRsVisitor.visit(rs);
            }
        };
        queryWithJdbc(conn, rsVisitor, sql, args);
    }

    /**
     * 查询，并用一个访问器来处理每次rs.next()后的工作
     *
     * @param conn      conn
     * @param rsVisitor 结果访问器
     * @param sql       sql
     * @param args      绑定参数
     */
    public static void queryWithJdbc(Connection conn, JdbcResultVisitor rsVisitor, String sql, Object... args) {
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            pstm = conn.prepareStatement(sql);
            int i = 1;
            for (Object arg : args) {
                pstm.setObject(i, toDbObj(arg));
                i++;
            }
            rs = pstm.executeQuery();
            rsVisitor.beforLoop(rs);
            while (rs.next()) {
                rsVisitor.visit(rs);
            }
            rsVisitor.afterLoop(rs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
            closePreparedStatement(pstm);
            closeConnection(conn);
        }
    }

    /**
     * 查询，并返回一个结果集，当调用结果集的close方法时，其相应的Connection、Statement也会随之关闭,故必须在final模块中调用close方法
     *
     * @param conn conn
     * @param sql  sql
     * @param args 绑定参数
     */
    public static ResultSet queryResultSet(Connection conn, String sql, Object... args) {
        try {
            PreparedStatement pstm = conn.prepareStatement(sql);
            int i = 1;
            for (Object arg : args) {
                pstm.setObject(i, toDbObj(arg));
                i++;
            }
            ResultSet rs = pstm.executeQuery();
            DecoratorResultSet drs = new DecoratorResultSet(rs) {
                @Override
                public void close() throws SQLException {
                    super.close();
                    closePreparedStatement(pstm);
                    closeConnection(conn);
                }
            };
            return drs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static void closeResultSet(ResultSet rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (Exception e) {
                log.warn("closeResultSet异常", e);
            }
        }
    }

    private static void closePreparedStatement(PreparedStatement pstm) {
        if (null != pstm) {
            try {
                pstm.close();
            } catch (Exception e) {
                log.warn("closePreparedStatement异常", e);
            }
        }
    }

    private static void closeConnection(Connection conn) {
        if (null != conn) {
            try {
                conn.close();
            } catch (Exception e) {
                log.warn("closeConnection异常", e);
            }
        }
    }
}
