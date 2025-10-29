package org.example.core.report.impl;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.example.core.report.TestReporter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;

/**
 * Allure Reporter - Implementation for Allure reporting.
 * Provides both standard TestReporter methods and Allure-specific features.
 * 
 * Features:
 * - Supports nested steps
 * - Rich attachments (screenshots, files, etc.)
 * - Beautiful HTML reports
 * - Advanced Allure-specific methods
 */
@Slf4j
public class AllureReporter implements TestReporter {

    // ==================== TestReporter Interface Methods ====================

    @Override
    public void logStep(String message) {
        Allure.step(message);
    }

    @Override
    public void info(String message) {
        Allure.step("INFO: " + message);
    }

    @Override
    public void logInfo(String message) {
        info(message);
    }

    @Override
    public void logFail(String message, Throwable error) {
        String errorMsg = "FAIL: " + message;
        if (error != null) {
            errorMsg += " - " + error.getMessage();
        }
        Allure.step(errorMsg, Status.FAILED);
    }

    @Override
    public void attachScreenshot(String name) {
        try {
            WebDriver driver = WebDriverRunner.getWebDriver();
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


    // ==================== Advanced Allure-Specific Methods ====================

    /**
     * Log a passed step explicitly
     */
    public void logPass(String message) {
        Allure.step(message, Status.PASSED);
    }

    /**
     * Log a warning step
     */
    public void logWarn(String message) {
        Allure.step("WARN: " + message, Status.BROKEN);
    }

    /**
     * Add a link to the report
     * Note: Use Allure.link() or Allure.addLinks() in your tests directly
     */
    public void addLink(String name, String url) {
        // Allure 2.x doesn't have addLink in core, use annotations instead
        log.info("Adding link: {} -> {}", name, url);
    }

    /**
     * Add description text to the test
     */
    public void addDescription(String description) {
        Allure.getLifecycle().updateTestCase(update -> update.setDescription(description));
    }

    // ==================== Step wrapper implementations ====================

    @Override
    public void withinStep(String name, Runnable runnable) {
        Allure.step(name, () -> runnable.run());
    }

    @Override
    public <T> T withinStep(String name, java.util.function.Supplier<T> supplier) {
        final Object[] holder = new Object[1];
        Allure.step(name, () -> {
            holder[0] = supplier.get();
        });
        @SuppressWarnings("unchecked")
        T result = (T) holder[0];
        return result;
    }
}