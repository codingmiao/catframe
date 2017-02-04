package org.wowtools.common.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * 文件资源读取工具类
 *
 * @author liuyu
 * @date 2017/2/3
 */
public class ResourcesReader {


    /**
     * 获得class所在根路径，若class被打成jar包，则返回jar所在根路径
     *
     * @param clazz 定位用的类
     * @return 根目录路径
     */
    public static String getClassRootPath(Class<?> clazz) {
        java.net.URL url = clazz.getProtectionDomain().getCodeSource()
                .getLocation();
        String filePath;
        try {
            filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
        } catch (Exception e) {
            throw new RuntimeException("获取class路径失败", e);
        }
        if (filePath.endsWith(".jar"))
            filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        java.io.File file = new java.io.File(filePath);
        filePath = file.getAbsolutePath();
        return filePath;
    }

    /**
     * 获得项目根路径
     *
     * @param clazz 定位用的类
     * @return
     */
    public static String getRootPath(Class<?> clazz) {
        // 检查用户传入的参数是否为空

        ClassLoader loader = clazz.getClassLoader();
        // 获得类的全名，包括包名
        String clsName = clazz.getName();
        // 将类的class文件全名改为路径形式
        String clsPath = clsName.replace(".", "/") + ".class";

        // 调用ClassLoader的getResource方法，传入包含路径信息的类文件名
        java.net.URL url = loader.getResource(clsPath);
        // 从URL对象中获取路径信息
        String realPath = url.getPath();
        // 去掉路径信息中的协议名"file:"
        int pos = realPath.indexOf("file:");
        if (pos > -1) {
            realPath = realPath.substring(pos + 5);
        }
        // 去掉路径信息最后包含类文件信息的部分，得到类所在的路径
        pos = realPath.indexOf(clsPath);
        realPath = realPath.substring(0, pos - 1);
        // 如果类文件被打包到JAR等文件中时，去掉对应的JAR等打包文件名
        if (realPath.endsWith("!")) {
            realPath = realPath.substring(0, realPath.lastIndexOf("/"));
        }
        java.io.File file = new java.io.File(realPath);
        realPath = file.getAbsolutePath();

        try {
            realPath = java.net.URLDecoder.decode(realPath, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return realPath;
    }

    /**
     * 读取类所处根目录下的文件路径加上path的文件内容为String
     *
     * @param clazz 定位用的类
     * @param path  类根路径下的相对路径
     * @return
     */
    public static String readStr(Class<?> clazz, String path) {
        InputStream is = null;
        try {
            is = readStream(clazz, path);
            byte b[] = new byte[is.available()];
            is.read(b);
            String res = new String(b, "UTF-8");
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
     * 读取类所处根目录下的文件路径加上path的文件内容为InputStream
     *
     * @param clazz 定位用的类
     * @param path  类根路径下的相对路径
     * @return
     */
    public static InputStream readStream(Class<?> clazz, String path) {
        try {
            String basePath = getRootPath(clazz);
            return new FileInputStream(basePath + "/" + path);
        } catch (Exception e) {
            throw new RuntimeException("读取配置文件异常", e);
        }
    }

}
