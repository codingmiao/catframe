package org.wowtools.dao;

/**
 * 执行sql产生的异常
 *
 * @author liuyu
 * @date 2017/7/14
 */
public class SqlException extends RuntimeException {
    public SqlException(Throwable cause, String sql, Object[] paramValue) {
        super(getLogSqlAndParams(sql, paramValue), cause);
    }

    public SqlException(String msg, String sql, Object[] paramValue) {
        super(msg + "\t" + getLogSqlAndParams(sql, paramValue));
    }

    public SqlException(String msg, Throwable cause, String sql, Object[] paramValue) {
        super(msg + "\t" + getLogSqlAndParams(sql, paramValue), cause);
    }

    private static String getLogSqlAndParams(String sql, Object[] paramValue) {
        StringBuilder sb = new StringBuilder("execute sql exception:\t");
        sb.append(sql);
        sb.append("\nparams:");
        for (Object p : paramValue) {
            sb.append(p == null ? "null" : p.toString()).append(",\t");
        }
        return sb.toString();
    }
}
