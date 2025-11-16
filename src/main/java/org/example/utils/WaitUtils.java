package org.example.utils;

import java.time.Duration;
import java.util.function.Function;
import org.example.common.Constants;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;


public final class WaitUtils {
    private WaitUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Wait using a locator and element-based condition.
     * Returns true if condition satisfied within timeout, false otherwise.
     */
    public static boolean waitForCondition(WebDriver driver, By locator,
                                           Function<WebElement, Boolean> condition,
                                           Duration timeout, Logger log) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        WebDriverWait wait = new WebDriverWait(driver, actualTimeout);
        try {
            wait.until(d -> {
                try {
                    WebElement e = d.findElement(locator);
                    return condition.apply(e);
                } catch (NoSuchElementException ex) {
                    return false;
                }
            });
            return true;
        } catch (Exception e) {
            if (log != null) {
                log.error("waitForCondition error on '{}': {}", locator, e.getMessage().split("\n")[0]);
            }
            return false;
        }
    }

    /**
     * Convenience overload without Logger.
     */
    public static boolean waitForCondition(WebDriver driver, By locator,
                                           Function<WebElement, Boolean> condition,
                                           Duration timeout) {
        return waitForCondition(driver, locator, condition, timeout, null);
    }

    /**
     * Generic WebDriver-based wait: caller supplies a Function<WebDriver, Boolean>.
     */
    public static boolean waitForCondition(WebDriver driver,
                                           Function<WebDriver, Boolean> condition,
                                           Duration timeout, Logger log) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        WebDriverWait wait = new WebDriverWait(driver, actualTimeout);
        try {
            wait.until(condition::apply);
            return true;
        } catch (Exception e) {
            if (log != null) {
                log.error("waitForCondition (generic) error: {}", e.getMessage().split("\n")[0]);
            }
            return false;
        }
    }

    /**
     * @deprecated Use Duration-based overload instead
     */
    @Deprecated
    public static boolean waitForCondition(WebDriver driver, By locator,
                                           Function<WebElement, Boolean> condition,
                                           int timeoutInSeconds, Logger log) {
        return waitForCondition(driver, locator, condition, Duration.ofSeconds(timeoutInSeconds), log);
    }

    /**
     * @deprecated Use Duration-based overload instead
     */
    @Deprecated
    public static boolean waitForCondition(WebDriver driver, By locator,
                                           Function<WebElement, Boolean> condition,
                                           int timeoutInSeconds) {
        return waitForCondition(driver, locator, condition, Duration.ofSeconds(timeoutInSeconds));
    }

    /**
     * @deprecated Use Duration-based overload instead
     */
    @Deprecated
    public static boolean waitForCondition(WebDriver driver,
                                           Function<WebDriver, Boolean> condition,
                                           int timeoutInSeconds, Logger log) {
        return waitForCondition(driver, condition, Duration.ofSeconds(timeoutInSeconds), log);
    }

}

