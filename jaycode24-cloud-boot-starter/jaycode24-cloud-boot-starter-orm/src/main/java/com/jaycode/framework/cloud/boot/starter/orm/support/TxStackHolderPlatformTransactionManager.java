package com.jaycode.framework.cloud.boot.starter.orm.support;

import com.jaycode.framework.cloud.boot.starter.orm.ds.datasource.LoadBalanceDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import java.util.Stack;

/**
 * 事务调用栈持有对象，通过代理事务管理器实现持物调用栈持有
 *  * 用于获取线程中最近调用的事务状态，方便进行主从切换
 * @author cheng.wang
 * @date 2022/5/16
 * @see LoadBalanceDataSource
 */
public class TxStackHolderPlatformTransactionManager implements PlatformTransactionManager{
    private final PlatformTransactionManager platformTransactionManager;
    private static final ThreadLocal<Stack<TransactionDefinition>> TRANSACTION_DEF_STACK_HOLDER = new ThreadLocal<>();


    public TxStackHolderPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    /**
     * 获取事务状态，用于判断当前事务类型，方便数据库路由
     *
     * @return 事务定义
     */
    public static TransactionDefinition getCurrentTransactionStatus() {
        Stack<TransactionDefinition> stack = TRANSACTION_DEF_STACK_HOLDER.get();
        if (stack == null || stack.size() == 0){
            return null;
        }
        return stack.peek();
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        return null;
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {

    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {

    }
}
