package com.jaycode.framework.cloud.boot.starter.orm.injector;


import com.jaycode.framework.cloud.boot.starter.orm.injector.metadata.Model;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 仓库缓存，缓存仓库对象信息
 *
 * @author jinlong.wang
 */
public class RepositoryCache {
    //private Class repositoryClass;
    private final static Map<String, Model> MODEL_REPOSITORY_CACHE = new ConcurrentHashMap<>();
    private final static Map<String, Method> REPOSITORY_METHOD_CACHE = new ConcurrentHashMap<>();
    private final static Map<String, String> REPOSITORY_ROUTE_ID_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, String> REPOSITORY_METHOD_MAP_CACHE = new ConcurrentHashMap<>();

    private static final RepositoryCache INSTANCE = new RepositoryCache();

    private RepositoryCache() {

    }

    public static RepositoryCache getInstance() {
        return INSTANCE;
    }

    /**
     * @param statementId
     * @return
     */
    public Model getModel(String statementId) {
        if (!MODEL_REPOSITORY_CACHE.containsKey(statementId)) {
            for (Map.Entry<String, Model> entry : MODEL_REPOSITORY_CACHE.entrySet()) {
                if (statementId.contains(entry.getKey())) {
                    MODEL_REPOSITORY_CACHE.put(statementId, entry.getValue());
                    break;
                }
            }
        }
        return MODEL_REPOSITORY_CACHE.get(statementId);
    }

    public Method getMethod(String statementId) {
        return REPOSITORY_METHOD_CACHE.get(statementId);
    }


    public void addRepoModel(String name, Model modelMetaData) {
        MODEL_REPOSITORY_CACHE.put(name, modelMetaData);
    }

    public String getDataSourceRouteId(String methodName) {
        return REPOSITORY_ROUTE_ID_CACHE.get(REPOSITORY_METHOD_MAP_CACHE.get(methodName));
    }

    public void addRepoMethod(String repoName, String methodName, Method method) {
        String fullMethodName = repoName + "." + methodName;
        REPOSITORY_METHOD_MAP_CACHE.put(fullMethodName, repoName);
        REPOSITORY_METHOD_CACHE.put(fullMethodName, method);
    }

    public void addRepoDataSourceRouteId(String repositoryName, String dataSourceRouteId) {
        REPOSITORY_ROUTE_ID_CACHE.put(repositoryName, dataSourceRouteId);
    }
}
