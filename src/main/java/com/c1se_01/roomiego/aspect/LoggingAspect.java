package com.c1se_01.roomiego.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/** LoggingAspect class for logging information, messages, and exceptions */
@Component
@Aspect
public class LoggingAspect {

    private final ObjectMapper objectMapper;
    private Logger logger;

    private void setLogger(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getName();
        this.logger = Logger.getLogger(className);
    }


    protected void logEntry(String methodName, Object[] args) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("Method: %s", methodName));
            Arrays.stream(args).forEach(arg -> logger.fine(String.format("Method arguments: %s", arg)));
        }
    }

    private LoggingAspect() {
        // Initialize ObjectMapper with pretty printing and JavaTimeModule for date/time serialization
        this.objectMapper =
            new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT) // Enable pretty printing
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS) // Do not fail on empty beans
                .registerModule(
                    new JavaTimeModule()) // Register JavaTimeModule for date/time serialization
                .disable(
                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Use ISO-8601 format for dates
    }

    /** Pointcut for all methods in classes ending with ServiceImpl */
    @Pointcut("execution(* com.c1se_01.roomiego.service.impl..*ServiceImpl.*(..))")
    public void serviceImplPointcut() {}

    @Pointcut("execution(* com.c1se_01.roomiego.service.impl.AuthenticationService")
    public void authServicePointcut() {}

    @Pointcut("execution(* com.c1se_01.roomiego.service.impl.OurUserDetailService")
    public void ourUserDetailServicePointcut() {}

    /** Combined pointcut for all logging scenarios */
    @Pointcut("serviceImplPointcut() || authServicePointcut() || ourUserDetailServicePointcut()")
    public void loggingPointcut() {}

    /**
     * Around advice to log method entry and exit
     *
     * @param joinPoint the join point
     * @return the result of the method execution
     * @throws Throwable if an error occurs during method execution
     */
    @Around("loggingPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        setLogger(joinPoint);
        Object[] args = joinPoint.getArgs();
        // Log method entry
        logEntry(methodName, args);
        // Execute the method
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();

        if (logger.isLoggable(Level.INFO)) {
            // Pretty print the result as JSON
            try {
                String jsonResult = objectMapper.writeValueAsString(result);
                logger.info("Result: \n" + jsonResult);
            } catch (Exception e) {
                // Fallback to toString() if JSON serialization fails
                logger.info("Result: " + result);
                logger.warning("Failed to serialize result to JSON: " + e.getMessage());
            }

            logger.info("Execution time: " + (endTime - startTime) + " ms");
        }

        return result;
    }

    /**
     * After throwing advice to log exceptions
     *
     * @param joinPoint the join point
     * @param throwable the throwable
     */
    @AfterThrowing(pointcut = "loggingPointcut()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable throwable) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        setLogger(joinPoint);
        if (logger.isLoggable(Level.SEVERE)) {
            logger.severe(
                String.format(
                    "Method: %s, Message: %s", methodName, throwable.getMessage()));
            // Log the stack trace of the exception
            try {
                // format the stack trace
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                logger.severe("Exception stack trace:\n" + sw);
            } catch (Exception e) {
                logger.warning("Failed to print stack trace: " + e.getMessage());
            }
        }
    }

    /**
     * Before advice to log method arguments for all controller methods
     *
     * @param joinPoint the join point listening to all methods in controller classes
     */
    @Before("execution(* com.c1se_01.roomiego.controller.*Controller.*(..))")
    public void loggingControllerArgs(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        setLogger(joinPoint);
        Object[] args = joinPoint.getArgs();
        logEntry(methodName, args);
    }
}