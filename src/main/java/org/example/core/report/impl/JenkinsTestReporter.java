package org.example.core.report.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.core.report.ITestReporter;
import org.testng.Reporter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.util.Base64;

import static org.example.core.control.util.DriverUtils.getDriver;

@Slf4j
public class JenkinsTestReporter implements ITestReporter {
    @Override
    public void logStep(String message) {
        String stepMessage = "STEP: " + message;
        log.info(stepMessage);
        Reporter.log(stepMessage);
    }
    @Override
    public void info(String message) {
        log.info(message);
        Reporter.log(message);
    }
    @Override
    public void logFail(String message, Throwable error) {
        String errorMsg = "FAIL: " + message;
        if (error != null) errorMsg += " - " + error.getMessage();
        log.error(errorMsg);
        Reporter.log(errorMsg);
        if (error != null) {
            Reporter.log("Exception class: " + error.getClass().getName());
            Reporter.log("Stack trace: " + getStackTrace(error));
        }
    }
    @Override
    public void attachScreenshot(String name) {
        try {
            WebDriver driver = getDriver();
            if (driver != null && (driver instanceof TakesScreenshot)) {
                byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                if (bytes != null && bytes.length > 0) {
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    Reporter.log("Screenshot: <br/><img src=\"data:image/png;base64," + base64 + "\" alt=\"" + (name != null ? name : "screenshot") + "\"/>");
                    log.info("Jenkins attached screenshot: {}", name);
                    return;
                }
            }
            log.debug("Jenkins: no driver or cannot take screenshot for {}", name);
        } catch (Throwable t) {
            log.warn("Jenkins attachScreenshot failed: {}", t.getMessage());
        }
    }
    
    @Override
    public void childStep(String name, Runnable runnable) {
        log.info("STEP: {}", name);
        Reporter.log("STEP: " + name);
        try {
            runnable.run();
            log.debug("STEP completed: {}", name);
        } catch (Throwable e) {
            log.error("STEP failed: {} - {}", name, e.getMessage());
            Reporter.log("STEP FAILED: " + name + " - " + e.getMessage());
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public <T> T childStep(String name, java.util.function.Supplier<T> supplier) {
        log.info("STEP: {}", name);
        Reporter.log("STEP: " + name);
        try {
            T result = supplier.get();
            log.debug("STEP completed: {}", name);
            return result;
        } catch (Throwable e) {
            log.error("STEP failed: {} - {}", name, e.getMessage());
            Reporter.log("STEP FAILED: " + name + " - " + e.getMessage());
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }
    
    private String getStackTrace(Throwable error) {
        if (error == null) return "";
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] elements = error.getStackTrace();
        int maxFrames = Math.min(5, elements.length);
        for (int i = 0; i < maxFrames; i++) sb.append(elements[i].toString()).append("\n");
        if (elements.length > maxFrames) sb.append("... " + elements.length + " more frames ...");
        return sb.toString();
    }
}
