package org.example.core.report;

import lombok.extern.slf4j.Slf4j;
import org.example.core.report.strategy.ReportStrategy;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

import static org.example.utils.DateUtils.getCurrentDate;


@Slf4j
public class SoftAssertConfig extends SoftAssert {

    private static final ThreadLocal<SoftAssertConfig> SOFT = ThreadLocal.withInitial(SoftAssertConfig::new);

    public static SoftAssertConfig get() {
        return SOFT.get();
    }

    @Override
    public void onAfterAssert(IAssert<?> assertCommand) {
    }

    @Override
    public void onAssertSuccess(IAssert<?> assertCommand) {

    }

    /**
     * - Notify the selected ReportStrategy so that the reporting backend immediately marks a failed step and captures a screenshot.
     * - Also notify the higher-level ITestReporter to log the failure and attach screenshot (reporter may implement its own behavior).
     * - The soft assertion remains soft (super.onAssertFailure records it but does not stop execution).
     */
    @Override
    public void onAssertFailure(IAssert<?> assertCommand, AssertionError ex) {
        String message = normalizeMessage(assertCommand.getMessage());
        if (message.isEmpty()) message = ex != null ? ex.getMessage() : "Assertion failed";

        String expected = stringify(assertCommand.getExpected());
        String actual = stringify(assertCommand.getActual());
        String stepName = "FAIL [" +  getCurrentDate("yyyy-MM-dd HH:mm:ss.SSS") + "]: " + message + " | expected=" + expected + " actual=" + actual;

        // 1) Strategy-level immediate step (captures screenshot + marks step failed)
        try {
            ReportStrategy strategy = ReportManager.selectStrategy();
            if (strategy != null) {
                strategy.failStep(stepName);
            }
        } catch (Throwable t) {
            log.debug("ReportStrategy.failStep failed: {}", t.getMessage());
        }

        // mark current TestNG result so listeners skip duplicate capture at teardown
        try {
            org.testng.ITestResult current = org.testng.Reporter.getCurrentTestResult();
            if (current != null) {
                current.setAttribute("soft.assert.handled", Boolean.TRUE);
            }
        } catch (Throwable t) {
            log.debug("Could not mark TestResult as handled: {}", t.getMessage());
        }

        // Also mark thread-local tracker so listeners running in same thread can detect handled state reliably.
        try {
            FailureTracker.markHandledForCurrentThread();
        } catch (Throwable t) {
            log.debug("Could not mark FailureTracker for current thread: {}", t.getMessage());
        }

        // 2) Reporter-level log + attach (reporter implementations may add additional context)
        try {
            ITestReporter reporter = ReportManager.getReporter();
            if (reporter != null) {
                try {
                    reporter.logFail(stepName, ex);
                    // request reporters to capture screenshot for this soft-assert failure
                    try { reporter.attachScreenshot("softassert_" + System.currentTimeMillis() + ".png"); } catch (Throwable tt) { log.debug("Reporter.attachScreenshot failed: {}", tt.getMessage()); }
                } catch (Throwable t) {
                    log.debug("Reporter.logFail failed: {}", t.getMessage());
                }
            }
        } catch (Throwable t) {
            log.debug("Failed to notify reporter about soft-assert failure: {}", t.getMessage());
        }

        // Record the soft-assert failure (does not throw), so test continues.
        super.onAssertFailure(assertCommand, ex);
    }

    private String normalizeMessage(String message) {
        return message == null ? "" : message;
    }

    private String stringify(Object obj) {
        try {
            return String.valueOf(obj);
        } catch (Exception e) {
            return "";
        }
    }

    public static void reset() {
        SOFT.remove();
        SOFT.set(new SoftAssertConfig());
    }

}



