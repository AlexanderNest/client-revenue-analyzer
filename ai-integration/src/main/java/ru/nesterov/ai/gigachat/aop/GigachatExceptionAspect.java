package ru.nesterov.ai.gigachat.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.nesterov.ai.core.exception.AiException;
import ru.nesterov.ai.gigachat.exception.GigachatException;

@Aspect
@Component
@Slf4j
public class GigachatExceptionAspect {
    @Around("execution(* ru.nesterov.ai.core.api.AIIntegrationService.*(..))")
    public Object wrapGigachatExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (GigachatException gigachatException) {
            log.error("Внутреннее исключение в модуле gigachat", gigachatException);
            throw new AiException(gigachatException.getMessage(), gigachatException);
        } catch (Exception e) {
            log.error("Внезапное исключение в модуле gigachat", e);
            throw new AiException("Ошибка при подключении к ИИ", e);
        }
    }
}
