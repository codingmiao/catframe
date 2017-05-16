package org.wowtools.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Collection;

/**
 * 事务型的sql工具类,若不希望使用@Transactional注解，而是自己控制提交/回滚，可按如下方式使用：
 * <p>
 * <pre>
 * TransactionalSqlUtil util = new TransactionalSqlUtil(conn);
 * try {
 * 	util.executeUpdate(sqlA);
 * 	util.executeUpdate(sqlB);
 * } catch (Exception e) {
 * 	util.rollback();
 * }
 * util.commit();
 *
 * </pre>
 *
 * @author liuyu
 * @date 2016年7月8日
 */
public class TransactionalSqlUtil {
    private static final ThreadLocal<TransactionalSqlUtil> threadLocal = new ThreadLocal<TransactionalSqlUtil>();

    private final Connection conn;

    /**
     * 利用传入的conn执行事务，conn将在执行提交或回滚后关闭
     *
     * @param conn
     */
    public TransactionalSqlUtil(Connection conn) {
        try {
            conn.setAutoCommit(false);
        } catch (Exception e) {
            close();
            throw new RuntimeException("无法执行conn.setAutoCommit(false)", e);
        }
        this.conn = conn;
    }

    /**
     * 获取当前线程中的TransactionalSqlUtil，没有则返回null
     *
     * @return
     */
    public static TransactionalSqlUtil getThreadLocal() {
        return threadLocal.get();
    }

    /**
     * 设置当前线程中的TransactionalSqlUtil
     *
     * @param u
     */
    public static void setThreadLocal(TransactionalSqlUtil u) {
        threadLocal.set(u);
    }


    private Savepoint lastSavepoint;

    /**
     * 设置保存点
     *
     * @return
     */
    public Savepoint setSavepoint() {
        try {
            lastSavepoint = conn.setSavepoint();
            return lastSavepoint;
        } catch (SQLException e) {
            throw new RuntimeException("setSavepoint失败", e);
        }
    }

    /**
     * 设置保存点
     *
     * @param name
     * @return
     */
    public Savepoint setSavepoint(String name) {
        try {
            return conn.setSavepoint(name);
        } catch (SQLException e) {
            throw new RuntimeException("setSavepoint失败", e);
        }
    }

    /**
     * 释放保存点
     *
     * @param savepoint
     */
    public void releaseSavepoint(Savepoint savepoint) {
        try {
            conn.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            throw new RuntimeException("releaseSavepoint失败", e);
        }
    }

    /**
     * 回滚到指定保存点
     *
     * @param savepoint
     */
    public void rollback(Savepoint savepoint) {
        try {
            conn.rollback(savepoint);
        } catch (SQLException e) {
            throw new RuntimeException("rollback Savepoint失败", e);
        }
    }

    /**
     * 回滚到指定保存点
     */
    public void rollbackToLastSavePoint() {
        try {
            conn.rollback(lastSavepoint);
        } catch (SQLException e) {
            throw new RuntimeException("rollback Savepoint失败", e);
        }
    }


    /**
     * 提交事务
     */
    public void commit() {
        try {
            conn.commit();
        } catch (Exception e) {
            throw new RuntimeException("提交事务异常", e);
        } finally {
            close();
        }
    }

    /**
     * 回滚事务
     */
    public void rollback() {
        try {
            conn.rollback();
        } catch (Exception e) {
            throw new RuntimeException("回滚事务异常", e);
        } finally {
            close();
        }
    }

    private void close() {
        try {
            conn.close();
        } catch (Exception e) {
            throw new RuntimeException("关闭数据库连接失败", e);
        }
    }

    /**
     * 批量执行CUD命令
     *
     * @param sql
     * @param paramValues
     * @return
     */
    public int[] batchUpdate(String sql, Collection<Object[]> paramValues) {
        return SqlUtil.batchUpdate(conn, sql, paramValues, false);
    }

    /**
     * 执行sql CUD命令
     *
     * @param sql
     * @param paramValue 参数值
     * @return the number of entities updated or deleted
     */
    public int executeUpdate(String sql, Object... paramValue) {
        return SqlUtil.executeUpdate(conn, sql, false, paramValue);
    }
}
