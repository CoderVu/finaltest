package org.example.core.report.strategy;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.MediaEntityBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.core.driver.AbstractDriverManager;
import org.example.core.driver.DriverFactory;
import org.example.core.report.impl.ExtentTestReporter;
import org.example.enums.BrowserType;
import org.example.configure.Config;
import org.openqa.selenium.*;
import org.openqa.selenium.io.FileHandler;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.example.core.report.ReportManager;
import org.example.core.report.ITestReporter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.core.control.util.DriverUtils.getDriver;

@Slf4j
public class ExtentStrategy implements ReportStrategy {
    private static final ExtentReports EXTENT = new ExtentReports();
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private static final Map<String, ExtentTest> NAME_TO_TEST = new ConcurrentHashMap<>();

    private static volatile boolean INITIALIZED = false;
    private static volatile String REPORT_DIR = null;

    public static String getReportDir() {
        return REPORT_DIR;
    }

    @Override
    public void onStart(ITestContext context) {
        if (INITIALIZED) return;
        synchronized (ExtentStrategy.class) {
            if (INITIALIZED) return;

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            REPORT_DIR = "target/extent-report/" + timestamp;
            File reportDir = new File(REPORT_DIR);
            if (!reportDir.exists()) {
                if (!reportDir.mkdirs()) {
                    log.warn("Could not create report directory: {}", reportDir.getAbsolutePath());
                }
            }

            // Use timestamp in the HTML report filename so each run has a unique report file
            File htmlReport = new File(REPORT_DIR, "index_" + timestamp + ".html");
            ExtentSparkReporter spark = new ExtentSparkReporter(htmlReport.getAbsolutePath());

            try {
                String suiteName = (context != null && context.getSuite() != null)
                        ? context.getSuite().getName()
                        : "Automation Test Suite";

                spark.config().setDocumentTitle(suiteName);
                spark.config().setReportName(suiteName);
                spark.config().setEncoding("utf-8");

                EXTENT.attachReporter(spark);
                EXTENT.setSystemInfo("Suite", suiteName);
                EXTENT.setSystemInfo("Environment", Config.getEnvFile());
                EXTENT.setSystemInfo("Browser", Config.getBrowserType().name());
                log.info("ExtentReports initialized at {}", htmlReport.getAbsolutePath());
            } catch (Throwable t) {
                log.warn("Failed to initialize ExtentSparkReporter: {}", t.getMessage(), t);
            } finally {
                INITIALIZED = true;
            }
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        try {
            EXTENT.flush();
            log.info("Extent report generated at {}", REPORT_DIR);
        } catch (Throwable t) {
            log.warn("Extent flush failed: {}", t.getMessage());
        }
        ExtentTestReporter.clearTest();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        ExtentTest test = EXTENT.createTest(testName);
        NAME_TO_TEST.put(testName, test);
        CURRENT_TEST.set(test);
        ExtentTestReporter.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        getTest(result).ifPresent(t -> t.log(Status.PASS, "Test Passed"));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (isSoftAssertHandled(result)) return;

        Throwable error = result.getThrowable();
        String message = (error == null) ? "Test Failed" : error.getMessage();

        // Delegate logging + screenshot capture to reporter implementations
        ITestReporter reporter = ReportManager.getReporter();
        try {
            reporter.logFail(message, error);
            reporter.attachScreenshot("failure_" + System.currentTimeMillis() + ".png");
        } catch (Throwable t) {
            log.warn("Reporter handling of failure failed: {}", t.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ITestReporter reporter = ReportManager.getReporter();
        try {
            reporter.logStep("Skipped: " + result.getMethod().getMethodName());
            reporter.attachScreenshot("skipped_" + System.currentTimeMillis() + ".png");
        } catch (Throwable t) {
            log.warn("Reporter handling of skipped failed: {}", t.getMessage());
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Log for diagnostic purposes (avoids being identical to the super implementation)
        try {
            log.debug("onTestFailedButWithinSuccessPercentage called for {}", result.getMethod().getMethodName());
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) { onTestFailure(result); }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
        ExtentTest test = getLastTestFromContext(itr);
        if (test != null) {
            CURRENT_TEST.set(test);
            ExtentTestReporter.setTest(test);
            log.debug("Restored context for @Configuration method: {}", itr.getMethod().getMethodName());
        }
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
        Throwable t = itr.getThrowable();
        String message = (t == null) ? "Config failed" : "Config failed: " + t.getMessage();
        ITestReporter reporter = ReportManager.getReporter();
        try {
            reporter.logFail(message, t);
            reporter.attachScreenshot("config_failure_" + System.currentTimeMillis() + ".png");
        } catch (Throwable tt) {
            log.warn("Reporter handling of configuration failure failed: {}", tt.getMessage());
        }
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
        try {
            log.debug("onConfigurationSkip called for {}", itr.getMethod().getMethodName());
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void failStep(String stepName) {
        try {
            ITestReporter reporter = ReportManager.getReporter();
            reporter.logFail(stepName, null);
            reporter.attachScreenshot("failstep_" + System.currentTimeMillis() + ".png");
        } catch (Throwable t) {
            log.warn("failStep failed: {}", t.getMessage());
        }
    }

    private Optional<ExtentTest> getTest(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) return Optional.of(test);
        return Optional.ofNullable(NAME_TO_TEST.get(result.getMethod().getMethodName()));
    }

    private ExtentTest getLastTestFromContext(ITestResult itr) {
        try {
            var ctx = itr.getTestContext();
            List<ITestResult> all = new ArrayList<>();
            all.addAll(ctx.getPassedTests().getAllResults());
            all.addAll(ctx.getFailedTests().getAllResults());
            all.addAll(ctx.getSkippedTests().getAllResults());
            if (all.isEmpty()) return null;

            all.sort(Comparator.comparingLong(ITestResult::getEndMillis).reversed());
            String lastTestName = all.get(0).getMethod().getMethodName();
            return NAME_TO_TEST.get(lastTestName);
        } catch (Throwable t) {
            return null;
        }
    }

    private boolean isSoftAssertHandled(ITestResult result) {
        try {
            if (org.example.core.report.FailureTracker.isHandledForCurrentThread()
                    || Boolean.TRUE.equals(result.getAttribute("soft.assert.handled"))) {
                org.example.core.report.FailureTracker.clearForCurrentThread();
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
