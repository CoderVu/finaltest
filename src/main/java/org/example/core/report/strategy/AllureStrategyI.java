package org.example.core.report.strategy;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.example.core.report.listener.IReportStrategyListener;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;

import static org.example.common.Constants.DEFAULT_TIMESTAMP_FORMAT;
import static org.example.core.control.util.DriverUtils.getWebDriver;
import static org.example.utils.DateUtils.getCurrentTimestamp;

@Slf4j
public class AllureStrategyI implements IReportStrategyListener {

    @Override
    public void onStart(ITestContext context) {
        log.debug("Allure report: start suite {}", context.getSuite().getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.debug("Allure report: finish suite {}", context.getSuite().getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.debug("Test started: {}", result.getName());
        // Ensure Allure test case is created for this test
        try {
            String testUuid = Allure.getLifecycle().getCurrentTestCaseOrStep().orElse(null);
            if (testUuid == null) {
                // Create test case if not exists
                String testCaseUuid = java.util.UUID.randomUUID().toString();
                io.qameta.allure.model.TestResult testResult = new io.qameta.allure.model.TestResult()
                        .setUuid(testCaseUuid)
                        .setName(result.getMethod().getMethodName())
                        .setFullName(result.getTestClass().getName() + "." + result.getMethod().getMethodName())
                        .setStatus(Status.PASSED);
                Allure.getLifecycle().scheduleTestCase(testResult);
                Allure.getLifecycle().startTestCase(testCaseUuid);
            }
        } catch (Exception e) {
            log.debug("Could not create Allure test case: {}", e.getMessage());
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.debug("Test passed: {}", result.getName());
        try {
            Allure.getLifecycle().getCurrentTestCaseOrStep().ifPresent(testUuid -> {
                Allure.getLifecycle().updateTestCase(testUuid, tr -> tr.setStatus(Status.PASSED));
                Allure.getLifecycle().stopTestCase(testUuid);
                Allure.getLifecycle().writeTestCase(testUuid);
            });
        } catch (Exception e) {
            log.debug("Could not update Allure test case on success: {}", e.getMessage());
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable t = result.getThrowable();
        try {
            Allure.getLifecycle().getCurrentTestCaseOrStep().ifPresent(testUuid -> {
                Allure.getLifecycle().updateTestCase(testUuid, tr -> {
                    tr.setStatus(Status.FAILED);
                    if (t != null) {
                        tr.setStatusDetails(new io.qameta.allure.model.StatusDetails()
                                .setMessage(getShortErrorMessage(t))
                                .setTrace(getStackTrace(t)));
                    }
                });
                Allure.getLifecycle().stopTestCase(testUuid);
                Allure.getLifecycle().writeTestCase(testUuid);
            });
        } catch (Exception e) {
            log.debug("Could not update Allure test case on failure: {}", e.getMessage());
        }
        log.debug("Test failed: {}", result.getName());
    }
    
    private String getStackTrace(Throwable t) {
        if (t == null) return "";
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    private String getShortErrorMessage(Throwable t) {
        if (t == null) return "Unknown error";
        String msg = t.getMessage();
        if (msg == null || msg.trim().isEmpty()) {
            return t.getClass().getSimpleName();
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
        log.debug("Test skipped: {}", result.getName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.debug("Test failed within success percentage: {}", result.getName());
    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {
        attachScreenshot("test_timeout_" + result.getName());
        onTestFailure(result);
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
        log.debug("Configuration method completed: {}", itr.getMethod().getMethodName());
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
        attachScreenshot(getCurrentTimestamp(DEFAULT_TIMESTAMP_FORMAT) + "config_failure_" +
                (itr.getMethod() != null ? itr.getMethod().getMethodName() : "config"));
        Throwable t = itr.getThrowable();
        if (t != null) {
            Allure.step(getCurrentTimestamp(DEFAULT_TIMESTAMP_FORMAT) + "CONFIG FAIL: " + t.getMessage(), Status.FAILED);
        }
        log.error("Configuration failure: {}", t != null ? t.getMessage() : "Unknown error");
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
    }

    public static void attachScreenshot(String name) {
        try {
            byte[] bytes = getScreenshotBytes();
            if (bytes != null && bytes.length > 0) {
                Allure.addAttachment(name, "image/png", new ByteArrayInputStream(bytes), "png");
            }
        } catch (Throwable ignored) {
        }
    }

    private static byte[] getScreenshotBytes() {
        try {
            WebDriver driver = getWebDriver();
            if (driver != null && driver instanceof TakesScreenshot) {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            }
        } catch (Throwable ignored) {
        }
        return new byte[0];
    }

    @Override
    public void failStep(String name) {
        String stepUuid = java.util.UUID.randomUUID().toString();
        try {
            io.qameta.allure.model.StepResult sr = new io.qameta.allure.model.StepResult()
                    .setName(name)
                    .setStatus(Status.FAILED);

            Allure.getLifecycle().startStep(stepUuid, sr);
            
            try {
                byte[] bytes = getScreenshotBytes();
                if (bytes != null && bytes.length > 0) {
                    Allure.getLifecycle().addAttachment("Screenshot", "image/png", "png", new ByteArrayInputStream(bytes));
                }
            } catch (Throwable t) {
                log.debug("Failed to capture screenshot for failed step '{}': {}", name, t.getMessage());
            }
            
            Allure.getLifecycle().stopStep(stepUuid);
        } catch (IllegalStateException | IllegalThreadStateException e) {
            log.debug("Cannot create Allure failed step (no active lifecycle): {}", name);
        } catch (Throwable e) {
            log.debug("Unexpected error while creating Allure failed step '{}': {}", name, e.getMessage());
        }
    }

}
