package org.example.core.element.util;

import java.time.Duration;

import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public final class WaitUtils {
    private WaitUtils() {}

    public static <T> T waitFor(ExpectedCondition<T> condition) {
        return waitFor(condition, null);
    }

    public static <T> T waitFor(ExpectedCondition<T> condition, Duration timeout) {
        Duration effective = timeout == null ? DriverUtils.getTimeOut() : timeout;
        return new WebDriverWait(DriverUtils.getWebDriver(), effective).until(condition);
    }
}
