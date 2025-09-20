package org.example.core.control.base.imp;

import lombok.extern.slf4j.Slf4j;
import org.example.core.control.base.IClickable;
import org.example.core.control.util.DriverUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@Slf4j
public class Clickable extends BaseControl implements IClickable {

    public Clickable(String locator) {
        super(locator);
    }

    public Clickable(By locator) {
        super(locator);
    }

    public Clickable(String locator, Object... value) {
        super(locator, value);
    }

    public Clickable(BaseControl parent, String locator) {
        super(parent, locator);
    }

    public Clickable(BaseControl parent, By locator) {
        super(parent, locator);
    }

    public Clickable(BaseControl parent, String locator, Object... value) {
        super(parent, locator, value);
    }

    @Override
    public void click() {
        click(1);
    }

    @Override
    public void click(int times) {
        if (times > 0) {
            try {
                log.debug("Click on {}", getLocator().toString());

                if (!isVisible()) {
                    waitForDisplay(DriverUtils.getTimeOut());
                }
                scrollElementToCenterScreen();
                waitForElementClickable(DriverUtils.getTimeOut());

                new Actions(getDriver()).moveToElement(getElement()).pause(Duration.ofMillis(100)).click().build().perform();
                return;
            } catch (Exception firstEx) {
                String errorMsg = firstEx.getMessage() == null ? "" : firstEx.getMessage().split("\n")[0];
                boolean intercepted = errorMsg.contains("Other element would receive the click")
                        || errorMsg.contains("Element is not clickable at point")
                        || errorMsg.contains("element click intercepted");

                if (intercepted && times > 0) {
                    times--;
                    if (times == 0) {
                        log.error("Click intercepted on '{}': {}", getLocator().toString(), errorMsg);
                        System.out.println("[CLICK-BY-JS] " + getLocator().toString());
                        clickByJs(); // Use JS as fallback
                        return;
                    }

                    DriverUtils.delay(0.5); // Reduce delay
                    try {
                        scrollElementToCenterScreen();
                        System.out.println("[CLICK-RETRY] " + getLocator().toString());
                        getElement().click();
                        return;
                    } catch (Exception secondEx) {
                        if (times == 1) {
                            System.out.println("[CLICK-BY-JS] " + getLocator().toString());
                            clickByJs();
                            return;
                        }
                        click(times);
                    }
                } else {
                    log.error("Click error on '{}': {}", getLocator().toString(), errorMsg);
                    // Try JS click as last resort
                    try {
                        System.out.println("[CLICK-BY-JS] " + getLocator().toString());
                        clickByJs();
                    } catch (Exception jsEx) {
                        throw firstEx;
                    }
                }
            }
        }
    }

    @Override
    public void click(int x, int y) {
        try {
            log.debug(String.format("Click on %s", getLocator().toString()));
            new Actions(getDriver()).moveToElement(getElement(), x, y).click().build().perform();
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }
    }

    @Override
    public void clickByJs() {
        try {
            log.debug(String.format("Click by js on %s", getLocator().toString()));
            jsExecutor().executeScript("arguments[0].click();", getElement());
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }
    }

    @Override
    public void doubleClick() {
        try {
            log.debug(String.format("Double click on %s", getLocator().toString()));
            new Actions(getDriver()).doubleClick(getElement()).build().perform();
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }

    }

    @Override
    public void waitForElementClickable() {
        waitForElementClickable(DriverUtils.getTimeOut());
    }
    @Override
    public void waitForElementClickable(int timeOutInSecond) {
        try {
            if (!isVisible()) {
                waitForDisplay(timeOutInSecond);
            }
            log.debug("Wait for element clickable {}", getLocator().toString());
            WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(timeOutInSecond));
            
            // 🔑 Dùng locator thay vì getElement()
            wait.until(ExpectedConditions.elementToBeClickable(getLocator()));
            
            log.debug("Element is clickable: {}", getLocator().toString());
        } catch (Exception e) {
            log.warn("WaitForElementClickable error on '{}': {}", 
                    getLocator().toString(), 
                    e.getMessage().split("\n")[0]);
        }
    }

}