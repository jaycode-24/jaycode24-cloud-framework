package com.jaycode.framework.cloud.boot.core.bean.md;

import com.jaycode.framework.cloud.boot.core.bean.annotation.Module;
import com.jaycode.framework.cloud.boot.core.config.Config;
import com.jaycode.framework.cloud.boot.core.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义bean扫描
 * 实现模块的启用和禁用
 * @author cheng.wang
 * @date 2022/5/13
 */
@Slf4j
public class ModuleFilter implements TypeFilter {
    private static final String BASE_PACKAGE = "com.jaycode.";
    private static final String MODULE_PACKAGE_KEYWORD = ".module.";
    private static final String MODULE_LOAD_CONFIG_PREFIX = "jaycode.module";
    private static final String MODULE_DEF_ALIAS_KEY = "jaycode.module-alias";


    private static Map<String, String> MODULE_ALIAS = new HashMap<>();
    private static Map<String, Boolean> ALLOW_MODULES = new HashMap<>();

    static {
        readModuleConfig();
        readModuleAliasConfig();
    }

    private static void readModuleAliasConfig() {
        //刷新别名
        Config config = ConfigFactory.getBootstrapConfig();
        MODULE_ALIAS = config.getValue(MODULE_DEF_ALIAS_KEY);
        if (MODULE_ALIAS != null){
            MODULE_ALIAS.forEach((k,v) -> {
                checkModuleNameStyle(v);
            });
        }
    }

    private static void readModuleConfig() {
        //优先从common.yaml中读取模块配置
        Config config = ConfigFactory.getShareConfig();
        if (config.getValue(MODULE_LOAD_CONFIG_PREFIX) != null){
            ALLOW_MODULES = config.getValue(MODULE_LOAD_CONFIG_PREFIX);
        }
        //然后从本地应用中读取模块配置
        config = ConfigFactory.getAppConfig();
        if (config != null && !(config instanceof ConfigFactory.NotFoundConfig)){
            if (config.getValue(MODULE_LOAD_CONFIG_PREFIX) != null){
                Map<String,Boolean> appModules = config.getValue(MODULE_LOAD_CONFIG_PREFIX);
                if (!CollectionUtils.isEmpty(appModules)){
                    appModules.forEach((k,v) -> {
                        ALLOW_MODULES.put(getFinalModuleName(k),v);
                    });
                }
            }
        }
        ALLOW_MODULES.forEach((k,v) -> {
            checkModuleNameStyle(getFinalModuleName(k));
            //log.info(() -> (v ? "启用" : "禁用") + getFinalModuleName(k) + "模块");
        });
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        String className = metadataReader.getClassMetadata().getClassName();
        //基于包名识别module
        if (className.startsWith(BASE_PACKAGE) && className.contains(MODULE_PACKAGE_KEYWORD)){
            String packageModuleName = resolveModuleNameWithClassName(className);
            return excludeModule(className, packageModuleName);
        }
        //基于注解识别module
        if (metadataReader.getAnnotationMetadata().hasAnnotation(Module.class.getName())){
            Map<String, Object> attributes = metadataReader.getAnnotationMetadata().getAnnotationAttributes(Module.class.getName());
            if (attributes != null && attributes.containsKey("value")){
                return excludeModule(className, (String) attributes.get("value"));
            }

        }
        return false;
    }

    private boolean excludeModule(String className, String packageModuleName) {
        checkModuleNameStyle(packageModuleName);
        String finalModuleName = getFinalModuleName(packageModuleName);
        if (ALLOW_MODULES.containsKey(finalModuleName) && ALLOW_MODULES.get(finalModuleName).equals(Boolean.TRUE)){
            return false;
        }
        log.info("因未启用{}模块，{}不会注册到Bean容器中", finalModuleName, className);
        return true;

    }

    private static String getFinalModuleName(String moduleName) {
        if (MODULE_ALIAS == null){
            return moduleName;
        }
        if (MODULE_ALIAS.containsKey(moduleName)){
            return MODULE_ALIAS.get(moduleName);
        }
        return moduleName;
    }

    private static void checkModuleNameStyle(String packageModuleName) {
        Assert.hasText(packageModuleName,"模块解析出错，请参见模块定义标准");
        if (packageModuleName.contains(".")){
            throw new ModuleDefineException("模块名" + packageModuleName + "定义错误，请参见模块名定义标准：\r\n" +
                    "1 模块名或则别名不能包含.\r\n" +
                    "2 使用" + Module.class.getName() + "注解时，模块名推荐使用[_]下划线字符连接代表层级关系");

        }
    }

    /**
     * 通过类名返回包名
     * 正则表达式
     * @param className 类名
     * @return 模块名
     */
    private String resolveModuleNameWithClassName(String className) {
        Pattern pattern = Pattern.compile(".*module\\.(\\w+).*");
        Matcher matcher = pattern.matcher(className);
        if (matcher.matches()){
            return matcher.group(1);
        }
        return null;
    }
}
