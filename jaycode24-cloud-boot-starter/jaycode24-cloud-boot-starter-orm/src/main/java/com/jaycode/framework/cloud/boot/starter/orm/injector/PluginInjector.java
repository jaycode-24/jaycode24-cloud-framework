package com.jaycode.framework.cloud.boot.starter.orm.injector;

import com.jaycode.framework.cloud.boot.starter.orm.entity.EntityPrepareInterceptor;
import com.jaycode.framework.cloud.boot.starter.orm.entity.EntityQueryInterceptor;
import com.jaycode.framework.cloud.boot.starter.orm.entity.EntityResultInterceptor;
import com.jaycode.framework.cloud.boot.starter.orm.entity.VersionLockInterceptor;
import com.jaycode.framework.cloud.boot.starter.orm.injector.statement.anotation.AutoInjector;
import com.jaycode.framework.cloud.boot.starter.orm.injector.statement.anotation.SqlStatement;
import com.jaycode.framework.cloud.boot.starter.orm.page.PaginationInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.reflections.Reflections;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.*;

/**
 * Mybatis 增强注入
 *  * 包括基础crud以及通用拦截器注入配置，同时支持ext扩展包扩展插件应用
 * @author cheng.wang
 * @date 2022/5/16
 */
@Slf4j
public class PluginInjector {
    private static final String EXT_PACKAGE = "com.funi.cloud.ext";


    private final static List<Interceptor> interceptorList = new ArrayList<>();


    private Set<Class<?>> sqlStatementSet = new HashSet<>();


    static {
        //安装默认插件
        interceptorList.add(new EntityQueryInterceptor());
        interceptorList.add(new EntityPrepareInterceptor());
        interceptorList.add(new VersionLockInterceptor());
        interceptorList.add(new EntityResultInterceptor());
        interceptorList.add(new PaginationInterceptor());
        //安装拓展插件
        Reflections reflections = new Reflections(EXT_PACKAGE);
        Set<Class<? extends Interceptor>> pluginInterceptors = reflections.getSubTypesOf(Interceptor.class);
        pluginInterceptors.stream().map((p) -> {
            try {
                if (p.isAnnotationPresent(AutoInjector.class)){
                    Interceptor obj = p.newInstance();
                    log.info("ORM插件" + p.getName() + "自动注入成功");
                    return obj;
                }
                return null;
            }catch (Exception e){
                log.error("ORM插件" + p.getName() + "初始化失败", e);
                return null;
            }
        }).filter(Objects::nonNull).forEach(interceptorList::add);
        AnnotationAwareOrderComparator.sort(interceptorList);
    }


    public PluginInjector(){
        Reflections reflections = new Reflections(PluginInjector.class.getPackage().getName());
        this.sqlStatementSet = reflections.getTypesAnnotatedWith(SqlStatement.class);
    }


    public void inject(Configuration configuration, Class<?> repositoryClass) {
        //todo
    }
}
