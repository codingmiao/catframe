package org.wowtools.common.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourcesReader {

	/**
	 * 读取jar包中的资源文件
	 * 
	 * @param clazz
	 * @param path
	 *            资源相对路径，如：res.txt
	 * @return
	 */
	public static String readInJarStr(Class<?> clazz, String path) {
		InputStream is = null;
		try {
			if(path.indexOf(0)!='/'){
				path = "/"+path;
			}
			is = clazz.getResourceAsStream(path);
			byte b[] = new byte[is.available()];
			is.read(b);
			String res = new String(b);
			return res;
		} catch (Exception e) {
			throw new RuntimeException("读取配置文件异常", e);
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * 读取项目中的资源
	 * 
	 * @param clazz
	 * @param path
	 *            资源相对路径，如：res.txt
	 * @return
	 */
	public static String readInProject(Class<?> clazz, String path) {
		String basePath = clazz.getClassLoader().getResource("/") != null
				? clazz.getClassLoader().getResource("/").getPath() : clazz.getResource("/").getPath();
		return readFileStr(basePath + path);
	}
	
	/**
	 * 读取文件中的字符串
	 * @param path
	 * @return
	 */
	public static String readFileStr(String path){
		InputStream is = null;
		try {
			is = new FileInputStream(path);
			byte b[] = new byte[is.available()];
			is.read(b);
			String res = new String(b);
			return res;
		} catch (Exception e) {
			throw new RuntimeException("读取文件异常", e);
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * 优先读取项目中的资源，读不到再读取jar中的
	 * 
	 * @param clazz
	 * @param path
	 * @return
	 */
	public static String read(Class<?> clazz, String path) {
		try {
			return readInProject(clazz, path);
		} catch (Exception e) {
			return readInJarStr(clazz, path);
		}
	}

}
