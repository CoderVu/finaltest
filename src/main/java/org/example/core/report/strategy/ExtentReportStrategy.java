package org.example.core.report.strategy;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import lombok.extern.slf4j.Slf4j;
import org.example.core.report.impl.ExtentReporter;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ExtentReportStrategy implements ReportStrategy {

    private static final ExtentReports EXTENT = new ExtentReports();
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<>();
    private static final Map<String, ExtentTest> NAME_TO_TEST = new ConcurrentHashMap<>();

    @Override
    public void onStart(ITestContext context) {
        File outDir = new File("target/extent");
        if (!outDir.exists()) outDir.mkdirs();
        ExtentSparkReporter spark = new ExtentSparkReporter(new File(outDir, "index.html"));
        EXTENT.attachReporter(spark);
        EXTENT.setSystemInfo("Suite", context.getSuite().getName());
        log.info("ExtentReports initialized at {}", outDir.getAbsolutePath());
    }

    @Override
    public void onFinish(ITestContext context) {
        try { EXTENT.flush(); } catch (Throwable t) { log.warn("Extent flush failed: {}", t.getMessage()); }
        // Cleanup ExtentReporter
        ExtentReporter.clearTest();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        ExtentTest test = EXTENT.createTest(name);
        NAME_TO_TEST.put(name, test);
        CURRENT_TEST.set(test);
        // Register test with ExtentReporter for TestReporter interface
        ExtentReporter.setTest(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = getTest(result);
        if (test != null) test.log(Status.PASS, "Passed");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = getTest(result);
        if (test != null) {
            Throwable t = result.getThrowable();
            test.log(Status.FAIL, t == null ? "Failed" : t.getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = getTest(result);
        if (test != null) test.log(Status.SKIP, "Skipped");
    }

    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) { }
    @Override public void onTestFailedWithTimeout(ITestResult result) { onTestFailure(result); }
    @Override public void onConfigurationSuccess(ITestResult itr) { }
    @Override public void onConfigurationFailure(ITestResult itr) { }
    @Override public void onConfigurationSkip(ITestResult itr) { }

    private ExtentTest getTest(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        if (test != null) return test;
        return NAME_TO_TEST.get(result.getMethod().getMethodName());
    }
}


