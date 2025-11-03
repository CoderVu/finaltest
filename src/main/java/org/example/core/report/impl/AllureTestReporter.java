package org.example.core.report.impl;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.example.configure.Config;
import org.example.core.driver.DriverManager;
import org.example.core.report.ITestReporter;
import org.example.enums.BrowserType;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.io.ByteArrayInputStream;

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
        String errorMsg = "FAIL: " + message;
        if (error != null) errorMsg += " - " + error.getMessage();
        Allure.step(errorMsg, Status.FAILED);
    }
    @Override
    public void attachScreenshot(String name) {
        try {
            BrowserType browserType = Config.getBrowserType();
            WebDriver driver = DriverManager.getInstance(browserType).getDriver();
            if (driver != null) {
                byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                if (bytes != null && bytes.length > 0) {
                    Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), "png");
                }
            }
        } catch (Throwable t) {
            log.warn("Failed to attach screenshot: {}", t.getMessage());
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
