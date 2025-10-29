package org.example.report.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.report.TestReporter;
import org.testng.Reporter;

/**
 * Jenkins Reporter - Implementation for Jenkins/TestNG reporting.
 * Provides both standard TestReporter methods and Jenkins-specific features.
 * 
 * Features:
 * - Console logging for local debugging
 * - TestNG Reporter logs for Jenkins
 * - Simple, lightweight output
 * - Advanced Jenkins-specific methods
 */
@Slf4j
public class JenkinsReporter implements TestReporter {

    // ==================== TestReporter Interface Methods ====================

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
    public void logInfo(String message) {
        info(message);
    }

    @Override
    public void logFail(String message, Throwable error) {
        String errorMsg = "FAIL: " + message;
        if (error != null) {
            errorMsg += " - " + error.getMessage();
        }
        log.error(errorMsg);
        Reporter.log(errorMsg);
        
        // Add full stack trace for Jenkins debugging
        if (error != null) {
            Reporter.log("Exception class: " + error.getClass().getName());
            Reporter.log("Stack trace: " + getStackTrace(error));
        }
    }

    @Override
    public void attachScreenshot(String name) {
        log.info("Screenshot: {}", name);
        Reporter.log("Screenshot: " + name);
    }

    // ==================== Advanced Jenkins-Specific Methods ====================

    /**
     * Log a passed step
     */
    public void logPass(String message) {
        String passMessage = "PASS: " + message;
        log.info(passMessage);
        Reporter.log(passMessage);
    }

    /**
     * Log a warning
     */
    public void logWarn(String message) {
        String warnMessage = "WARN: " + message;
        log.warn(warnMessage);
        Reporter.log(warnMessage);
    }

    /**
     * Log with metadata for Jenkins
     */
    public void logWithMetadata(String message, String metadata) {
        String fullMessage = message + " [Metadata: " + metadata + "]";
        log.info(fullMessage);
        Reporter.log(fullMessage);
    }

    /**
     * Get formatted stack trace
     */
    private String getStackTrace(Throwable error) {
        if (error == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] elements = error.getStackTrace();
        
        // Limit to first 5 frames to avoid huge logs
        int maxFrames = Math.min(5, elements.length);
        for (int i = 0; i < maxFrames; i++) {
            sb.append(elements[i].toString()).append("\n");
        }
        
        if (elements.length > maxFrames) {
            sb.append("... " + elements.length + " more frames ...");
        }
        
        return sb.toString();
    }
}