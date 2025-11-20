package org.example.core.element.component;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.core.element.IElement;
import org.example.core.element.util.DriverUtils;
import org.example.utils.WaitUtils;
import org.openqa.selenium.StaleElementReferenceException;

import java.time.Duration;

import static org.example.core.element.util.DriverUtils.getWebDriver;

@Slf4j
public class ElementChecks {
    
    private final IElement element;
    
    public ElementChecks(IElement element) {
        this.element = element;
    }
    
    public boolean isVisible() {
        return isVisible(DriverUtils.getTimeOut());
    }
    
    public boolean isVisible(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            return WaitUtils.waitForCondition(getWebDriver(), element.getLocator(), e -> {
                try {
                    return e.isDisplayed();
                } catch (StaleElementReferenceException ex) {
                    return false;
                }
            }, actualTimeout, log);
        } catch (Exception e) {
            log.debug("isVisible() error for locator '{}': {}", element.getLocator(), e.getMessage());
            return false;
        }
    }
    
    public boolean isEnabled() {
        try {
            log.debug("is control enabled or not: {}", element.getLocator().toString());
            return element.getElement().isEnabled();
        } catch (Exception e) {
            log.error("IsEnabled: Has error with control '{}': {}", element.getLocator().toString(),
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            return false;
        }
    }
    
    public boolean isSelected() {
        try {
            log.debug("is control selected or not: {}", element.getLocator().toString());
            return element.getElement().isSelected();
        } catch (Exception e) {
            log.error("IsSelected: Has error with control '{}': {}", element.getLocator().toString(),
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            return false;
        }
    }
    
    public boolean isClickable() {
        Duration timeout = DriverUtils.getTimeOut();
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            return WaitUtils.waitForCondition(getWebDriver(), element.getLocator(), e -> {
                try {
                    return e.isDisplayed() && e.isEnabled();
                } catch (StaleElementReferenceException ex) {
                    return false;
                }
            }, actualTimeout, log);
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isExist() {
        return isExist(DriverUtils.getTimeOut());
    }
    
    public boolean isExist(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            return WaitUtils.waitForCondition(getWebDriver(), element.getLocator(), e -> true, actualTimeout, log);
        } catch (Exception e) {
            log.debug("isExist() - Exception for locator '{}': {}", element.getLocator(), e.getMessage());
            return false;
        }
    }
}

