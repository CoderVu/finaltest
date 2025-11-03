package org.example.core.report.strategy;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.MediaEntityBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.core.driver.DriverManager;
import org.example.core.report.impl.ExtentTestReporter;
import org.example.enums.BrowserType;
import org.example.configure.Config;
import org.openqa.selenium.*;
import org.openqa.selenium.io.FileHandler;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ExtentStrategy implements ReportStrategy {
    private static final ExtentReports EXTENT = new ExtentReports();
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private static final Map<String, ExtentTest> NAME_TO_TEST = new ConcurrentHashMap<>();

    private static volatile boolean INITIALIZED = false;
    private static String REPORT_DIR = null;

    @Override
    public void onStart(ITestContext context) {
        if (INITIALIZED) return;
        synchronized (ExtentStrategy.class) {
            if (INITIALIZED) return;

            // Tạo thư mục chứa report và screenshot
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
            ExtentSparkReporter spark = new ExtentSparkReporter(htmlReport);

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
                log.info("✅ ExtentReports initialized at {}", htmlReport.getAbsolutePath());
            } catch (Throwable t) {
                log.warn("⚠️ Failed to initialize ExtentSparkReporter: {}", t.getMessage(), t);
            } finally {
                INITIALIZED = true;
            }
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        try {
            EXTENT.flush();
            log.info("✅ Extent report generated at {}", REPORT_DIR);
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
        getTest(result).ifPresent(t -> t.log(Status.PASS, "✅ Test Passed"));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (isSoftAssertHandled(result)) return;

        Throwable error = result.getThrowable();
        String message = (error == null) ? "❌ Test Failed" : error.getMessage();
        String screenshot = attachScreenshot();

        getTest(result).ifPresent(t -> {
            try {
                if (screenshot != null)
                    t.fail(message, MediaEntityBuilder.createScreenCaptureFromPath(screenshot).build());
                else
                    t.fail(message);
            } catch (Exception e) {
                t.log(Status.FAIL, message);
            }
        });
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String screenshot = attachScreenshot();
        getTest(result).ifPresent(t -> {
            try {
                if (screenshot != null)
                    t.skip("⏭️ Skipped", MediaEntityBuilder.createScreenCaptureFromPath(screenshot).build());
                else
                    t.skip("⏭️ Skipped");
            } catch (Exception e) {
                t.log(Status.SKIP, "Skipped");
            }
        });
    }

    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onTestFailedWithTimeout(ITestResult result) { onTestFailure(result); }

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
        String message = (t == null) ? "⚙️ Config failed" : "⚙️ Config failed: " + t.getMessage();
        String screenshot = attachScreenshot();

        getTest(itr).ifPresent(test -> {
            try {
                if (screenshot != null)
                    test.fail(message, MediaEntityBuilder.createScreenCaptureFromPath(screenshot).build());
                else
                    test.log(Status.FAIL, message);
            } catch (Exception e) {
                test.log(Status.FAIL, message);
            }
        });
    }

    @Override public void onConfigurationSkip(ITestResult itr) {}

    @Override
    public void failStep(String stepName) {
        try {
            ExtentTest test = CURRENT_TEST.get();
            if (test == null) test = EXTENT.createTest(stepName);

            String screenshot = attachScreenshot();
            if (screenshot != null)
                test.fail(stepName, MediaEntityBuilder.createScreenCaptureFromPath(screenshot).build());
            else
                test.fail(stepName);
        } catch (Throwable t) {
            log.warn("failStep failed: {}", t.getMessage());
        }
    }

    // ================== Screenshot logic ==================
    private String attachScreenshot() {
        try {
            BrowserType browserType = Config.getBrowserType();
            WebDriver driver = DriverManager.getInstance(browserType).getDriver();
            if (driver instanceof TakesScreenshot) {
                String screenshotName = "screenshot_" + System.currentTimeMillis() + ".png";
                File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

                File screenshotDir = new File(REPORT_DIR, "screenshots");
                if (!screenshotDir.exists()) {
                    if (!screenshotDir.mkdirs()) {
                        log.warn("Could not create screenshot directory: {}", screenshotDir.getAbsolutePath());
                    }
                }

                File dest = new File(screenshotDir, screenshotName);
                FileHandler.copy(src, dest);

                // Trả về đường dẫn tương đối để hiển thị được trong HTML
                return "screenshots/" + screenshotName;
            }
        } catch (Throwable e) {
            log.warn("❌ Screenshot capture failed: {}", e.getMessage());
        }
        return null;
    }

    // ================== Helpers ==================
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
