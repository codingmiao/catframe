package org.wowtools.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 直接用sql操作数据的工具类
 * 
 * @author liuyu
 *
 */
public class SqlUtil {
	private static final Logger log = LoggerFactory.getLogger(SqlUtil.class);

	/**
	 * ResultSet遍历器
	 * 
	 * @author liuyu
	 *
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
	 *
	 */
	@FunctionalInterface
	public static interface SimpleJdbcResultVisitor {

		public void visit(ResultSet rs) throws SQLException;

	}

	/**
	 * 执行hql CUD命令
	 * 
	 * @param sql
	 * @param paramValue
	 *            参数值
	 * @return the number of entities updated or deleted
	 */
	public static int executeUpdateByNativeQuery(EntityManager em, String sql, Object[] paramValue) {
		if (log.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer();
			sb.append("SqlUtil executeUpdate:\t").append(sql);
			sb.append("\nparams:");
			for (Object p : paramValue) {
				sb.append(p == null ? "null" : sb.toString()).append("\t");
			}
			log.debug(sb.toString());
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
			e.printStackTrace();
			throw new RuntimeException("执行异常,sql:" + sql, e);
		}
	}

	/**
	 * 执行sql CUD命令
	 * 
	 * @param sql
	 * @param paramValue
	 *            参数值
	 * @return the number of entities updated or deleted
	 */
	public static int executeUpdate(Connection conn, String sql, Object... paramValue) {
		return executeUpdate(conn, sql, true, paramValue);
	}

	/**
	 * 执行sql CUD命令
	 * 
	 * @param sql
	 * @param paramValue
	 *            参数值
	 * @return the number of entities updated or deleted
	 */
	public static int executeUpdate(Connection conn, String sql, boolean closeConn, Object... paramValue) {
		if (log.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer();
			sb.append("SqlUtil executeUpdate:\t").append(sql);
			sb.append("\nparams:");
			for (Object p : paramValue) {
				sb.append(p == null ? "null" : sb.toString()).append("\t");
			}
			log.debug(sb.toString());
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
				e.printStackTrace();
				throw new RuntimeException("执行异常,msg:" + e.getMessage() + ",sql:" + sql, e);
			}
			return 1;
		} finally {
			if (null != pstm) {
				try {
					pstm.close();
				} catch (Exception e) {
				}
			}
			if (closeConn && null != conn) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
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
	 * @return
	 */
	public static int[] batchUpdate(Connection conn, String sql, Collection<Object[]> paramValues, boolean closeConn) {
		if(log.isDebugEnabled()){
			log.debug("SqlUtil batchUpdate:\t" + sql + "\t params:" + paramValues.size());
		}
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
			throw new RuntimeException(e);
		} finally {
			if (null != pstm) {
				try {
					pstm.close();
				} catch (Exception e) {
				}
			}
			try {
				conn.setAutoCommit(true);
			} catch (SQLException e1) {
			}
			if (closeConn && null != conn) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 查询java基本数据类型
	 * 
	 * @return
	 */
	public static <T> T queryBaseObjectWithJdbc(Connection conn, final String sql, final Object... args) {
		class Visitor extends JdbcResultVisitor {
			int n = 0;
			T res = null;

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
		;
		Visitor v = new Visitor();
		queryWithJdbc(conn, v, sql, args);
		return v.res;
	}
	
	public static void queryWithJdbc(Connection conn, SimpleJdbcResultVisitor simpleRsVisitor, String sql, Object... args) {
		JdbcResultVisitor rsVisitor = new JdbcResultVisitor() {
			@Override
			public void visit(ResultSet rs) throws SQLException {
				simpleRsVisitor.visit(rs);
			}
		};
		queryWithJdbc(conn, rsVisitor, sql, args);
	}

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
			if (null != rs) {
				try {
					rs.close();
				} catch (Exception e) {
				}
			}
			if (null != pstm) {
				try {
					pstm.close();
				} catch (Exception e) {
				}
			}
			if (null != conn) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
	}
}
