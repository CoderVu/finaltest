package org.example.core.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.core.annotations.Step;
import org.example.core.report.IReporter;
import org.example.core.report.ReportManager;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Slf4j
public class StepAspect {

    private static final StepAspect INSTANCE = new StepAspect();

    public static StepAspect aspectOf() {
        return INSTANCE;
    }

    @Around("@annotation(org.example.core.annotations.Step)")
    public Object aroundStep(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = getMethod(joinPoint);
        Step step = method.getAnnotation(Step.class);
        String message = buildMessage(step, method, joinPoint.getArgs());

        IReporter reporter = ReportManager.getReporter();
        if (reporter == null) {
            return joinPoint.proceed();
        }

        if (method.getReturnType() == void.class || method.getReturnType() == Void.class) {
            reporter.childStep(message, () -> {
                try {
                    joinPoint.proceed();
                } catch (Throwable throwable) {
                    rethrowUnchecked(throwable);
                }
            });
            return null;
        } else {
            return reporter.childStep(message, () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    rethrowUnchecked(throwable);
                    return null;
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Throwable> void rethrowUnchecked(Throwable throwable) throws T {
        throw (T) throwable;
    }

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

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        try {
            return Arrays.toString(args);
        } catch (Throwable ignored) {
            return "";
        }
    }
}