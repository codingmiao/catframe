package org.wowtools.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wowtools.common.pcm.ltp.LtpProducer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 查询数据的生产者
 *
 * @author liuyu
 * @date 2017/3/21
 */
public abstract class PcmDbProducer<T> implements LtpProducer<T> {
    private final ResultSet rs;
    private boolean hasNext;
    private static final Logger log = LoggerFactory.getLogger(PcmDbProducer.class);
    /**
     * @param rs ResultSet
     */
    public PcmDbProducer(ResultSet rs) {
        this.rs = rs;
        try {
            hasNext = rs.next();
        } catch (SQLException e) {
            close();
            throw new RuntimeException(e);
        }
    }

    private void close(){
        try {
            rs.close();
        } catch (SQLException e) {
            log.warn("rs.close() error!!!!!!!!!!!!!!!11",e);
        }
    }


    /**
     * @param conn conn
     * @param sql  sql
     * @param args 绑定参数
     */
    public PcmDbProducer(Connection conn, String sql, Object... args) {
        this(SqlUtil.queryResultSet(conn, sql, args));
    }

    @Override
    public T produce() {
        if (hasNext) {
            try {
                T obj = rs2Obj(rs);
                hasNext = rs.next();
                return obj;
            } catch (Exception e) {
                close();
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }

    }

    /**
     * 将ResultSet中的row取出并转为需要的对象
     *
     * @param rs
     * @return
     */
    protected abstract T rs2Obj(ResultSet rs) throws SQLException;

    @Override
    public boolean isFinish() {
        if (hasNext){
            return false;
        }else {
            close();
            return true;
        }
    }
}
