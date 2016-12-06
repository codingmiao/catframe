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
			is = readInJarStream(clazz, path);
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
	
	public static InputStream readInJarStream(Class<?> clazz, String path){
		try {
			if(path.indexOf(0)!='/'){
				path = "/"+path;
			}
			return clazz.getResourceAsStream(path);
		} catch (Exception e) {
			throw new RuntimeException("读取配置文件异常", e);
		} finally {
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
	public static InputStream readInProjectStream(Class<?> clazz, String path) {
		try {
			String basePath = clazz.getClassLoader().getResource("/") != null
					? clazz.getClassLoader().getResource("/").getPath() : clazz.getResource("/").getPath();
			return new FileInputStream(basePath + path);
		} catch (Exception e) {
			throw new RuntimeException("读取配置文件异常", e);
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
	public static String readInProjectStr(Class<?> clazz, String path) {
		InputStream is = null;
		try {
			is = readInProjectStream(clazz, path);
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
	 * 优先读取项目中的资源，读不到再读取jar中的
	 * 
	 * @param clazz
	 * @param path
	 * @return
	 */
	public static String readStr(Class<?> clazz, String path) {
		try {
			return readInProjectStr(clazz, path);
		} catch (Exception e) {
			return readInJarStr(clazz, path);
		}
	}
	
	/**
	 * 优先读取项目中的资源，读不到再读取jar中的
	 * 
	 * @param clazz
	 * @param path
	 * @return
	 */
	public static InputStream readStream(Class<?> clazz, String path) {
		try {
			return readInProjectStream(clazz, path);
		} catch (Exception e) {
			return readInJarStream(clazz, path);
		}
	}

}
