package com.jaycode.framework.cloud.boot.core.support;

import cn.hutool.core.util.StrUtil;
import com.jaycode.framework.cloud.boot.core.JayCodeApplication;
import com.jaycode.framework.cloud.boot.core.config.ConfigConst;
import com.jaycode.framework.cloud.boot.core.config.ConfigServiceHolder;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.StringMemberValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

/**
 * 微服务启动流程
 * @author cheng.wang
 * @date 2022/5/13
 */
@Slf4j
public class BootstrapStream {

    /**
     * 自定义系统日志目录属性
     */
    private static final String SYSTEM_PROPERTY_LOG_PATH_KEY = "logging.path";
    /**
     * 默认系统日志目录名称
     */
    private static final String DEFAULT_LOG_DIR_NAME = "funi_cloud_log";
    /**
     * activemq受信包
     */
    private static final String ACTIVE_MQ_TRUST_PACKAGES = "java.lang,javax.security,java.util,org.apache.activemq,org.fusesource.hawtbuf,com.thoughtworks.xstream.mapper,org.apache.activemq.SERIALIZABLE_PACKAGES,com.jaycode.cloud";

    private boolean optimizeApplicationBootClass = false;

    /**
     * 启动参数
     */
    private final String[] args;

    public BootstrapStream(String... args){
        this.args = args;
        this.init();
    }

    private void init() {
        String logPath = System.getProperty(SYSTEM_PROPERTY_LOG_PATH_KEY);
        if (StrUtil.isBlank(logPath)){
            //如果日志路径为空，先设置
            if (RuntimeUtils.isWindows()){
                System.setProperty(SYSTEM_PROPERTY_LOG_PATH_KEY,
                        System.getProperty("java.io.tmpdir") + DEFAULT_LOG_DIR_NAME + File.separator + ConfigConst.getApplicationName());

            }else {
                System.setProperty(SYSTEM_PROPERTY_LOG_PATH_KEY ,
                        "/" + DEFAULT_LOG_DIR_NAME + "/" + ConfigConst.getApplicationName());
            }

        }
        log.info("应用日志记录：" + System.getProperty(SYSTEM_PROPERTY_LOG_PATH_KEY));
        System.out.println("\r\n==== JayCode Cloud Framework Version 1.0.0 ====");
        //关闭jmx
        System.setProperty("spring.jmx.enabled","false");
    }


    public static BootstrapStream of(String[] args) {
        return new BootstrapStream(args);
    }

    public BootstrapStream configNacos() {
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", ACTIVE_MQ_TRUST_PACKAGES);
        ConfigServiceHolder.initConfigService();
        return this;
    }

    public BootstrapStream optimizeApplicationBootClass() {
        this.optimizeApplicationBootClass = true;
        return this;
    }

    public void start(Class<?> cls) {
        if (optimizeApplicationBootClass) {
            SpringApplication.run(new Class[]{optimizeApplicationBootClassCompatibleWithModuleScan(cls), JayCodeApplication.class}, args);
        } else {
            SpringApplication.run(new Class[]{cls, JayCodeApplication.class}, args);
        }
    }

    /**
     * 优化启动类
     * 为SpringBootApplication指定scanBasePackages(如果业务应用没有指定则默认指定为none)。
     * 因SpringBootApplication注解实现了默认的bean扫描规则，而默认扫描规则会初始化Controller等常规注解Bean，这与模块化设计存在冲突。
     * 为解决该冲突，需要通过字节码修复的方式将启动类添加SpringBootApplication注解，并且什么扫描路径为none，则不会自动启用controller等注解
     *
     * @param cls 真实入口类
     * @return 优化后的入口类
     */
    private Class<?> optimizeApplicationBootClassCompatibleWithModuleScan(Class<?> cls) {
        try {
            ClassPool pool = ClassPool.getDefault();
            pool.insertClassPath(new ClassClassPath(cls));
            //获取一个Student类的CtClass对象
            CtClass ctClass = pool.get(cls.getName());
            ConstPool constpool = ctClass.getClassFile().getConstPool();
            AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
            Annotation bootApplication = new Annotation(SpringBootApplication.class.getName(), constpool);
            StringMemberValue[] stringMemberValues = new StringMemberValue[1];
            stringMemberValues[0] = new StringMemberValue("none", constpool);
            ArrayMemberValue arrayMemberValue = new ArrayMemberValue(constpool);
            arrayMemberValue.setValue(stringMemberValues);
            bootApplication.addMemberValue("scanBasePackages", arrayMemberValue);
            attr.addAnnotation(bootApplication);
            ctClass.getClassFile().addAttribute(attr);
            ClassLoader parent = cls.getClassLoader();
            EntityClassLoader loader = new EntityClassLoader(parent);
            Class<?> c = ctClass.toClass(loader, null);//替换Class的时候，  加载该Class的ClassLoader也必须用新的
            ctClass.detach(); //注销class

            return c;
        } catch (Exception e) {
            log.error("模块化配置失败，入口类优化错误" + e.getMessage());
        }
        return cls;


    }



    public static class EntityClassLoader extends ClassLoader {
        private final ClassLoader parent;

        private EntityClassLoader(ClassLoader parent) {
            this.parent = parent;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return this.loadClass(name, false);
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            Class<?> clazz = this.findLoadedClass(name);
            if (null != parent)
                clazz = parent.loadClass(name);
            if (null == clazz)
                this.findSystemClass(name);
            if (null == clazz)
                throw new ClassNotFoundException();
            if (resolve)
                this.resolveClass(clazz);
            return clazz;
        }
    }
}
