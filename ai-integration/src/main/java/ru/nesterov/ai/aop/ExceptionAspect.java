package ru.nesterov.ai.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.nesterov.ai.exception.AiException;

@Aspect
@Component
@Slf4j
public class ExceptionAspect {
    @Around("execution(* ru.nesterov.ai.AIIntegrationService.*(..))")
    public Object wrapGigachatExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (AiException aiException) {
            log.error("Внутреннее исключение в модуле gigachat", aiException);
            throw new AiException(aiException.getMessage(), aiException);
        } catch (Exception e) {
            log.error("Внезапное исключение в модуле gigachat", e);
            throw new AiException("Ошибка при подключении к ИИ", e);
        }
    }
}
