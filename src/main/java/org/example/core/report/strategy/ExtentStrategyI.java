package org.example.core.report.strategy;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import lombok.extern.slf4j.Slf4j;
import org.example.core.report.impl.ExtentReporter;
import org.example.configure.Config;
import org.example.core.report.listener.IReportStrategyListener;
import org.example.utils.EnvUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.example.core.report.ReportManager;
import org.example.core.report.IReporter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ExtentStrategyI implements IReportStrategyListener {
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
        synchronized (ExtentStrategyI.class) {
            if (INITIALIZED) return;

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            REPORT_DIR = "target/extent-report/" + timestamp;
            File reportDir = new File(REPORT_DIR);
            if (!reportDir.exists()) {
                if (!reportDir.mkdirs()) {
                    log.warn("Could not create report directory: {}", reportDir.getAbsolutePath());
                }
            }

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
                EXTENT.setSystemInfo("Browser", EnvUtils.getBrowsers().toString());
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
        ExtentReporter.clearTest();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        ExtentTest test = EXTENT.createTest(testName);
        NAME_TO_TEST.put(testName, test);
        CURRENT_TEST.set(test);
        ExtentReporter.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        getTest(result).ifPresent(t -> t.log(Status.PASS, "Test Passed"));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (isSoftAssertHandled(result)) return;

        Throwable error = result.getThrowable();
        String message = getShortErrorMessage(error);

        IReporter reporter = ReportManager.getReporter();
        try {
            reporter.logFail(message, null);
        } catch (Throwable t) {
            log.warn("Reporter handling of failure failed: {}", t.getMessage());
        }
    }

    private String getShortErrorMessage(Throwable error) {
        if (error == null) return "Test Failed";
        String msg = error.getMessage();
        if (msg == null || msg.trim().isEmpty()) {
            return error.getClass().getSimpleName();
        }
        
        // Xử lý soft assert summary
        if (msg.contains("The following asserts failed:")) {
            return "Soft assert failed";
        }
        
        // Chỉ lấy dòng đầu tiên, bỏ qua stack trace
        String[] lines = msg.split("\n");
        return lines[0].trim();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        IReporter reporter = ReportManager.getReporter();
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
            ExtentReporter.setTest(test);
            log.debug("Restored context for @Configuration method: {}", itr.getMethod().getMethodName());
        }
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
        Throwable t = itr.getThrowable();
        String message = (t == null) ? "Config failed" : "Config failed: " + t.getMessage();
        IReporter reporter = ReportManager.getReporter();
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
            ExtentTest test = CURRENT_TEST.get();
            if (test != null) {
                ExtentTest stepNode = test.createNode(stepName);
                IReporter reporter = ReportManager.getReporter();
                if (reporter instanceof ExtentReporter) {
                    ((ExtentReporter) reporter).attachScreenshotToNode(stepNode, "softassert_" + System.currentTimeMillis());
                }
                stepNode.fail("FAILED");
            } else {
                IReporter reporter = ReportManager.getReporter();
                reporter.logFail(stepName, null);
            }
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
        return Boolean.TRUE.equals(result.getAttribute("soft.assert.handled"));
    }
}
