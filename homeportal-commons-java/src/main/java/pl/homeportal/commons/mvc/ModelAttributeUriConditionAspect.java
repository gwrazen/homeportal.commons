package pl.homeportal.commons.mvc;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static pl.homeportal.commons.mvc.ControllerUtils.currentUri;
import static pl.homeportal.commons.mvc.ControllerUtils.isResourceUriSuffix;

@Aspect
@Component
public class ModelAttributeUriConditionAspect
{
    @Around("@annotation(pl.homeportal.commons.mvc.ModelAttributeUriCondition)")
    public Object decideExecution(ProceedingJoinPoint joinPoint) throws Throwable
    {
        MethodSignature signature = MethodSignature.class.cast(joinPoint.getSignature());
        ModelAttributeUriCondition annotation = signature.getMethod().getAnnotation(ModelAttributeUriCondition.class);
        final String uri = currentUri();

        if (isResourceUriSuffix())
        {
            return null;
        }

        if (isBlank(annotation.prefixUri()) && isEmpty(annotation.allowedUris()))
        {
            return joinPoint.proceed();
        }

        if (isNotBlank(annotation.prefixUri()) && uri.startsWith(annotation.prefixUri()))
        {
            return joinPoint.proceed();
        }

        for (String allowedUri : annotation.allowedUris())
        {
            if (uri.equals(allowedUri))
            {
                return joinPoint.proceed();
            }
        }
        return null;
    }
}
