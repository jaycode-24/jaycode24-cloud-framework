package com.jaycode.framework.cloud.boot.core.support;

/**
 * 运行时工具类
 * @author cheng.wang
 * @date 2022/5/13
 */
public class RuntimeUtils {

    /**
     * 判断当前运行是否在ide中
     *
     * @return 如果是则返回true
     */
    public static boolean isInIde() {
        String classPath = System.getProperty("java.class.path");
        return !org.apache.commons.lang.StringUtils.isEmpty(classPath) && classPath.contains("idea_rt.jar");
    }

    /**
     * 判断当前环境是否在windows操作系统中
     *
     * @return 如果是则返回true
     */
    public static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().startsWith("win");
    }
}
