package org.example.core.element;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.core.element.util.DriverUtils;
import org.example.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.example.core.element.util.DriverUtils.getWebDriver;

@Slf4j
public class ElementWrapperWrapper implements IElementWrapper {
    
    protected By byLocator;
    
    public ElementWrapperWrapper(By byLocator) {
        this.byLocator = byLocator;
    }
    
    public ElementWrapperWrapper(String locator, Object... args) {
        // Default to xpath if type not specified
        this(locator, "xpath", args);
    }
    
    public ElementWrapperWrapper(String locator, String locatorType, Object... args) {
        String formattedLocator = String.format(locator, args);
        this.byLocator = createBy(locatorType, formattedLocator);
    }
    
    private By createBy(String locatorType, String formattedLocator) {
        switch (locatorType.toLowerCase()) {
            case "xpath":
                return By.xpath(formattedLocator);
            case "id":
                return By.id(formattedLocator);
            case "cssselector":
                return By.cssSelector(formattedLocator);
            case "classname":
                return By.className(formattedLocator);
            case "name":
                return By.name(formattedLocator);
            case "tagname":
                return By.tagName(formattedLocator);
            case "linktext":
                return By.linkText(formattedLocator);
            case "partiallinktext":
                return By.partialLinkText(formattedLocator);
            default:
                return By.xpath(formattedLocator);
        }
    }
    
    @Override
    public By getLocator() {
        return this.byLocator;
    }
    
    @Override
    public WebElement getElement() {
        try {
            return getWebDriver().findElement(getLocator());
        } catch (StaleElementReferenceException e) {
            log.error("StaleElementReferenceException '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            return getElement();
        }
    }
    
    @Override
    public List<WebElement> getElements() {
        return getWebDriver().findElements(getLocator());
    }
    
    // Helper method for auto wait
    private WebDriverWait getWait() {
        return new WebDriverWait(getWebDriver(), DriverUtils.getTimeOut());
    }
    
    protected JavascriptExecutor jsExecutor() {
        return (JavascriptExecutor) getWebDriver();
    }
    
    // ========== ACTIONS ==========
    
    @Override
    public void click() {
        click(1);
    }
    
    @Override
    public void click(int times) {
        if (times <= 0) return;
        
        int attemptsLeft = times;
        Exception lastException = null;
        
        while (attemptsLeft > 0) {
            try {
                if (!isVisible()) {
                    waitForDisplay(DriverUtils.getTimeOut());
                }
                
                scrollElementToCenterScreen();
                waitForElementClickable(DriverUtils.getTimeOut());
                
                new Actions(getWebDriver())
                        .moveToElement(getElement())
                        .pause(Duration.ofMillis(100))
                        .click()
                        .build()
                        .perform();
                return;
            } catch (Exception e) {
                lastException = e;
                String msg = e.getMessage() == null ? "" : e.getMessage().split("\n")[0];
                
                boolean intercepted = msg.contains("Other element would receive the click")
                        || msg.contains("Element is not clickable at point")
                        || msg.contains("element click intercepted");
                
                attemptsLeft--;
                
                if (!intercepted) {
                    clickByJs();
                    return;
                }
                
                if (attemptsLeft == 0) {
                    try {
                        clickByJs();
                        return;
                    } catch (Exception jsEx) {
                        throw new RuntimeException("Click failed after retries on: " + getLocator(), lastException);
                    }
                }
                
                DriverUtils.delay(0.5);
            }
        }
        
        throw new RuntimeException("Click failed after retries on: " + getLocator(), lastException);
    }
    
    @Override
    public void click(int x, int y) {
        try {
            WebElement element = getWait().until(ExpectedConditions.elementToBeClickable(getLocator()));
            new Actions(getWebDriver()).moveToElement(element, x, y).click().build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void clickByJs() {
        try {
            ((JavascriptExecutor) getWebDriver()).executeScript("arguments[0].click();", getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void doubleClick() {
        try {
            log.debug("Double click on {}", getLocator().toString());
            WebElement element = getWait().until(ExpectedConditions.elementToBeClickable(getLocator()));
            new Actions(getWebDriver()).doubleClick(element).build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void setText(String text) {
        try {
            WebElement element = getWait().until(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            element.sendKeys(text);
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void clear() {
        try {
            WebElement element = getWait().until(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            element.clear();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void enter(CharSequence... value) {
        try {
            WebElement element = getWait().until(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            element.sendKeys(value);
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void sendKeys(Keys key) {
        try {
            WebElement element = getWait().until(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            element.sendKeys(key);
        } catch (Exception e) {
            log.error("Has error sending key to control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void submit() {
        try {
            WebElement element = getWait().until(ExpectedConditions.elementToBeClickable(getLocator()));
            element.submit();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void focus() {
        try {
            DriverUtils.execJavaScript("arguments[0].focus();", getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void dragAndDrop(int xOffset, int yOffset) {
        try {
            Actions actions = new Actions(getWebDriver());
            actions.dragAndDropBy(getElement(), xOffset, yOffset).build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void dragAndDrop(IElementWrapper target) {
        try {
            Actions actions = new Actions(getWebDriver());
            actions.dragAndDrop(getElement(), target.getElement()).build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void moveTo() {
        try {
            new Actions(getWebDriver()).moveToElement(getElement()).build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void moveTo(int x, int y) {
        try {
            new Actions(getWebDriver()).moveToElement(getElement(), x, y).build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void moveToCenter() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", getElement());
            new Actions(getWebDriver()).moveToElement(getElement()).build().perform();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void mouseHoverJScript() {
        try {
            String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
            ((JavascriptExecutor) getWebDriver()).executeScript(mouseOverScript, getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void setAttributeJS(String attributeName, String value) {
        try {
            log.debug("Set attribute for {}", getLocator().toString());
            ((JavascriptExecutor) getWebDriver())
                    .executeScript(String.format("arguments[0].setAttribute('%s','%s');", attributeName, value),
                            getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void checkCheckBoxByJs() {
        try {
            log.debug("Check checkbox by JS for {}", getLocator().toString());
            ((JavascriptExecutor) getWebDriver())
                    .executeScript("arguments[0].checked=true; arguments[0].dispatchEvent(new Event('change'));", 
                            getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    // ========== SCROLLS ==========
    
    @Override
    public void scrollElementToCenterScreen() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void scrollToView() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView(true);", getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public void scrollToView(int offsetX, int offsetY) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            String script = String.format(
                    "arguments[0].scrollIntoView(true); window.scrollBy(%d, %d);", 
                    offsetX, offsetY);
            js.executeScript(script, getElement());
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    // ========== GETTERS ==========
    
    @Override
    public String getText() {
        try {
            return getElement().getText();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public String getValue() {
        try {
            return getElement().getAttribute("value");
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public String getAttribute(String attributeName) {
        try {
            return getElement().getAttribute(attributeName);
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public String getClassName() {
        return getAttribute("class");
    }
    
    @Override
    public String getTagName() {
        try {
            return getElement().getTagName();
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public WebElement getChildElement(String xpath) {
        try {
            return getElement().findElement(By.xpath(xpath));
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    @Override
    public List<WebElement> getChildElements() {
        return getChildElements("./*");
    }
    
    @Override
    public List<WebElement> getChildElements(String xpath) {
        try {
            return getElement().findElements(By.xpath(xpath));
        } catch (Exception e) {
            log.error("Has error with control '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw e;
        }
    }
    
    // ========== CHECKS ==========
    
    @Override
    public boolean isVisible() {
        return isVisible(DriverUtils.getTimeOut());
    }
    
    @Override
    public boolean isVisible(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            return WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> {
                try {
                    return e.isDisplayed();
                } catch (StaleElementReferenceException ex) {
                    return false;
                }
            }, actualTimeout, log);
        } catch (Exception e) {
            log.debug("isVisible() error for locator '{}': {}", getLocator(), e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean isEnabled() {
        try {
            log.debug("is control enabled or not: {}", getLocator().toString());
            return getElement().isEnabled();
        } catch (Exception e) {
            log.error("IsEnabled: Has error with control '{}': {}", getLocator().toString(),
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            return false;
        }
    }
    
    @Override
    public boolean isSelected() {
        try {
            log.debug("is control selected or not: {}", getLocator().toString());
            return getElement().isSelected();
        } catch (Exception e) {
            log.error("IsSelected: Has error with control '{}': {}", getLocator().toString(),
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            return false;
        }
    }
    
    @Override
    public boolean isClickable() {
        Duration timeout = DriverUtils.getTimeOut();
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            return WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> {
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
    
    @Override
    public boolean isExist() {
        return isExist(DriverUtils.getTimeOut());
    }
    
    @Override
    public boolean isExist(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            return WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> true, actualTimeout, log);
        } catch (Exception e) {
            log.debug("isExist() - Exception for locator '{}': {}", getLocator(), e.getMessage());
            return false;
        }
    }
    
    // ========== WAITS ==========
    
    @Override
    public void waitForVisibility() {
        waitForVisibility(DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForVisibility(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> {
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
                    actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForVisibility timeout after {} seconds for control '{}': {}", 
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }
    
    @Override
    public void waitForElementVisible() {
        waitForElementVisible(DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForElementVisible(Duration timeout) {
        waitForVisibility(timeout);
    }
    
    @Override
    public void waitForElementClickable() {
        waitForElementClickable(DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForElementClickable(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> {
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
                    actualTimeout.getSeconds(), getLocator().toString());
            log.error("WaitForElementClickable timeout after {} seconds for control '{}': {}", 
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }
    
    @Override
    public void waitForDisplay() {
        waitForDisplay(DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForDisplay(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> {
            try {
                return e.isDisplayed();
            } catch (NoSuchElementException ex) {
                return false;
            } catch (StaleElementReferenceException ex) {
                return false;
            }
        }, actualTimeout, log);
        if (!ok) {
            String msg = "Element not displayed after " + actualTimeout.getSeconds() + " seconds: " + getLocator().toString();
            log.error("waitForDisplay timeout after {} seconds for control '{}': {}", 
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }
    
    @Override
    public void waitForInvisibility() {
        waitForInvisibility(DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForInvisibility(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        WebDriver driver = getWebDriver();
        boolean ok = WaitUtils.waitForCondition(driver, d -> {
            try {
                List<WebElement> els = d.findElements(getLocator());
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
            String msg = "waitForInvisibility timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.warn("waitForInvisibility timeout after {} seconds for control '{}'. Throwing.", 
                    actualTimeout.getSeconds(), getLocator().toString());
            throw new RuntimeException(msg);
        }
    }
    
    @Override
    public void waitForDisappear() {
        waitForDisappear(DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForDisappear(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        WebDriver driver = getWebDriver();
        
        boolean success = WaitUtils.waitForCondition(driver, d -> {
            try {
                List<WebElement> els = d.findElements(getLocator());
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
            String msg = "Element still visible after " + actualTimeout.getSeconds() + " seconds: " + getLocator().toString();
            log.warn("Element '{}' still visible after {} seconds", getLocator().toString(), actualTimeout.getSeconds());
            throw new RuntimeException(msg);
        }
    }
    
    @Override
    public void waitForElementEnabled() {
        waitForElementEnabled(DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForElementEnabled(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> {
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
                    actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForElementEnabled timeout after {} seconds for control '{}': {}", 
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }
    
    @Override
    public void waitForElementDisabled() {
        waitForElementDisabled(DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForElementDisabled(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> {
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
                    actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForElementDisabled timeout after {} seconds for control '{}': {}", 
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }
    
    @Override
    public void waitForTextToBePresent(String text) {
        waitForTextToBePresent(text, DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForTextToBePresent(String text, Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> {
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
            String msg = "waitForTextToBePresent timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForTextToBePresent: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg);
        }
    }
    
    @Override
    public void waitForTextToBeNotPresent(String text) {
        waitForTextToBeNotPresent(text, DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForTextToBeNotPresent(String text, Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> {
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
            String msg = "waitForTextToBeNotPresent timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForTextToBeNotPresent: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg);
        }
    }
    
    @Override
    public void waitForValuePresentInAttribute(String attribute, String value) {
        waitForValuePresentInAttribute(attribute, value, DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForValuePresentInAttribute(String attribute, String value, Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> {
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
            String msg = "waitForValuePresentInAttribute timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForValuePresentInAttribute: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg);
        }
    }
    
    @Override
    public void waitForValueNotPresentInAttribute(String attribute, String value) {
        waitForValueNotPresentInAttribute(attribute, value, DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForValueNotPresentInAttribute(String attribute, String value, Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        boolean ok = WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> {
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
            String msg = "waitForValueNotPresentInAttribute timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForValueNotPresentInAttribute: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg);
        }
    }
    
    @Override
    public void waitForStalenessOfElement() {
        waitForStalenessOfElement(DriverUtils.getTimeOut());
    }
    
    @Override
    public void waitForStalenessOfElement(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        WebDriver driver = getWebDriver();
        try {
            boolean ok = WaitUtils.waitForCondition(driver, d -> {
                try {
                    List<WebElement> els = d.findElements(getLocator());
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
                String msg = "waitForStalenessOfElement timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
                log.error("waitForStalenessOfElement: Has error with control '{}'", getLocator().toString());
                throw new RuntimeException(msg);
            }
        } catch (Exception e) {
            log.error("waitForStalenessOfElement: Has error with control '{}': {}", 
                    getLocator().toString(), e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw new RuntimeException("waitForStalenessOfElement error for control: " + getLocator().toString(), e);
        }
    }
    
    // ========== OTHER ==========
    
    @Override
    public Select getSelect() {
        return new Select(getElement());
    }
}

