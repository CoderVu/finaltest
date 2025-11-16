package org.example.core.control.util;

import org.example.configure.Config;
import org.example.core.driver.DriverFactory;
import org.example.enums.BrowserType;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DriverUtils {

    private DriverUtils() {
    }

    public static WebDriver getWebDriver() {
        var manager = DriverFactory.getCurrentDriverManager();
        if (manager == null) {
            throw new IllegalStateException("WebDriver not initialized for current thread. Call DriverFactory.createDriver(...) first.");
        }
        return manager.getDriver();
    }

    public static String sanitizeVersion(String version) {
        if (version == null) return null;
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)+)");
        Matcher matcher = pattern.matcher(version);
        return matcher.find() ? matcher.group(1) : version.trim();
    }

    public static void deleteCookie() {
        getWebDriver().manage().deleteAllCookies();
    }

    public static Object execJavaScript(String script, Object... args) {
        return ((JavascriptExecutor) getWebDriver()).executeScript(script, args);
    }

    public static String getAppiumCapability(String key) {
        return ((RemoteWebDriver) getWebDriver()).getCapabilities().getCapability(key).toString();
    }

    public static void executeJavaScript(String argumentsClick, WebElement element) {
        ((JavascriptExecutor) getWebDriver()).executeScript(argumentsClick, element);
    }

    public static String getCurrentUrl() {
        return getWebDriver().getCurrentUrl();
    }

    public static void waitForUrlContains(String expectedUrlPart, Duration timeout) {
        WebDriverWait wait = new WebDriverWait(getWebDriver(), timeout);
        wait.until(driver -> driver.getCurrentUrl().contains(expectedUrlPart));
    }

    public static void waitForUrlContains(String expectedUrlPart, int timeoutInSeconds) {
        waitForUrlContains(expectedUrlPart, Duration.ofSeconds(timeoutInSeconds));
    }

    public static void openNewTab() {
        ((JavascriptExecutor) getWebDriver()).executeScript("window.open('about:blank','_blank');");
    }

    public static void refresh() {
        getWebDriver().navigate().refresh();
    }

    public static String getWindowHandle() {
        return getWebDriver().getWindowHandle();
    }

    public static List<String> getWindowHandles() {
        return new ArrayList<>(getWebDriver().getWindowHandles());
    }

    public static int getNumberOfWindows() {
        return getWebDriver().getWindowHandles().size();
    }

    public static void switchTo(String windowHandle) {
        getWebDriver().switchTo().window(windowHandle);
    }

    public static void switchToNewWindow() {
        for (String winHandle : getWebDriver().getWindowHandles()) {
            getWebDriver().switchTo().window(winHandle);
        }
    }

    public static void switchToWindow(int index) {
        ArrayList<String> windows = new ArrayList<>(getWebDriver().getWindowHandles());
        getWebDriver().switchTo().window(windows.get(index));
    }

    public static Duration getTimeOut() {
        return Config.getTimeout();
    }

    public static void waitForNewWindowOpened(int expectedNumberOfWindows) {
        WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeOut());
        wait.until(ExpectedConditions.numberOfWindowsToBe(expectedNumberOfWindows));
    }

    public static void moveMouseByOffset(int x, int y) {
        Actions action = new Actions(getWebDriver());
        action.moveByOffset(x, y).perform();
    }

    public static boolean isUrlStable(String url, StringBuilder msg) {
        try {
            if (msg == null) msg = new StringBuilder();
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("HEAD");
            int statusCode = con.getResponseCode();
            msg.append("Status code: ").append(statusCode);
            msg.append(" - Error: ").append(con.getResponseMessage());
            return (statusCode == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean waitForCondition(Callable<Boolean> conditionEvaluator, Duration interval, Duration timeout) {
        Wait<WebDriver> wait = new FluentWait<>(getWebDriver()).withTimeout(timeout).pollingEvery(interval);
        try {
            return wait.until(driver -> {
                try {
                    return conditionEvaluator.call();
                } catch (Exception e) {
                    return false;
                }
            });
        } catch (TimeoutException e) {
            return false;
        }
    }

    public static void waitForJavaScriptIdle() {
        try {
            WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeOut());
            wait.until(driver -> {
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                Object domIsComplete = executor.executeScript("return document.readyState == 'complete';");
                return Boolean.TRUE.equals(domIsComplete);
            });
        } catch (Exception ignored) {
        }
    }

    public static void waitForAjax() {
        try {
            WebDriverWait wait = new WebDriverWait(getWebDriver(), getTimeOut());
            wait.until(driver -> {
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                Object ajaxIsComplete = executor.executeScript(
                        "if (typeof jQuery != 'undefined') { return jQuery.active == 0; } else {  return true; }");
                Object domIsComplete = executor.executeScript("return document.readyState == 'complete';");
                return Boolean.TRUE.equals(ajaxIsComplete) && Boolean.TRUE.equals(domIsComplete);
            });
        } catch (Exception ignored) {
        }
    }

    public static void setWindowSize(int width, int height) {
        getWebDriver().manage().window().setSize(new Dimension(width, height));
    }

    public static void waitForEventTriggered(String eventRegex) {
        waitForEventTriggered(eventRegex, getTimeOut());
    }

    public static void waitForEventTriggered(String eventRegex, Duration timeout) {
        List<Map<String, Object>> xmlHttpRequestList = getXmlHttpRequestList();
        double interval = 0.5;
        double timeOut = timeout.getSeconds();
        Pattern pattern = Pattern.compile(eventRegex);
        while (timeOut > 0) {
            for (Map<String, Object> xmlHttpRequest : xmlHttpRequestList) {
                Object name = xmlHttpRequest.get("name");
                if (name instanceof String) {
                    Matcher matcher = pattern.matcher((String) name);
                    if (matcher.find()) {
                        return;
                    }
                }
            }
            delay(interval);
            timeOut -= interval;
            xmlHttpRequestList = getXmlHttpRequestList();
        }
    }

    @Deprecated
    public static void waitForEventTriggered(String eventRegex, int inputTimeOut) {
        waitForEventTriggered(eventRegex, Duration.ofSeconds(inputTimeOut));
    }

    public static List<Map<String, Object>> getXmlHttpRequestList() {
        List<Map<String, Object>> requestList = (List<Map<String, Object>>) DriverUtils.execJavaScript("return window.performance.getEntries()");
        return requestList == null ? new ArrayList<>() : requestList.stream()
                .filter(m -> m.containsKey("initiatorType"))
                .filter(x -> "xmlhttprequest".equals(String.valueOf(x.get("initiatorType"))))
                .collect(Collectors.toList());
    }

    public static void delay(double seconds) {
        try {
            Thread.sleep((long) (seconds * 1000L));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    public static void waitForAutoScrollingStopped() {
        delay(1);
    }

    public static void navigateTo(String url) {
        getWebDriver().get(url);
    }
}
