package com.jaycode.framework.cloud.boot.starter.orm.injector;

import com.jaycode.framework.cloud.boot.starter.orm.entity.EntityPrepareInterceptor;
import com.jaycode.framework.cloud.boot.starter.orm.entity.EntityQueryInterceptor;
import com.jaycode.framework.cloud.boot.starter.orm.entity.EntityResultInterceptor;
import com.jaycode.framework.cloud.boot.starter.orm.entity.VersionLockInterceptor;
import com.jaycode.framework.cloud.boot.starter.orm.injector.metadata.Model;
import com.jaycode.framework.cloud.boot.starter.orm.injector.statement.AbstractStatement;
import com.jaycode.framework.cloud.boot.starter.orm.injector.statement.anotation.AutoInjector;
import com.jaycode.framework.cloud.boot.starter.orm.injector.statement.anotation.SqlStatement;
import com.jaycode.framework.cloud.boot.starter.orm.page.PaginationInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.reflections.Reflections;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
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

    private LanguageDriver languageDriver;
    private Set<Class<?>> sqlStatementSet = new HashSet<>();
    private final RepositoryCache repositoryCache = RepositoryCache.getInstance();



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


    public void inject(Configuration configuration, Class<?> repositoryClass) throws Exception{
        //注入插件
        interceptorList.forEach(configuration::addInterceptor);
        //this.repositoryClass = repositoryClass;
        Class<?> modelClass = extractModelClass(repositoryClass);
        //可能是非实体仓库定义，则忽略自动生成的crud
        if (modelClass != null){
            Model modelMetaData = new Model(modelClass);
            if (modelMetaData.getEntityMeta() == null){
                log.warn(repositoryClass + "CRUD基础操作创建失败，对应的实体对象" + modelClass + "未实现@Entity注解");
            }else {
                //将模型和仓库都注册到内存中，方便后续解析
                repositoryCache.addRepoModel(repositoryClass.getName(), modelMetaData);
                this.languageDriver = configuration.getDefaultScriptingLanguageInstance();
                for (Class cls : sqlStatementSet) {
                    SqlStatement sqlStatement = (SqlStatement) cls.getAnnotation(SqlStatement.class);
                    Class resultClass = sqlStatement.resultType() == Object.class ? modelMetaData.getModelClass() : sqlStatement.resultType();
                    this.createCommandStatement(
                            configuration,
                            (AbstractStatement) cls.getConstructor(Model.class, java.lang.Class.class).newInstance(modelMetaData, repositoryClass),
                            sqlStatement.commandType(),
                            resultClass);
                }
                //绑定基础操作
            }
        }
    }


    /**
     * 提取泛型模型,多泛型的时候请将泛型T放在第一位
     *
     * @param mapperClass mapper 接口
     * @return mapper 泛型
     */
    protected Class<?> extractModelClass(Class<?> mapperClass) {
        Type[] types = mapperClass.getGenericInterfaces();
        ParameterizedType target = null;
        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                Type[] typeArray = ((ParameterizedType) type).getActualTypeArguments();
                if (typeArray != null && typeArray.length > 0) {
                    for (Type t : typeArray) {
                        if (t instanceof TypeVariable || t instanceof WildcardType) {
                            break;
                        } else {
                            target = (ParameterizedType) type;
                            break;
                        }
                    }
                }
                break;
            }
        }
        return target == null ? null : (Class<?>) target.getActualTypeArguments()[0];
    }

    private void createCommandStatement(Configuration configuration, AbstractStatement statement, SqlCommandType sqlCommandType, final Class<?> resultType) {
        if (hasMappedStatement(configuration, statement)) {
            log.warn("MappedStatement " + statement.id() + " Already Exists");
            return;
        }
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, statement.sql(), Map.class);
        MappedStatement ms = new MappedStatement.Builder(configuration, statement.id(), sqlSource, sqlCommandType).resultMaps(
                new ArrayList<ResultMap>() {
                    {
                        add(new ResultMap.Builder(configuration, "defaultResultMap", resultType, new ArrayList<>(0))
                                .build());
                    }
                }).build();
        // 缓存
        configuration.addMappedStatement(ms);
    }

    /**
     * 是否已经存在MappedStatement
     *
     * @param statement
     * @return
     */
    private boolean hasMappedStatement(Configuration configuration, AbstractStatement statement) {
        return configuration.hasStatement(statement.id(), false);
    }
}
