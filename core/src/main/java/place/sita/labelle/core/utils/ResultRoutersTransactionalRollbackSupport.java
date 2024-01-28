package place.sita.labelle.core.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Aspect
@Component
public class ResultRoutersTransactionalRollbackSupport {

    @Around("@annotation(transactional)")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        Object result = null;
        result = joinPoint.proceed();
        // After method execution code

        if (result instanceof Successable successable) {
            if (!successable.isSuccess()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }
        return result;
    }
}
