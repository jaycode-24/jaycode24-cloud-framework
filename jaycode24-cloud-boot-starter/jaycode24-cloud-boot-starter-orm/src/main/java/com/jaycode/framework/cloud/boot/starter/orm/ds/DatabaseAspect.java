package com.jaycode.framework.cloud.boot.starter.orm.ds;

import com.jaycode.framework.cloud.boot.starter.orm.ds.annotation.Database;
import com.jaycode.framework.cloud.boot.starter.orm.ds.resource.DataSourceRouteManager;
import com.jaycode.framework.cloud.boot.starter.orm.ds.route.DataSourceRoute;
import com.jaycode.framework.cloud.boot.starter.orm.ds.route.DataSourceRouteLocator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * 数据源捆绑Aspect支持
 * @author cheng.wang
 * @date 2022/5/16
 */
@Aspect
public class DatabaseAspect {

    @Pointcut("(@annotation(com.jaycode.framework.cloud.boot.starter.orm.ds.annotation.Database) || @within(com.jaycode.framework.cloud.boot.starter.orm.ds.annotation.Database)) + " +
            "&& @within(org.springframework.stereotype.Service)")
    public void annotationPointCut(){}

    @Around("annotationPointCut()")
    public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Signature signature = pjp.getSignature();
        Method method = ((MethodSignature) signature).getMethod();
        Database database = getAnnotation(method);
        if (database == null){
            database = method.getDeclaringClass().getAnnotation(Database.class);
        }
        if (database == null){
            return pjp.proceed();
        }
        DataSourceRoute preDsr = DataSourceRouteLocator.getRoute();
        if (preDsr != null && database.value().equals(preDsr.getId())){
            //如果当前线程中已存在数据源路由，且与定义相同则无需切换
            return pjp.proceed();
        }
        //如果当前线程不存在数据源路由或与当前业务指定数据源路由不同，则进行切换工作
        String databaseRouteId = database.value();
        DataSourceRouteManager.initIfNecessary(databaseRouteId);
        DataSourceRoute newDsr = DataSourceRoute.builder().id(databaseRouteId).build();
        DataSourceRouteLocator.setRoute(newDsr);
        try {
            return pjp.proceed();
        }finally {
            //保证数据源状态一致的情况下，才进行清除操作，如果业务有自行重置DS且未及时释放，则不进行清除操作
            if (newDsr == DataSourceRouteLocator.getRoute()){
                DataSourceRouteLocator.clean();
                DataSourceRouteLocator.setRoute(preDsr);
            }
        }

    }

    private Database getAnnotation(Method method) {
        // 如果有多个annotation 似乎就不好用了 如放在controller上 由于已经有了@RequestMapping注解了 所以...
        if (method.isAnnotationPresent(Database.class)){
            return method.getAnnotation(Database.class);
        }
        return null;
    }
}
