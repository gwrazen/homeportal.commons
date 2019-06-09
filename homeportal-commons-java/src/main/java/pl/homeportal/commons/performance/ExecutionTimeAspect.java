package pl.homeportal.commons.performance;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeAspect
{
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionTimeAspect.class.getSimpleName());

    @Around("@annotation(ExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable
    {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        float executionTime = (System.currentTimeMillis() - start) / 1000f;
        LOG.info(joinPoint.getSignature() + " executed in " + executionTime + "s");
        return proceed;
    }
}
