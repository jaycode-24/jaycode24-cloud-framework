package com.jaycode.framework.cloud.boot.starter.orm.support;

import com.jaycode.framework.cloud.boot.starter.orm.support.database.DmDatabaseFeature;
import com.jaycode.framework.cloud.boot.starter.orm.support.database.MysqlDataBaseFeature;
import com.jaycode.framework.cloud.boot.starter.orm.support.database.OracleDatabaseFeature;
import com.sun.deploy.uitoolkit.ui.ConsoleController;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cheng.wang
 * @date 2022/5/17
 */
@Slf4j
public class DatabaseProduct {
    private static final DatabaseFeature DEFAULT = new MysqlDataBaseFeature();

    private static final Map<String, DatabaseFeature> REGISTRY = new HashMap<>();

    static {
        Arrays.asList(new DatabaseFeature[]{
                new MysqlDataBaseFeature(),
                new OracleDatabaseFeature(),
                new DmDatabaseFeature()
        }).forEach(d -> {
            REGISTRY.put(d.getProductName(),d);
        });
    }

    public static DatabaseFeature getFeature(Connection connection) {
        try {
            return getFeature(connection.getMetaData().getURL());
        } catch (Exception e) {
            log.warn("未识别数据库产品名，采用mysql作为默认数据库连接驱动");
            return DEFAULT;
        }
    }

    public static DatabaseFeature getFeature(String jdbcUrl) {
        String jdbcUrlCaseSensitivity = jdbcUrl.toLowerCase();
        for (Map.Entry<String, DatabaseFeature> entry : REGISTRY.entrySet()) {
            if (jdbcUrlCaseSensitivity.startsWith("jdbc:" + entry.getKey() + ":")) {
                return entry.getValue();
            }
        }
        return DEFAULT;
    }
}
