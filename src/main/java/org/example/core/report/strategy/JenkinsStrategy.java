package org.example.core.report.strategy;

import lombok.extern.slf4j.Slf4j;
import org.example.core.driver.AbstractDriverManager;
import org.example.core.driver.DriverFactory;
import org.example.enums.BrowserType;
import org.example.configure.Config;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.example.core.control.util.DriverUtils.getDriver;

@Slf4j
public class JenkinsStrategy implements ReportStrategy {

    // buffer steps per thread/test
    private static final ThreadLocal<List<String>> STEP_BUFFER = ThreadLocal.withInitial(ArrayList::new);

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS");

    @Override
    public void onStart(ITestContext context) {
        log.info("Jenkins report: rely on Surefire/JUnit XML at target/surefire-reports/");
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("Jenkins report: test suite finished. Reports available at target/surefire-reports/");
    }

    @Override
    public void onTestStart(ITestResult result) {
        // initialize step buffer for this test
        List<String> buf = STEP_BUFFER.get();
        buf.clear();
        String header = String.format("TEST START: %s (%s)", result.getMethod().getMethodName(),
                LocalDateTime.now().format(TS_FMT));
        buf.add(header);
        log.info(header);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String msg = String.format("TEST PASS: %s", result.getMethod().getMethodName());
        log.info(msg);
        STEP_BUFFER.get().add(msg);
        writeStepLog(result);
        STEP_BUFFER.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            if (org.example.core.report.FailureTracker.isHandledForCurrentThread()
                    || Boolean.TRUE.equals(result.getAttribute("soft.assert.handled"))) {
                log.debug("Skipping Jenkins onTestFailure (already handled by soft-assert step): {}", result.getName());
                org.example.core.report.FailureTracker.clearForCurrentThread();
                return;
            }
        } catch (Throwable ignored) {}

        String failMsg = String.format("TEST FAIL: %s - %s", result.getMethod().getMethodName(), getErrorMessage(result));
        log.error(failMsg);
        STEP_BUFFER.get().add(failMsg);

        writeStepLog(result);
        try { org.example.core.report.FailureTracker.clearForCurrentThread(); } catch (Throwable ignored) {}
        STEP_BUFFER.remove();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String msg = String.format("TEST SKIPPED: %s", result.getMethod().getMethodName());
        log.info(msg);
        STEP_BUFFER.get().add(msg);
        writeStepLog(result);
        STEP_BUFFER.remove();
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) { }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) { onTestFailure(result); }

    @Override
    public void onConfigurationSuccess(ITestResult itr) { }

    @Override
    public void onConfigurationFailure(ITestResult itr) { }

    @Override
    public void onConfigurationSkip(ITestResult itr) { }

    /**
     * Called by framework when a step fails (ReportStrategy.failStep)
     * Will record failed step and attempt to capture a screenshot immediately.
     */
    @Override
    public void failStep(String name) {
        try {
            String entry = "STEP FAIL: " + name;
            log.error(entry);
            STEP_BUFFER.get().add(entry);
        } catch (Throwable t) {
            log.debug("JenkinsStrategy.failStep failed: {}", t.getMessage());
        }
    }

    /**
     * Optional: allow other parts of code to log normal steps to JenkinsStrategy buffer.
     * Call JenkinsStrategy.logStep("...") from JenkinsTestReporter or similar if desired.
     */
    public void logStep(String name) {
        try {
            String entry = "STEP: " + name;
            log.info("STEP: {}", name);
            STEP_BUFFER.get().add(entry);
        } catch (Throwable ignored) {}
    }

    // helper to write buffered steps to a file under target/jenkins-steps/
    private void writeStepLog(ITestResult result) {
        try {
            List<String> buf = STEP_BUFFER.get();
            if (buf == null || buf.isEmpty()) return;

            String testName = (result != null && result.getMethod() != null)
                    ? result.getMethod().getMethodName()
                    : "unknown_test";
            String ts = LocalDateTime.now().format(TS_FMT);
            Path dir = new File("target/jenkins-steps").toPath();
            if (!Files.exists(dir)) Files.createDirectories(dir);

            Path file = dir.resolve(testName + "_" + ts + ".log");
            Files.write(file, buf, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            log.info("Wrote Jenkins step log: {}", file.toAbsolutePath());
        } catch (IOException e) {
            log.warn("Failed to write Jenkins step log: {}", e.getMessage(), e);
        } catch (Throwable t) {
            log.debug("Unexpected error writing Jenkins step log: {}", t.getMessage());
        }
    }

    private String getErrorMessage(ITestResult result) {
        if (result == null) return "No result info";
        Throwable t = result.getThrowable();
        if (t == null) return "No throwable available";
        String msg = t.getMessage();
        if (msg != null && !msg.trim().isEmpty()) return msg;
        return t.getClass().getName();
    }
}
