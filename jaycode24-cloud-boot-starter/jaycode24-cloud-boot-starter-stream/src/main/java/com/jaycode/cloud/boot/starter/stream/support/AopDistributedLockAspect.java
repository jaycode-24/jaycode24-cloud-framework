package com.jaycode.cloud.boot.starter.stream.support;

import com.jaycode.cloud.boot.starter.stream.values.Message;
import com.jaycode.cloud.boot.starter.stream.values.Payload;
import com.jaycode.framework.cloud.boot.core.lock.Lock;
import com.jaycode.framework.cloud.boot.core.lock.LockManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import javax.annotation.Resource;

/**
 *
 * 基于消息的分布式锁切面
 *  * 考虑到无论是什么消息中间件都存在重复发送消息的设计以降低消息丢失率，所以还是引入分布式锁来实现消息重复消费限制，并且业务实现的时候也要充分考虑幂等性
 * @author cheng.wang
 * @date 2022/5/20
 */
@Aspect
@ConditionalOnClass(ProceedingJoinPoint.class)
@Slf4j
public class AopDistributedLockAspect {

    @Resource
    private LockManager lockManager;

    /**
     * 仅拦截用spring service注解声明的类的@SchedulerEndpoint方法
     */
    @Pointcut("@annotation(com.jaycode.framework.cloud.boot.core.lock.DistributedLock)")
    public void annotationMethod() {
    }

    @Pointcut("@within(org.springframework.cloud.stream.annotation.EnableBinding)")
    public void annotationClass() {
    }

    @Around("annotationMethod() && annotationClass()")
    public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        if (args.length == 1 && args[0] instanceof Message){
            String lockKey = "jaycode:stream:" + ((Payload)args[0]).getId();
            Lock lock = lockManager.createLock(lockKey);
            if (lock.tryLock()){
                try {
                    return pjp.proceed();
                }finally {
                    lock.unLock();
                }
            }else {return null;}
        }
        return pjp.proceed();
    }
}
