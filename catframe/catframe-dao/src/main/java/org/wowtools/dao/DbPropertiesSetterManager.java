package org.wowtools.dao;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import java.sql.Timestamp;
import java.util.Date;

/**
 * 数据库字段值->对象属性映射管理
 * 
 * @author liuyu
 *
 */
public class DbPropertiesSetterManager {

	public static DbPropertiesSetter getDbPropertiesSetter(Field f) {
		Class<?> clazz = f.getType();
		if (Date.class.equals(clazz)) {
			return dateDbPropertiesSetter;
		}
		if (Long.class.equals(clazz) || long.class.equals(clazz)) {
			return longDbPropertiesSetter;
		}
		if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
			return intDbPropertiesSetter;
		}
		if (Double.class.equals(clazz) || double.class.equals(clazz)) {
			return doubleDbPropertiesSetter;
		}
		if (String.class.equals(clazz)) {
			return stringDbPropertiesSetter;
		}
		return commonDbPropertiesSetter;
	}

	private static void commonSet(Field f, Object obj, Object value) {
		try {
			f.set(obj, value);
		} catch (Exception e) {
			throw new RuntimeException("设置属性出错：" + f.getName(), e);
		}
	}

	@FunctionalInterface
	public static interface DbPropertiesSetter {
		public void set(Field f, Object obj, Object value);
	}

	public static DbPropertiesSetter commonDbPropertiesSetter = (f, obj, value) -> {
		commonSet(f, obj, value);
	};

	public static DbPropertiesSetter stringDbPropertiesSetter = (f, obj, value) -> {
		if (null == value) {
			commonSet(f, obj, null);
		} else {
			commonSet(f, obj, value.toString());
		}
	};

	public static DbPropertiesSetter longDbPropertiesSetter = (f, obj, value) -> {
		if (null == value) {
			commonSet(f, obj, null);
			return;
		}
		if (value instanceof BigDecimal) {
			BigDecimal bd = (BigDecimal) value;
			commonSet(f, obj, bd.longValue());
		} else if (value instanceof String) {
			commonSet(f, obj, Long.valueOf(value.toString()));
		} else {
			commonSet(f, obj, (Long) value);
		}

	};

	public static DbPropertiesSetter intDbPropertiesSetter = (f, obj, value) -> {
		if (null == value) {
			commonSet(f, obj, null);
			return;
		}
		if (value instanceof BigDecimal) {
			BigDecimal bd = (BigDecimal) value;
			commonSet(f, obj, bd.intValue());
		} else {
			commonSet(f, obj, (Integer) value);
		}

	};

	public static DbPropertiesSetter doubleDbPropertiesSetter = (f, obj, value) -> {
		if (null == value) {
			commonSet(f, obj, null);
			return;
		}
		if (value instanceof BigDecimal) {
			BigDecimal bd = (BigDecimal) value;
			commonSet(f, obj, bd.doubleValue());
		} else {
			commonSet(f, obj, (Double) value);
		}

	};

	public static DbPropertiesSetter dateDbPropertiesSetter = (f, obj, value) -> {
		if (null == value) {
			commonSet(f, obj, null);
			return;
		}
		Date d;
//		if (value instanceof oracle.sql.TIMESTAMP) {
//			oracle.sql.TIMESTAMP ts = (TIMESTAMP) value;
//			try {
//				d = ts.dateValue();
//			} catch (SQLException e) {
//				throw new RuntimeException("oracle.sql.TIMESTAMP转换date出错：" + f.getName(), e);
//			}
//		} else 
		if (value instanceof Timestamp) {
			Timestamp t = (Timestamp) value;
			d = new Date(t.getTime());
		} else if (value instanceof Long) {
			long t = (long) value;
			d = new Date(t);
		} else {
			throw new RuntimeException("未指定的时间对象类型:" + value.getClass());
		}
		commonSet(f, obj, d);
	};

}
