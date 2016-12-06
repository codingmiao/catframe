package org.wowtools.common.utils;

import java.util.Properties;

/**
 * properties文件读取工具
 * 
 * @author liuyu
 * @date 2016年12月5日
 */
public class PropertiesReader {
	private final Properties p;

	public PropertiesReader(Class<?> clazz, String path) {
		try {
			p = new Properties();
			p.load(ResourcesReader.readStream(clazz, path));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getString(String key) {
		return p.getProperty(key);
	}

	public Integer getInteger(String key) {
		String s = getString(key);
		if (null == s) {
			return null;
		}
		return Integer.valueOf(s);
	}

	public void clear() {
		p.clear();
	}
}
