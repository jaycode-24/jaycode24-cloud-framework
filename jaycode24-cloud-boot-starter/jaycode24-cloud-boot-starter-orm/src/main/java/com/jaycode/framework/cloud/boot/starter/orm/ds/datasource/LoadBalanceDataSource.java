package com.jaycode.framework.cloud.boot.starter.orm.ds.datasource;

import cn.hutool.core.collection.CollUtil;
import com.jaycode.framework.cloud.boot.starter.orm.ds.resource.DataSourceGroup;
import com.jaycode.framework.cloud.boot.starter.orm.ds.route.DataSourceRoutingException;
import com.jaycode.framework.cloud.boot.starter.orm.support.TxStackHolderPlatformTransactionManager;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.transaction.TransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Random;

/**
 * 负载均衡数据库连接路由器
 * @author cheng.wang
 * @date 2022/5/16
 */
public abstract class LoadBalanceDataSource extends AbstractDataSource {

    @Override
    public Connection getConnection() throws SQLException {
        return routingDataSource().getConnection();
    }

    /**
     * 路由数据源，采用随机负载均衡算法
     * 如果当前业务没有声明事务，则只会读取从库
     * 如果当前业务声明了只读事务，则只会读取从库
     * 如果当前业务声明了非只读事务，则自动读取主库
     *
     * @return 数据源
     */
    private DataSource routingDataSource(){
        DataSourceGroup dataSourceGroup = routingDataSourceGroup();
        Boolean readMaster = Boolean.TRUE;
        TransactionDefinition transactionDefinition = TxStackHolderPlatformTransactionManager.getCurrentTransactionStatus();
        if (transactionDefinition == null){
            //没有任何事务定义，认为时从库读
            readMaster = Boolean.FALSE;
        }
        if (transactionDefinition != null && transactionDefinition.isReadOnly()){
            //有事务定义但是是只读事务，则认为是从库读
            readMaster = Boolean.FALSE;
        }
        logger.debug("当前请求路由到" + (readMaster ? "主" : "从") + "库");
        List<DataSource> dataSources = readMaster ? dataSourceGroup.getMasters() :
                (CollUtil.isEmpty(dataSourceGroup.getSalves()) ? dataSourceGroup.getMasters() : dataSourceGroup.getSalves());
        if (dataSources.size() == 1){
            return dataSources.get(0);
        }else if (dataSources.size() > 1){
            //存在多个则用随机取模算法
            return dataSources.get(
                    new Random().nextInt(dataSources.size() - 1)
            );
        }else {
            throw new DataSourceRoutingException("数据库路由失败，未找到" + (readMaster ? "主" : "从") + "库配置");

        }
    }

    protected abstract DataSourceGroup routingDataSourceGroup();

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return routingDataSource().getConnection(username,password);
    }
}
