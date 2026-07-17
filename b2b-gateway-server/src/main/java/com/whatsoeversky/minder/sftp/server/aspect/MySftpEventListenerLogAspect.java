package com.whatsoeversky.minder.sftp.server.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class MySftpEventListenerLogAspect {
    @Pointcut(value = "execution(* com.whatsoeversky.minder.server.listener.MySftpEventListener.*(..))")
    public void pointcut() {

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        if (log.isDebugEnabled()) {
            log.debug("listener execute, method name: {}", signature.getName());
        }
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing(JoinPoint joinPoint, Throwable throwable) {
        Signature signature = joinPoint.getSignature();
        log.error("listener throw, method name: {}", signature.getName(), throwable);
    }
}
