package com.jaycode.framework.cloud.boot.starter.orm.support;

import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.jaycode.framework.cloud.boot.core.config.ConfigConst;
import com.jaycode.framework.cloud.boot.starter.orm.ds.datasource.DataSourceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import javax.transaction.UserTransaction;

/**
 * @author cheng.wang
 * @date 2022/5/16
 */
@Slf4j
public class TransactionManager {

    private static final Boolean enableJta;
    //JAT事务超时设置，目前数据库配置的innodb_lock_wait_timeout 为50 ,程序配置事务超时 为60秒 ，不配置的情况下根据transaction timeout配置
    private static final Integer JTA_TRANSACTION_TIMEOUT = 60;

    static {
        enableJta = ConfigConst.isJtaEnabled();
        if (enableJta){
            //atomikos属性设置
            //解决end for XID '' raised -7: the XA resource has become unavailable
            System.setProperty("com.atomikos.icatch.serial_jta_transactions", "false");
            log.warn("当前应用已开启分布式事务，将使用Atomikos管理事务");
            log.warn("从0.3.0开始，框架支持本地事务路由切换，之前预想的XA使用场景，目前看来是一个伪命题，请考虑配置spring.jta.enable=false切换到非JTA事务。");
        }else {
            log.info("当前应用已开启本地事务，将使用Spring管理事务");
        }
    }
    public static PlatformTransactionManager acquireTransactionManager() {
        try {
            PlatformTransactionManager transactionManager;
            if (usingJta()){
                System.setProperty("com.atomikos.icatch.max_actives", "10000");
                UserTransactionManager userTransactionManager = new UserTransactionManager();
                userTransactionManager.setTransactionTimeout(JTA_TRANSACTION_TIMEOUT);
                UserTransaction userTransaction = new UserTransactionImp();
                transactionManager = new JtaTransactionManager(userTransaction, userTransactionManager);
                ((JtaTransactionManager) transactionManager).setDefaultTimeout(JTA_TRANSACTION_TIMEOUT);
            }else {
                DataSource dataSource = DataSourceFactory.getInstance().prepareDefaultDataSource();
                //因架构设计 只支持动态路由与静态路由，所以当静态路由不存在的时候，可以断定是动态路由
                if (dataSource == null){
                    //本地配置没有获取到数据源配置
                    log.warn("未找到默认数据源路由信息，使用动态路由实现");
                    dataSource = DataSourceFactory.getInstance().prepareDynamicDataSource();
                }
                transactionManager = new DataSourceTransactionManager(dataSource);
            }
            return new TxStackHolderPlatformTransactionManager(transactionManager);
        }catch (Exception e){
            throw new TransactionCreateException(e.getMessage(), e);
        }

    }

    public static boolean usingJta() {
        return enableJta;
    }
}
