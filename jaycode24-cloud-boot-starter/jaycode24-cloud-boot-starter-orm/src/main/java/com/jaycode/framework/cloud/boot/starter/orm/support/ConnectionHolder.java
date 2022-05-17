package com.jaycode.framework.cloud.boot.starter.orm.support;

import java.sql.Connection;

/**
 * @author cheng.wang
 * @date 2022/5/17
 */
public class ConnectionHolder {
    private static final ThreadLocal<Connection> CONNECTION_HOLDER = new ThreadLocal<>();

    public static void setConnection(Connection connection) {
        CONNECTION_HOLDER.set(connection);
    }

    public static Connection getConnection() {
        return CONNECTION_HOLDER.get();
    }

    public static void empty() {
        CONNECTION_HOLDER.remove();
    }
}
