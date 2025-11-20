package org.example.core.element.component;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.core.element.IElement;
import org.example.core.element.util.DriverUtils;
import org.example.utils.WaitUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

import static org.example.core.element.util.DriverUtils.getWebDriver;

@Slf4j
public class ElementWaits {
    
    private final IElement element;
    
    public ElementWaits(IElement element) {
        this.element = element;
    }
    
    public void waitForVisibility() {
        waitForVisibility(DriverUtils.getTimeOut());
    }
    
    public void waitForVisibility(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), element.getLocator(), e -> {
            try {
                return e.isDisplayed();
            } catch (NoSuchElementException ex) {
                return false;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        }, actualTimeout, log);
        if (!ok) {
            String msg = String.format("Element not visible after %d seconds: %s", 
                    actualTimeout.getSeconds(), element.getLocator().toString());
            log.error("waitForVisibility timeout after {} seconds for control '{}': {}", 
                    actualTimeout.getSeconds(), element.getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }
    
    public void waitForElementVisible() {
        waitForElementVisible(DriverUtils.getTimeOut());
    }
    
    public void waitForElementVisible(Duration timeout) {
        waitForVisibility(timeout);
    }
    
    public void waitForElementClickable() {
        waitForElementClickable(DriverUtils.getTimeOut());
    }
    
    public void waitForElementClickable(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), element.getLocator(), e -> {
            try {
                return e.isDisplayed() && e.isEnabled();
            } catch (NoSuchElementException ex) {
                return false;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        }, actualTimeout, log);
        if (!ok) {
            String msg = String.format("Element not clickable after %d seconds: %s", 
                    actualTimeout.getSeconds(), element.getLocator().toString());
            log.error("WaitForElementClickable timeout after {} seconds for control '{}': {}", 
                    actualTimeout.getSeconds(), element.getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }
    
    public void waitForDisplay() {
        waitForDisplay(DriverUtils.getTimeOut());
    }
    
    public void waitForDisplay(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), element.getLocator(), e -> {
            try {
                return e.isDisplayed();
            } catch (NoSuchElementException ex) {
                return false;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        }, actualTimeout, log);
        if (!ok) {
            String msg = "Element not displayed after " + actualTimeout.getSeconds() + " seconds: " + element.getLocator().toString();
            log.error("waitForDisplay timeout after {} seconds for control '{}': {}", 
                    actualTimeout.getSeconds(), element.getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }
    
    public void waitForInvisibility() {
        waitForInvisibility(DriverUtils.getTimeOut());
    }
    
    public void waitForInvisibility(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        WebDriver driver = getWebDriver();
        boolean ok = WaitUtils.waitForCondition(driver, d -> {
            try {
                List<WebElement> els = d.findElements(element.getLocator());
                if (els.isEmpty()) return true;
                for (WebElement el : els) {
                    try {
                        if (el.isDisplayed()) return false;
                    } catch (StaleElementReferenceException sre) {
                        return true;
                    }
                }
                return true;
            } catch (Exception ex) {
                return true;
            }
        }, actualTimeout, log);
        if (!ok) {
            String msg = "waitForInvisibility timeout after " + actualTimeout.getSeconds() + " seconds for control: " + element.getLocator().toString();
            log.warn("waitForInvisibility timeout after {} seconds for control '{}'. Throwing.", 
                    actualTimeout.getSeconds(), element.getLocator().toString());
            throw new RuntimeException(msg);
        } else {
            log.info("Element {} is now invisible or removed from DOM", element.getLocator().toString());
        }
    }
    
    public void waitForDisappear() {
        waitForDisappear(DriverUtils.getTimeOut());
    }
    
    public void waitForDisappear(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        WebDriver driver = getWebDriver();
        log.info("Wait for control to disappear {} with timeout {} seconds", 
                element.getLocator().toString(), actualTimeout.getSeconds());
        
        boolean success = WaitUtils.waitForCondition(driver, d -> {
            try {
                List<WebElement> els = d.findElements(element.getLocator());
                if (els.isEmpty()) return true;
                for (WebElement el : els) {
                    try {
                        if (el.isDisplayed()) return false;
                    } catch (StaleElementReferenceException sre) {
                        return true;
                    }
                }
                return true;
            } catch (Exception ex) {
                return true;
            }
        }, actualTimeout, log);
        
        if (!success) {
            String msg = "Element still visible after " + actualTimeout.getSeconds() + " seconds: " + element.getLocator().toString();
            log.warn("Element '{}' still visible after {} seconds", element.getLocator().toString(), actualTimeout.getSeconds());
            throw new RuntimeException(msg);
        }
    }
    
    public void waitForElementEnabled() {
        waitForElementEnabled(DriverUtils.getTimeOut());
    }
    
    public void waitForElementEnabled(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), element.getLocator(), e -> {
            try {
                return e.isEnabled();
            } catch (NoSuchElementException ex) {
                return false;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        }, actualTimeout, log);
        if (!ok) {
            String msg = String.format("Element not enabled after %d seconds: %s", 
                    actualTimeout.getSeconds(), element.getLocator().toString());
            log.error("waitForElementEnabled timeout after {} seconds for control '{}': {}", 
                    actualTimeout.getSeconds(), element.getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }
    
    public void waitForElementDisabled() {
        waitForElementDisabled(DriverUtils.getTimeOut());
    }
    
    public void waitForElementDisabled(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), element.getLocator(), e -> {
            try {
                return !e.isEnabled();
            } catch (NoSuchElementException ex) {
                return false;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        }, actualTimeout, log);
        if (!ok) {
            String msg = String.format("Element not disabled after %d seconds: %s", 
                    actualTimeout.getSeconds(), element.getLocator().toString());
            log.error("waitForElementDisabled timeout after {} seconds for control '{}': {}", 
                    actualTimeout.getSeconds(), element.getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }
    
    public void waitForTextToBePresent(String text) {
        waitForTextToBePresent(text, DriverUtils.getTimeOut());
    }
    
    public void waitForTextToBePresent(String text, Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), element.getLocator(), e -> {
            try {
                String t = e.getText();
                return t != null && t.contains(text);
            } catch (NoSuchElementException ex) {
                return false;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        }, actualTimeout, log);
        if (!ok) {
            String msg = "waitForTextToBePresent timeout after " + actualTimeout.getSeconds() + " seconds for control: " + element.getLocator().toString();
            log.error("waitForTextToBePresent: Has error with control '{}'", element.getLocator().toString());
            throw new RuntimeException(msg);
        }
    }
    
    public void waitForTextToBeNotPresent(String text) {
        waitForTextToBeNotPresent(text, DriverUtils.getTimeOut());
    }
    
    public void waitForTextToBeNotPresent(String text, Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), element.getLocator(), e -> {
            try {
                String t = e.getText();
                return t == null || !t.contains(text);
            } catch (NoSuchElementException ex) {
                return true;
            } catch (StaleElementReferenceException ex) {
                return true;
            }
        }, actualTimeout, log);
        if (!ok) {
            String msg = "waitForTextToBeNotPresent timeout after " + actualTimeout.getSeconds() + " seconds for control: " + element.getLocator().toString();
            log.error("waitForTextToBeNotPresent: Has error with control '{}'", element.getLocator().toString());
            throw new RuntimeException(msg);
        }
    }
    
    public void waitForValuePresentInAttribute(String attribute, String value) {
        waitForValuePresentInAttribute(attribute, value, DriverUtils.getTimeOut());
    }
    
    public void waitForValuePresentInAttribute(String attribute, String value, Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), element.getLocator(), e -> {
            try {
                String attr = e.getAttribute(attribute);
                return attr != null && attr.contains(value);
            } catch (NoSuchElementException ex) {
                return false;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        }, actualTimeout, log);
        if (!ok) {
            String msg = "waitForValuePresentInAttribute timeout after " + actualTimeout.getSeconds() + " seconds for control: " + element.getLocator().toString();
            log.error("waitForValuePresentInAttribute: Has error with control '{}'", element.getLocator().toString());
            throw new RuntimeException(msg);
        }
    }
    
    public void waitForValueNotPresentInAttribute(String attribute, String value) {
        waitForValueNotPresentInAttribute(attribute, value, DriverUtils.getTimeOut());
    }
    
    public void waitForValueNotPresentInAttribute(String attribute, String value, Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), element.getLocator(), e -> {
            try {
                String attr = e.getAttribute(attribute);
                return attr == null || !attr.contains(value);
            } catch (NoSuchElementException ex) {
                return true;
            } catch (StaleElementReferenceException ex) {
                return true;
            }
        }, actualTimeout, log);
        if (!ok) {
            String msg = "waitForValueNotPresentInAttribute timeout after " + actualTimeout.getSeconds() + " seconds for control: " + element.getLocator().toString();
            log.error("waitForValueNotPresentInAttribute: Has error with control '{}'", element.getLocator().toString());
            throw new RuntimeException(msg);
        }
    }
    
    public void waitForStalenessOfElement() {
        waitForStalenessOfElement(DriverUtils.getTimeOut());
    }
    
    public void waitForStalenessOfElement(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        WebDriver driver = getWebDriver();
        try {
            log.info("Wait for control staleness {}", element.getLocator().toString());
            boolean ok = WaitUtils.waitForCondition(driver, d -> {
                try {
                    List<WebElement> els = d.findElements(element.getLocator());
                    if (els.isEmpty()) return true;
                    try {
                        els.get(0).isDisplayed();
                        return false;
                    } catch (StaleElementReferenceException se) {
                        return true;
                    }
                } catch (NoSuchElementException ne) {
                    return true;
                }
            }, actualTimeout, log);
            if (!ok) {
                String msg = "waitForStalenessOfElement timeout after " + actualTimeout.getSeconds() + " seconds for control: " + element.getLocator().toString();
                log.error("waitForStalenessOfElement: Has error with control '{}'", element.getLocator().toString());
                throw new RuntimeException(msg);
            }
        } catch (Exception e) {
            log.error("waitForStalenessOfElement: Has error with control '{}': {}", 
                    element.getLocator().toString(), e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw new RuntimeException("waitForStalenessOfElement error for control: " + element.getLocator().toString(), e);
        }
    }
}

