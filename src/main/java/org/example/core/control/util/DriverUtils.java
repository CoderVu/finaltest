package org.example.core.control.util;

import org.example.configure.Config;
import org.example.core.driver.DriverManager;
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

    private DriverUtils() {}

    public static WebDriver getWebDriver() {
        BrowserType browserType = Config.getBrowserType();
        return DriverManager.getInstance(browserType).getDriver();
    }

    public static WebDriver getDriver() {
        return getWebDriver();
    }

    public static void deleteCookie() {
        getDriver().manage().deleteAllCookies();
    }

    public static Object execJavaScript(String script, Object... args) {
        return ((JavascriptExecutor) getDriver()).executeScript(script, args);
    }

    public static String getAppiumCapability(String key) {
        return ((RemoteWebDriver) getDriver()).getCapabilities().getCapability(key).toString();
    }
    public static void executeJavaScript(String arguments0click, WebElement element) {
        ((JavascriptExecutor) getDriver()).executeScript(arguments0click, element);
    }

    public static String getCurrentUrl() {
        return getDriver().getCurrentUrl();
    }
    public static void waitForUrlContains(String expectedUrlPart, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutInSeconds));
        wait.until(driver -> driver.getCurrentUrl().contains(expectedUrlPart));
    }

    public static void openNewTab() {
        ((JavascriptExecutor) getDriver()).executeScript("window.open('about:blank','_blank');");
    }

    public static void refresh() {
        getDriver().navigate().refresh();
    }

    public static String getWindowHandle() {
        return getDriver().getWindowHandle();
    }

    public static List<String> getWindowHandles() {
        return new ArrayList<>(getDriver().getWindowHandles());
    }

    public static int getNumberOfWindows() {
        return getDriver().getWindowHandles().size();
    }

    public static void switchTo(String windowHandle) {
        getDriver().switchTo().window(windowHandle);
    }

    public static void switchToNewWindow() {
        for (String winHandle : getDriver().getWindowHandles()) {
            getDriver().switchTo().window(winHandle);
        }
    }

    public static void switchToWindow(int index) {
        ArrayList<String> windows = new ArrayList<>(getDriver().getWindowHandles());
        getDriver().switchTo().window(windows.get(index));
    }

    public static void waitForNewWindowOpened(int expectedNumberOfWindows) {
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(getTimeOut()));
        wait.until(ExpectedConditions.numberOfWindowsToBe(expectedNumberOfWindows));
    }

    public static void moveMouseByOffset(int x, int y) {
        Actions action = new Actions(getDriver());
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

    public static void waitForAngularReady() {
        try {
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(getTimeOut()));
            wait.until(driver -> {
                JavascriptExecutor executor = (JavascriptExecutor) driver;
                Object result = executor.executeScript(
                        "return (window.angular && angular.element(document).injector && angular.element(document).injector()!=null) ? angular.element(document).injector().get('$http').pendingRequests.length === 0 : true;");
                return Boolean.TRUE.equals(result);
            });
        } catch (Exception ignored) {
        }
    }

    public static boolean waitForCondition(Callable<Boolean> conditionEvaluator, Duration interval, Duration timeout) {
        Wait<WebDriver> wait = new FluentWait<>(getDriver()).withTimeout(timeout).pollingEvery(interval);
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
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(getTimeOut()));
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
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(getTimeOut()));
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
        getDriver().manage().window().setSize(new Dimension(width, height));
    }

    public static void waitForEventTriggered(String eventRegex) {
        waitForEventTriggered(eventRegex, getTimeOut());
    }

    public static void waitForEventTriggered(String eventRegex, int inputTimeOut) {
        List<Map<String, Object>> xmlHttpRequestList = getXmlHttpRequestList();
        double interval = 0.5;
        double timeOut = inputTimeOut;
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

    public static int getTimeOut() {
        long ms = Config.getTimeout();
        int seconds = (int) Math.max(1, ms / 1000);
        return seconds;
    }

    public static void waitForAutoScrollingStopped() {
        delay(1);
    }

    public static void navigateTo(String url) {
        getDriver().get(url);
    }
}
