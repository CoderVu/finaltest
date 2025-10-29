package org.example.core.report.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.core.report.ReporterManager;
import org.example.core.report.TestReporter;
import org.example.core.report.annotations.Step;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspect to intercept methods annotated with @Step and log them via active reporter.
 * Wraps method execution in a step to enable nested step structure in reports.
 */
@Aspect
@Slf4j
public class StepAspect {

    @Around("@annotation(org.example.core.report.annotations.Step)")
    public Object aroundStep(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Method method = getMethod(joinPoint);
            Step step = method.getAnnotation(Step.class);
            String message = buildMessage(step, method, joinPoint.getArgs());
            TestReporter reporter = ReporterManager.get();

            // Reporter-agnostic: delegate to reporter's withinStep wrappers
            if (method.getReturnType() == void.class || method.getReturnType() == Void.class) {
                reporter.withinStep(message, () -> {
                    try {
                        joinPoint.proceed();
                    } catch (Throwable throwable) {
                        rethrowUnchecked(throwable);
                    }
                });
                return null;
            } else {
                return reporter.withinStep(message, () -> {
                    try {
                        return joinPoint.proceed();
                    } catch (Throwable throwable) {
                        rethrowUnchecked(throwable);
                        return null; // unreachable
                    }
                });
            }
        } catch (Throwable t) {
            log.debug("@Step execution failed: {}", t.getMessage());
            throw t;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Throwable> void rethrowUnchecked(Throwable throwable) throws T {
        throw (T) throwable;
    }

    // ChildStep support removed; nested logs are explicit via reporter.logStep(...)

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }

    private String buildMessage(Step step, Method method, Object[] args) {
        String template = step != null ? step.value() : "";
        if (template == null || template.trim().isEmpty()) {
            return method.getName() + formatArgs(args);
        }
        String message = template;
        for (int i = 0; i < args.length; i++) {
            String placeholder = "{arg" + i + "}";
            message = message.replace(placeholder, String.valueOf(args[i]));
        }
        return message;
    }

    // buildMessageForChild removed

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        try {
            return Arrays.toString(args);
        } catch (Throwable ignored) {
            return "";
        }
    }
}


