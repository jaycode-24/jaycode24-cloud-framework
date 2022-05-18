package com.jaycode.framework.cloud.boot.core.entity;

import com.funi.framework.cloud.boot.core.CloudFrameworkException;
import com.funi.framework.cloud.boot.core.entity.persistence.PreArchived;
import org.springframework.util.StringUtils;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体管理器，保存实体的元属性数据，通过单例实现避免性能消耗
 *
 * @author jinlong.wang
 */
public class EntityManager {

    private static final EntityManager ENTITY_MANAGER = new EntityManager();

    private static final Map<Class<?>, EntityMeta> ENTITY_META_CACHE = new ConcurrentHashMap<>();

    private static final Map<Class<?>, String> PROPERTY_FUNC_CACHE = new ConcurrentHashMap<>();


    public static EntityManager getInstance() {
        return ENTITY_MANAGER;
    }

    public boolean isEntityObject(Object paramObject) {
        return paramObject != null && paramObject.getClass().isAnnotationPresent(javax.persistence.Entity.class);
    }

    public boolean isEntity(Class<?> entityClass) {
        return entityClass != null && entityClass.isAnnotationPresent(javax.persistence.Entity.class);
    }

    public void prepareUpdate(Object paramObject) {
        EntityMeta entityMeta = getEntityMeta(paramObject.getClass());
        entityMeta.invokePostMethod(PreUpdate.class, paramObject);
    }

    public void preparePersist(Object paramObject) {
        EntityMeta entityMeta = getEntityMeta(paramObject.getClass());
        entityMeta.generateId(paramObject);
        entityMeta.invokePostMethod(PrePersist.class, paramObject);
    }

    public EntityMeta getEntityMeta(Class<?> modelClass) {
        if (!ENTITY_META_CACHE.containsKey(modelClass)) {
            ENTITY_META_CACHE.put(modelClass, new EntityMeta(modelClass));
        }
        return ENTITY_META_CACHE.get(modelClass);
    }


    public <T, R> String getPropertyName(PropertyFunc<T, R> func) {
        Class<?> lambdaClass = func.getClass();
        if (!PROPERTY_FUNC_CACHE.containsKey(lambdaClass)) {
            try {
                // 通过获取对象方法，判断是否存在该方法
                Method method = func.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(Boolean.TRUE);
                // 利用jdk的SerializedLambda 解析方法引用
                SerializedLambda serializedLambda = (SerializedLambda) method.invoke(func);
                String getter = serializedLambda.getImplMethodName();
                String pName = resolveFieldName(getter);
                if (StringUtils.isEmpty(pName)) {
                    throw new CloudFrameworkException("不应该出现的异常，获取属性名失败，当前获取属性名为空，对应getter为:" + getter + ",SerializedLambda info :" + serializedLambda.toString());
                }
                PROPERTY_FUNC_CACHE.put(lambdaClass, pName);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        String pString = PROPERTY_FUNC_CACHE.get(lambdaClass);
        if (StringUtils.isEmpty(pString)) {
            throw new CloudFrameworkException("不应该出现的异常，获取属性名失败，当前获取属性名为空，对应lambdaClass为:" + lambdaClass);
        }
        return pString;
    }


    private String resolveFieldName(String getMethodName) {
        if (getMethodName.startsWith("get")) {
            getMethodName = getMethodName.substring(3);
        } else if (getMethodName.startsWith("is")) {
            getMethodName = getMethodName.substring(2);
        }
        // 小写第一个字母
        return firstToLowerCase(getMethodName);
    }

    private String firstToLowerCase(String param) {
        if (org.apache.commons.lang.StringUtils.isBlank(param)) {
            return "";
        }
        return param.substring(0, 1).toLowerCase() + param.substring(1);
    }

    public void prepareArchived(Object paramObject) {
        EntityMeta entityMeta = getEntityMeta(paramObject.getClass());
        entityMeta.invokePostMethod(PreArchived.class, paramObject);
    }
}
