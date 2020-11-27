package cn.lw.fz.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LogAspect {

    @Pointcut(value = "@within(cn.lw.fz.aop.AutoClassLog)")
    public void ctl() {}

    @Before(value = "ctl()")
    public void logBefore(JoinPoint jp) {
        log.info(jp.toString());
    }
}