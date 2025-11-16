package org.example.core.assertion;

import lombok.extern.slf4j.Slf4j;
import org.example.core.report.IReporter;
import org.example.core.report.ReportManager;
import org.example.core.report.listener.IReportStrategyListener;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

import static org.example.common.Constants.DEFAULT_TIMESTAMP_FORMAT;
import static org.example.utils.DateUtils.getCurrentTimestamp;

import java.util.function.Supplier;

@Slf4j
public class SoftAssertImpl extends SoftAssert {

    private static final ThreadLocal<SoftAssertImpl> SOFT = ThreadLocal.withInitial(SoftAssertImpl::new);

    public static SoftAssertImpl get() {
        return SOFT.get();
    }

    @Override
    public void onAfterAssert(IAssert<?> assertCommand) {
    }

    @Override
    public void onAssertSuccess(IAssert<?> assertCommand) {
        // no implementation
    }

    /**
     * - Notify the selected IReportStrategyListener so that the reporting backend immediately marks a failed step and captures a screenshot.
     * - Also notify the higher-level IReporter to log the failure and attach screenshot (reporter may implement its own behavior).
     * - The soft assertion remains soft (super.onAssertFailure records it but does not stop execution).
     */
    @Override
    public void onAssertFailure(IAssert<?> assertCommand, AssertionError ex) {
        String message = normalizeMessage(assertCommand.getMessage());
        if (message.isEmpty()) message = ex != null ? ex.getMessage() : "Assertion failed";

        String expected = stringify(assertCommand.getExpected());
        String actual = stringify(assertCommand.getActual());
        String stepName = "FAIL [" +  getCurrentTimestamp(DEFAULT_TIMESTAMP_FORMAT) + "]: " + message + " | expected=" + expected + " actual=" + actual;

        // 1) Strategy-level immediate step (marks step failed)
        IReportStrategyListener strategy = safeGet(() -> ReportManager.selectStrategy(), null, "ReportManager.selectStrategy failed");
        if (strategy != null) {
            safeRun(() -> strategy.failStep(stepName), "IReportStrategyListener.failStep failed");
        }

        // mark current TestNG result so listeners skip duplicate capture at teardown
        safeRun(() -> {
            org.testng.ITestResult current = org.testng.Reporter.getCurrentTestResult();
            if (current != null) {
                current.setAttribute("soft.assert.handled", Boolean.TRUE);
            }
        }, "Could not mark TestResult as handled");

        // 2) Reporter-level log không cần vì failStep đã tạo step failed với screenshot rồi

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
        SOFT.set(new SoftAssertImpl());
    }

    private void safeRun(Runnable action, String debugMessage) {
        try {
            action.run();
        } catch (Throwable t) {
            log.debug("{}: {}", debugMessage, t.getMessage(), t);
        }
    }

    private <T> T safeGet(Supplier<T> supplier, T defaultValue, String debugMessage) {
        try {
            return supplier.get();
        } catch (Throwable t) {
            log.debug("{}: {}", debugMessage, t.getMessage(), t);
            return defaultValue;
        }
    }
}
