package org.example.project.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

@Aspect
@Component
@Slf4j
public class RequestLoggingAspect {

    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    public void logControllerRequest(JoinPoint joinPoint) {
        if (joinPoint.getSignature().getDeclaringType().isAnnotationPresent(RestController.class)) {
            log.info("[REQUEST] {}.{}()",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
        }
    }

    @AfterThrowing(pointcut = "within(@org.springframework.web.bind.annotation.RestController *)",
            throwing = "ex")
    public void logControllerException(JoinPoint joinPoint, Throwable ex) {
        log.error("[EXCEPTION] {}.{}() - {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                ex.getMessage());
    }
}
