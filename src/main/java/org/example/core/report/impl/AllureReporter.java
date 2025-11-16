package org.example.core.report.impl;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.example.core.report.AbstractReporter;
import org.example.enums.ReportType;
import java.io.ByteArrayInputStream;

/**
 * Implementation của IReporter cho Allure reporting.
 * Tương tự Chrome extends AbstractDriverManager trong package driver.
 */
@Slf4j
public class AllureReporter extends AbstractReporter {

    public AllureReporter() {
        super(ReportType.ALLURE);
    }

    @Override
    public void logStep(String message) {
        Allure.step(message);
    }

    @Override
    public void info(String message) {
        Allure.step("INFO: " + message);
    }

    @Override
    public void logFail(String message, Throwable error) {
        Allure.step(message, Status.FAILED);
    }

    @Override
    public void attachScreenshot(String name) {
        byte[] bytes = getScreenshotBytes();
        if (bytes != null && bytes.length > 0) {
            try {
                Allure.addAttachment(name != null ? name : "screenshot", "image/png", new ByteArrayInputStream(bytes), "png");
            } catch (Throwable t) {
                log.warn("Allure attachScreenshot failed: {}", t.getMessage());
            }
        }
    }

    @Override
    public void childStep(String name, Runnable runnable) {
        Allure.step(name, () -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                attachScreenshot("step_fail_" + name);
                throw e;
            }
        });
    }

    @Override
    public <T> T childStep(String name, java.util.function.Supplier<T> supplier) {
        final Object[] holder = new Object[1];
        Allure.step(name, () -> {
            try {
                holder[0] = supplier.get();
            } catch (Throwable e) {
                attachScreenshot("step_fail_" + name);
                throw e;
            }
        });
        @SuppressWarnings("unchecked")
        T result = (T) holder[0];
        return result;
    }
}

