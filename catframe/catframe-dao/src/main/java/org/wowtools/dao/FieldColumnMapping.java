package org.wowtools.dao;

import java.lang.reflect.Field;

import org.wowtools.dao.DbPropertiesSetterManager.DbPropertiesSetter;

/**
 * 对象属性和数据库字段的映射关系
 * 
 * @author liuyu
 *
 */
public class FieldColumnMapping {
	private String autoSqlParm;//数据库自动生成的sql片段，不一定有
	private String columnName;
	private Field field;
	private DbPropertiesSetter propertiesSetter;

	public FieldColumnMapping(String columnName, Field field,String autoSqlParm) {
		this.columnName = columnName;
		this.field = field;
		this.autoSqlParm = autoSqlParm;
		propertiesSetter = DbPropertiesSetterManager.getDbPropertiesSetter(field);
	}

	public String getColumnName() {
		return columnName;
	}

	public Field getField() {
		return field;
	}
	
	public void setValue(Object obj,Object value){
		propertiesSetter.set(field, obj, value);
	}

	public String getAutoSqlParm() {
		return autoSqlParm;
	}

}
