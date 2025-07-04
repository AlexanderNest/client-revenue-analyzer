package ru.nesterov.core.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.nesterov.ai.exception.AiException;
import ru.nesterov.core.exception.CoreException;

@Aspect
@Component
@Slf4j
public class CoreExceptionAspect {
    @Around("execution(* ru.nesterov.core.service..*.*(..))")
    public Object wrapCoreExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (CoreException coreException) {
            log.error("Внутреннее исключение в модуле core", coreException);
            throw coreException;
        } catch (Exception e) {
            log.error("Внезапное исключение в модуле core", e);
            throw new AiException("Ошибка при обработке данных", e);
        }
    }
}
