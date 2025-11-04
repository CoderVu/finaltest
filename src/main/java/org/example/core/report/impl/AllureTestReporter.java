package org.example.core.report.impl;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.example.core.report.ITestReporter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;

import static org.example.core.control.util.DriverUtils.getDriver;

@Slf4j
public class AllureTestReporter implements ITestReporter {
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
        try {
            WebDriver driver = getDriver();
            if (driver != null && (driver instanceof TakesScreenshot)) {
                byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                if (bytes != null && bytes.length > 0) {
                    Allure.addAttachment(name != null ? name : "screenshot", "image/png", new ByteArrayInputStream(bytes), "png");
                    log.debug("Allure attached screenshot: {}", name);
                    return;
                }
            }
            log.debug("Allure: no driver or cannot take screenshot for {}", name);
        } catch (Throwable t) {
            log.warn("Allure attachScreenshot failed: {}", t.getMessage());
        }
    }
    @Override
    public void childStep(String name, Runnable runnable) {
        try {
            log.debug("AllureTestReporter.childStep called: {}", name);
            Allure.step(name, runnable::run);
            log.debug("AllureTestReporter.childStep completed: {}", name);
        } catch (Exception e) {
            log.warn("Failed to create Allure step '{}': {}", name, e.getMessage(), e);
            // Fallback: execute without Allure step if Allure lifecycle is not ready
            runnable.run();
        }
    }

    @Override
    public <T> T childStep(String name, java.util.function.Supplier<T> supplier) {
        try {
            log.debug("AllureTestReporter.childStep (Supplier) called: {}", name);
            final Object[] holder = new Object[1];
            Allure.step(name, () -> holder[0] = supplier.get());
            @SuppressWarnings("unchecked")
            T result = (T) holder[0];
            log.debug("AllureTestReporter.childStep (Supplier) completed: {}", name);
            return result;
        } catch (Exception e) {
            log.warn("Failed to create Allure step '{}': {}", name, e.getMessage(), e);
            // Fallback: execute without Allure step if Allure lifecycle is not ready
            return supplier.get();
        }
    }
}
