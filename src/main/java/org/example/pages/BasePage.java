package org.example.pages;

import lombok.extern.slf4j.Slf4j;
import org.example.core.reporting.ReportClient;
import org.example.core.reporting.ReportingManager;

import java.util.function.Supplier;

/**
 * Base class cho tất cả PageObject.
 * 
 * Sử dụng đơn giản như Allure.step():
 * - step(() -> { ... }) - Tự động lấy tên method
 * - step("Step name", () -> { ... }) - Tên tùy chỉnh
 * - Nếu method A gọi method B, B tự động trở thành child step của A
 */
@Slf4j
public class BasePage {

    protected ReportClient reporter = ReportingManager.getReportClient();

    /**
     * Tạo step - đơn giản như Allure.step()
     * Tự động lấy tên method làm tên step.
     * 
     * Ví dụ:
     * public void login() {
     *     step(() -> {
     *         enterUsername();
     *         enterPassword(); // Tự động trở thành child step
     *     });
     * }
     */
    protected void step(Runnable action) {
        String methodName = getCallingMethodName();
        String stepName = formatMethodName(methodName);
        reporter.childStep(stepName, action);
    }

    /**
     * Tạo step với tên tùy chỉnh.
     * 
     * Ví dụ:
     * step("Login with username: " + username, () -> {
     *     enterUsername(username);
     * });
     */
    protected void step(String stepName, Runnable action) {
        reporter.childStep(stepName, action);
    }

    /**
     * Tạo step với return value.
     */
    protected <T> T step(Supplier<T> supplier) {
        String methodName = getCallingMethodName();
        String stepName = formatMethodName(methodName);
        return reporter.childStep(stepName, supplier);
    }

    /**
     * Tạo step với tên tùy chỉnh và return value.
     */
    protected <T> T step(String stepName, Supplier<T> supplier) {
        return reporter.childStep(stepName, supplier);
    }

    /**
     * Log thông tin (không tạo step).
     */
    protected void log(String message) {
        reporter.info(message);
    }

    /**
     * Attach screenshot.
     */
    protected void screenshot(String name) {
        reporter.attachScreenshot(name);
    }

    /**
     * Lấy tên method đang gọi step() từ stack trace.
     */
    private String getCallingMethodName() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // stack[0] = getStackTrace
        // stack[1] = getCallingMethodName
        // stack[2] = step
        // stack[3] = method đang gọi step (method của PageObject)
        if (stack.length > 3) {
            return stack[3].getMethodName();
        }
        return "unknown";
    }

    /**
     * Format tên method thành tên step dễ đọc.
     * Ví dụ: "enterDestination" -> "Enter Destination"
     */
    private String formatMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            return "Unknown Step";
        }
        
        // Chuyển camelCase thành "Camel Case"
        String formatted = methodName.replaceAll("([a-z])([A-Z])", "$1 $2");
        // Viết hoa chữ cái đầu
        return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
    }
}
