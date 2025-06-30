package ru.nesterov.calendar.integration.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import ru.nesterov.calendar.integration.exception.CalendarIntegrationException;

@Aspect
@Component
@Slf4j
public class CalendarIntegrationExceptionAop {
    @Around("execution(* ru.nesterov.calendar.integration.service.CalendarService.*(..))")
    public Object wrapCalendarExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (CalendarIntegrationException coreException) {
            log.error("Внутреннее исключение в модуле calendar", coreException);
            throw coreException;
        } catch (Exception e) {
            log.error("Внезапное исключение в модуле calendar", e);
            throw new CalendarIntegrationException("Ошибка при обращении к календарю", e);
        }
    }
}