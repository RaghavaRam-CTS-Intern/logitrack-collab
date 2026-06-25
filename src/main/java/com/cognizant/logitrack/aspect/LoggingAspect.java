package com.cognizant.logitrack.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Cross-cutting logging concern handled with Spring AOP.
 * Logs method entry, execution time, and exceptions for the controller
 * and service-implementation layers without touching the business code.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /** All methods in any @RestController under the controller package. */
    @Pointcut("within(com.cognizant.logitrack.controller..*)")
    public void controllerLayer() {
    }

    /** All methods in the service implementation package. */
    @Pointcut("within(com.cognizant.logitrack.serviceImplementation..*)")
    public void serviceLayer() {
    }

    /**
     * Wraps controller and service methods: logs the call with arguments,
     * measures execution time, and logs the outcome.
     */
    @Around("controllerLayer() || serviceLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().getDeclaringType().getSimpleName()
                + "." + joinPoint.getSignature().getName() + "()";

        if (log.isDebugEnabled()) {
            log.debug("--> Entering {} with args {}", signature, Arrays.toString(joinPoint.getArgs()));
        }

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("<-- {} executed in {} ms", signature, elapsed);
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("<-- {} failed after {} ms: {}", signature, elapsed, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Dedicated advice that fires whenever a controller or service method
     * throws, capturing the exception type for diagnostics.
     */
    @AfterThrowing(pointcut = "controllerLayer() || serviceLayer()", throwing = "ex")
    public void logException(org.aspectj.lang.JoinPoint joinPoint, Throwable ex) {
        log.warn("Exception in {}.{}: {} - {}",
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(),
                ex.getClass().getSimpleName(),
                ex.getMessage());
    }
}
