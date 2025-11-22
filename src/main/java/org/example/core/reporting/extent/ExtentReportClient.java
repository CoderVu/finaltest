package org.example.core.reporting.extent;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.ReportClient;
import org.example.enums.ReportType;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.io.FileHandler;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.example.core.element.util.DriverUtils.getWebDriver;

@Slf4j
public class ExtentReportClient implements ReportClient {

    @Override
    public ReportType getReportType() {
        return ReportType.EXTENT;
    }

    @Override
    public boolean isInStep() {
        ITestResult current = currentResult();
        Deque<ExtentTest> stack = getExistingStack(current);
        return current != null && stack != null && !stack.isEmpty();
    }

    @Override
    public void logStep(String message) {
        ExtentTest node = getActiveNode();
        if (node != null) {
            node.info("STEP: " + message);
        }
    }

    @Override
    public void info(String message) {
        ExtentTest node = getActiveNode();
        if (node != null) {
            node.info(message);
        } else {
            log.info(message);
        }
    }

    @Override
    public void logFail(String message, Throwable error) {
        ExtentTest test = getActiveTest();
        if (test != null) {
            if (error != null) {
                test.fail(message + " - " + error.getMessage());
            } else {
                test.fail(message);
            }
        } else {
            log.error("FAIL: {} - {}", message, error != null ? error.getMessage() : "No error");
        }
    }

    @Override
    public void attachScreenshot(String name) {
        ExtentTest test = getActiveTest();
        WebDriver driver = getWebDriver();
        if (driver != null && (driver instanceof TakesScreenshot)) {
            try {
                // Check if session is still valid by attempting a simple operation
                driver.getWindowHandle();
            } catch (Exception e) {
                log.warn("WebDriver session is invalid, skipping screenshot: {}", e.getMessage());
                return;
            }

            File src;
            try {
                src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            } catch (Exception e) {
                log.warn("Failed to capture screenshot (session may be invalid): {}", e.getMessage());
                return;
            }

            String reportDirPath = ExtentReportLifecycle.getReportDir();
            File screenshotDir = (reportDirPath != null && !reportDirPath.isEmpty())
                    ? new File(reportDirPath, "screenshots")
                    : new File(System.getProperty("user.dir"), "target/extent-report/screenshots");

            if (!screenshotDir.exists() && !screenshotDir.mkdirs()) {
                log.warn("Could not create screenshot directory: {}", screenshotDir.getAbsolutePath());
            }

            String filename = (name == null || name.isEmpty())
                    ? "screenshot_" + System.currentTimeMillis() + ".png"
                    : name;
            if (!filename.toLowerCase().endsWith(".png")) {
                filename += ".png";
            }

            File dest = new File(screenshotDir, filename);
            try {
                FileHandler.copy(src, dest);
            } catch (Throwable e) {
                log.error("Failed to copy screenshot to destination: {}", e.getMessage());
                return;
            }

            String pathForReport = (reportDirPath != null && !reportDirPath.isEmpty())
                    ? "screenshots/" + filename
                    : dest.getAbsolutePath();

            if (test != null) {
                try {
                    test.info("Screenshot: " + filename,
                            MediaEntityBuilder.createScreenCaptureFromPath(pathForReport).build());
                } catch (Exception e) {
                    log.warn("Unable to attach screenshot to extent report: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public void childStep(String name, Runnable runnable) {
        ITestResult result = currentResult();
        if (result == null) {
            runnable.run();
            return;
        }

        ExtentTest parent = getActiveNode(result);
        if (parent == null) {
            runnable.run();
            return;
        }

        Deque<ExtentTest> stack = ensureStack(result);
        ExtentTest stepNode = parent.createNode(name);
        stack.push(stepNode);

        try {
            runnable.run();
            stepNode.pass("PASSED");
        } catch (Throwable e) {
            attachScreenshotToNode(stepNode, "step_fail_" + name);
            stepNode.fail("FAILED: " + e.getMessage());
            throwRuntime(e);
        } finally {
            stack.pop();
        }
    }

    @Override
    public <T> T childStep(String name, java.util.function.Supplier<T> supplier) {
        ITestResult result = currentResult();
        if (result == null) {
            return supplier.get();
        }

        ExtentTest parent = getActiveNode(result);
        if (parent == null) {
            return supplier.get();
        }

        Deque<ExtentTest> stack = ensureStack(result);
        ExtentTest stepNode = parent.createNode(name);
        stack.push(stepNode);

        try {
            T returnValue = supplier.get();
            stepNode.pass("PASSED");
            return returnValue;
        } catch (Throwable e) {
            attachScreenshotToNode(stepNode, "step_fail_" + name);
            stepNode.fail("FAILED: " + e.getMessage());
            throwRuntime(e);
            return null; // unreachable
        } finally {
            stack.pop();
        }
    }

    private ExtentTest getActiveNode() {
        return getActiveNode(currentResult());
    }

    private ExtentTest getActiveNode(ITestResult result) {
        if (result == null) {
            return null;
        }
        Deque<ExtentTest> stack = getExistingStack(result);
        if (stack != null && !stack.isEmpty()) {
            return stack.peek();
        }
        // Try to get attempt node first, then fall back to test node
        ExtentTest attemptNode = (ExtentTest) result.getAttribute(ExtentReportLifecycle.ATTEMPT_NODE_ATTRIBUTE);
        if (attemptNode != null) {
            return attemptNode;
        }
        return getActiveTest(result);
    }

    private ExtentTest getActiveTest() {
        return getActiveTest(currentResult());
    }

    private ExtentTest getActiveTest(ITestResult result) {
        if (result == null) {
            return null;
        }
        // Try to get attempt node first, then fall back to test node
        ExtentTest attemptNode = (ExtentTest) result.getAttribute(ExtentReportLifecycle.ATTEMPT_NODE_ATTRIBUTE);
        if (attemptNode != null) {
            return attemptNode;
        }
        return (ExtentTest) result.getAttribute(ExtentReportLifecycle.TEST_ATTRIBUTE);
    }

    private ITestResult currentResult() {
        return Reporter.getCurrentTestResult();
    }

    @SuppressWarnings("unchecked")
    private Deque<ExtentTest> getExistingStack(ITestResult result) {
        if (result == null) {
            return null;
        }
        return (Deque<ExtentTest>) result.getAttribute(ExtentReportLifecycle.STEP_STACK_ATTRIBUTE);
    }

    private Deque<ExtentTest> ensureStack(ITestResult result) {
        Deque<ExtentTest> stack = getExistingStack(result);
        if (stack == null) {
            stack = new ArrayDeque<>();
            result.setAttribute(ExtentReportLifecycle.STEP_STACK_ATTRIBUTE, stack);
        }
        return stack;
    }

    private void throwRuntime(Throwable e) {
        if (e instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw new RuntimeException(e);
    }

    public void attachScreenshotToNode(ExtentTest stepNode, String name) {
        WebDriver driver = getWebDriver();
        if (driver == null) {
            log.debug("WebDriver is null, skipping screenshot attachment");
            return;
        }
        
        if (!(driver instanceof TakesScreenshot)) {
            log.debug("WebDriver does not support screenshots");
            return;
        }

        try {
            // Check if session is still valid by attempting a simple operation
            driver.getWindowHandle();
        } catch (Exception e) {
            log.warn("WebDriver session is invalid, skipping screenshot: {}", e.getMessage());
            return;
        }

        File src;
        try {
            src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        } catch (Exception e) {
            log.warn("Failed to capture screenshot (session may be invalid): {}", e.getMessage());
            return;
        }

        String reportDirPath = ExtentReportLifecycle.getReportDir();
        File screenshotDir = (reportDirPath != null && !reportDirPath.isEmpty())
                ? new File(reportDirPath, "screenshots")
                : new File(System.getProperty("user.dir"), "target/extent-report/screenshots");
        if (!screenshotDir.exists() && !screenshotDir.mkdirs()) {
            log.warn("Could not create screenshot directory: {}", screenshotDir.getAbsolutePath());
        }
        String filename = (name == null || name.isEmpty())
                ? "screenshot_" + System.currentTimeMillis() + ".png"
                : name;
        if (!filename.toLowerCase().endsWith(".png")) {
            filename += ".png";
        }
        try {
            FileHandler.copy(src, new File(screenshotDir, filename));
        } catch (Throwable e) {
            log.error("Failed to copy screenshot to destination: {}", e.getMessage());
            return;
        }
        String pathForReport = (reportDirPath != null && !reportDirPath.isEmpty())
                ? "screenshots/" + filename
                : new File(screenshotDir, filename).getAbsolutePath();
        try {
            stepNode.info("Screenshot", MediaEntityBuilder.createScreenCaptureFromPath(pathForReport).build());
        } catch (Exception e) {
            log.warn("Unable to attach screenshot to extent report: {}", e.getMessage());
        }
    }
}

