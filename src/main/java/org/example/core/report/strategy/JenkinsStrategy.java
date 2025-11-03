package org.example.core.report.strategy;

import lombok.extern.slf4j.Slf4j;
import org.example.core.driver.DriverManager;
import org.example.enums.BrowserType;
import org.example.configure.Config;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.io.File;
import java.nio.file.Files;

@Slf4j
public class JenkinsStrategy implements ReportStrategy {
    
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
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
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

        // existing behavior (if any) - currently no default screenshot here
        try { org.example.core.report.FailureTracker.clearForCurrentThread(); } catch (Throwable ignored) {}
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
    }
    
    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
    }
    
    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
    }
    
    @Override
    public void onConfigurationSuccess(ITestResult itr) {
    }
    
    @Override
    public void onConfigurationFailure(ITestResult itr) {
    }
    
    @Override
    public void onConfigurationSkip(ITestResult itr) {
    }
    
    @Override
    public void failStep(String name) {
        try {
            // try to capture screenshot and save under target/jenkins-screenshots
            BrowserType browserType = Config.getBrowserType();
            WebDriver driver = null;
            try {
                driver = DriverManager.getInstance(browserType).getDriver();
            } catch (Throwable ignored) {}
            if (driver != null && driver instanceof TakesScreenshot) {
                byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                if (bytes != null && bytes.length > 0) {
                    File out = new File("target/jenkins-screenshots");
                    if (!out.exists()) out.mkdirs();
                    String fileName = "jenkins_fail_" + System.currentTimeMillis() + ".png";
                    File file = new File(out, fileName);
                    Files.write(file.toPath(), bytes);
                    log.error("STEP FAILED: {}  (screenshot saved: {})", name, file.getAbsolutePath());
                    return;
                }
            }
            log.error("STEP FAILED: {}  (no screenshot available)", name);
        } catch (Throwable t) {
            log.debug("JenkinsStrategy.failStep failed: {}", t.getMessage());
        }
    }
}
