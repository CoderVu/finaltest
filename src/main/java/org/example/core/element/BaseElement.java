package org.example.core.element;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.core.assertion.retry.ElementAssertions;
import org.example.utils.DriverUtils;
import org.example.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import static org.example.configure.Config.getMaxActionRetries;
import static org.example.utils.DriverUtils.getWebDriver;

@Slf4j
public class BaseElement extends ElementAssertions<BaseElement> implements IBaseElement {

    protected final By byLocator;

    public BaseElement(By byLocator) {
        this.byLocator = byLocator;
    }
    public static BaseElement $(By byLocator) {
        return new BaseElement(byLocator);
    }

    public static BaseElement $(String xpathLocator, Object... args) {
        return new BaseElement(xpathLocator, args);
    }

    public BaseElement(String xpathLocator, Object... args) {
        this(By.xpath(formatLocator(xpathLocator, args)));
    }

    private static String formatLocator(String locator, Object... args) {
        if (args == null || args.length == 0) {
            return locator;
        }
        return String.format(locator, args);
    }

    private <T> T doWithRetry(Supplier<T> action) {
        int maxAttempts = Math.max(1, getMaxActionRetries());
        int attempt = 1;
        Exception lastError = null;

        do {
            try {
                return action.get();
            }
            catch (
                    StaleElementReferenceException |
                    NoSuchElementException |
                    ElementNotInteractableException |
                    MoveTargetOutOfBoundsException e
            ) {
                lastError = e;

                log.warn("Retry {}/{} for '{}' due to {}",
                        attempt,
                        maxAttempts,
                        getLocator(),
                        e.getClass().getSimpleName());

                DriverUtils.delay(0.5);
                attempt++;
            }
        } while (attempt <= maxAttempts);

        throw new RuntimeException(
                "Failed after " + maxAttempts + " attempts for " + getLocator(),
                lastError
        );
    }

    private void doWithRetry(Runnable action) {
        doWithRetry(() -> {
            action.run();
            return null;
        });
    }

    @Override
    public By getLocator() {
        return this.byLocator;
    }

    @Override
    public WebElement getElement() {
        log.debug("Get element for locator: {}", byLocator.toString());
        return doWithRetry(() -> getWebDriver().findElement(byLocator));
    }

    @Override
    public List<WebElement> getElements() {
        log.debug("Get elements for locator: {}", byLocator.toString());
        return doWithRetry(() -> getWebDriver().findElements(byLocator));
    }

    protected JavascriptExecutor jsExecutor() {
        return (JavascriptExecutor) getWebDriver();
    }

    // ========== ACTIONS ==========

    @Override
    public void click() {
        doWithRetry(() -> {
            log.info("Click on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(getLocator()));
            element.click();
        });
    }

    @Override
    public void click(int x, int y) {
        doWithRetry(() -> {
            log.info("Click at offset ({}, {}) on {}", x, y, getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(getLocator()));
            new Actions(getWebDriver()).moveToElement(element, x, y).click().build().perform();
        });
    }

    @Override
    public void clickByJs() {
        doWithRetry(() -> {
            log.info("Click by JS on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            ((JavascriptExecutor) getWebDriver()).executeScript("arguments[0].click();", element);
        });
    }

    @Override
    public void doubleClick() {
        doWithRetry(() -> {
            log.info("Double click on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(getLocator()));
            new Actions(getWebDriver()).doubleClick(element).build().perform();
        });
    }

    @Override
    public void setText(String text) {
        doWithRetry(() -> {
            log.info("Set text '{}' on {}", text, getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            element.sendKeys(text);
        });
    }

    @Override
    public void clear() {
        doWithRetry(() -> {
            log.info("Clear text on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            element.clear();
        });
    }

    @Override
    public void enter(CharSequence... value) {
        doWithRetry(() -> {
            log.info("Enter value on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            element.sendKeys(value);
        });
    }

    @Override
    public void sendKeys(Keys key) {
        doWithRetry(() -> {
            log.info("Send key '{}' on {}", key, getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            element.sendKeys(key);
        });
    }

    @Override
    public void submit() {
        doWithRetry(() -> {
            log.info("Submit on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(getLocator()));
            element.submit();
        });
    }

    @Override
    public void focus() {
        doWithRetry(() -> {
            log.info("Focus on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            DriverUtils.execJavaScript("arguments[0].focus();", element);
        });
    }

    @Override
    public void dragAndDrop(int xOffset, int yOffset) {
        doWithRetry(() -> {
            log.info("Drag and drop by offset ({}, {}) on {}", xOffset, yOffset, getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            Actions actions = new Actions(getWebDriver());
            actions.dragAndDropBy(element, xOffset, yOffset).build().perform();
        });
    }

    @Override
    public void dragAndDrop(IBaseElement target) {
        doWithRetry(() -> {
            log.info("Drag element {} to target {}", getLocator().toString(), target.getLocator().toString());
            WebElement sourceElement = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            WebElement targetElement = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(target.getLocator()));
            Actions actions = new Actions(getWebDriver());
            actions.dragAndDrop(sourceElement, targetElement).build().perform();
        });
    }

    @Override
    public void moveTo() {
        doWithRetry(() -> {
            log.info("Move to {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            new Actions(getWebDriver()).moveToElement(element).build().perform();
        });
    }

    @Override
    public void moveTo(int x, int y) {
        doWithRetry(() -> {
            log.info("Move to offset ({}, {}) on {}", x, y, getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            new Actions(getWebDriver()).moveToElement(element, x, y).build().perform();
        });
    }

    @Override
    public void moveToCenter() {
        doWithRetry(() -> {
            log.info("Move to center of {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            new Actions(getWebDriver()).moveToElement(element).build().perform();
        });
    }

    @Override
    public void mouseHoverJScript() {
        doWithRetry(() -> {
            log.info("Mouse hover by JS on {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
            ((JavascriptExecutor) getWebDriver()).executeScript(mouseOverScript, element);
        });
    }

    @Override
    public void setAttributeJS(String attributeName, String value) {
        doWithRetry(() -> {
            log.info("Set attribute '{}'='{}' for {}", attributeName, value, getLocator().toString());
            log.debug("Set attribute for {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            ((JavascriptExecutor) getWebDriver())
                    .executeScript(String.format("arguments[0].setAttribute('%s','%s');", attributeName, value),
                            element);
        });
    }

    @Override
    public void checkCheckBoxByJs() {
        doWithRetry(() -> {
            log.info("Check checkbox by JS for {}", getLocator().toString());
            log.debug("Check checkbox by JS for {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            ((JavascriptExecutor) getWebDriver())
                    .executeScript("arguments[0].checked=true; arguments[0].dispatchEvent(new Event('change'));",
                            element);
        });
    }

    // ========== SCROLLS ==========

    @Override
    public void scrollElementToCenterScreen() {
        doWithRetry(() -> {
            log.info("Scroll element to center of screen: {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", element);
        });
    }

    @Override
    public void scrollToView() {
        doWithRetry(() -> {
            log.info("Scroll element into view: {}", getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            js.executeScript("arguments[0].scrollIntoView(true);", element);
        });
    }

    @Override
    public void scrollToView(int offsetX, int offsetY) {
        doWithRetry(() -> {
            log.info("Scroll element into view with extra offset ({}, {}): {}", offsetX, offsetY, getLocator().toString());
            WebElement element = WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            JavascriptExecutor js = (JavascriptExecutor) getWebDriver();
            String script = String.format(
                    "arguments[0].scrollIntoView(true); window.scrollBy(%d, %d);",
                    offsetX, offsetY);
            js.executeScript(script, element);
        });
    }

    // ========== GETTERS ==========

    @Override
    public String getText() {
        return doWithRetry(() -> {
            log.info("Get text of {}", getLocator().toString());
            try {
                WebElement element = WaitUtils.waitFor(
                        ExpectedConditions.visibilityOfElementLocated(getLocator()),
                        DriverUtils.getTimeOut());
                return element.getText();
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    @Override
    public String getValue() {
        return doWithRetry(() -> {
            log.info("Get value of {}", getLocator().toString());
            try {
                WebElement element = WaitUtils.waitFor(
                        ExpectedConditions.visibilityOfElementLocated(getLocator()),
                        DriverUtils.getTimeOut());
                return element.getAttribute("value");
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    @Override
    public String getAttribute(String attributeName) {
        log.info("Get attribute '{}' of {}", attributeName, getLocator().toString());
        return doWithRetry(() -> {
            try {
                WebElement element = WaitUtils.waitFor(
                        ExpectedConditions.visibilityOfElementLocated(getLocator()),
                        DriverUtils.getTimeOut());
                return element.getAttribute(attributeName);
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    @Override
    public String getClassName() {
        log.info("Get class name of {}", getLocator().toString());
        return doWithRetry(() -> {
            try {
                WebElement element = WaitUtils.waitFor(
                        ExpectedConditions.visibilityOfElementLocated(getLocator()),
                        DriverUtils.getTimeOut());
                return element.getAttribute("class");
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    @Override
    public String getTagName() {
        log.info("Get tag name of {}", getLocator().toString());
        return doWithRetry(() -> {
            try {
                WebElement element = WaitUtils.waitFor(
                        ExpectedConditions.visibilityOfElementLocated(getLocator()),
                        DriverUtils.getTimeOut());
                return element.getTagName();
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    @Override
    public WebElement getChildElement(String xpath) {
        log.info("Get child element '{}' of {}", xpath, getLocator().toString());
        return doWithRetry(() -> {
            try {
                WebElement element = WaitUtils.waitFor(
                        ExpectedConditions.visibilityOfElementLocated(getLocator()),
                        DriverUtils.getTimeOut());
                return element.findElement(buildChildLocator(xpath));
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    @Override
    public List<WebElement> getChildElements() {
        return getChildElements("./*");
    }

    @Override
    public List<WebElement> getChildElements(String xpath) {
        log.info("Get child elements '{}' of {}", xpath, getLocator().toString());
        return doWithRetry(() -> {
            try {
                WebElement element = WaitUtils.waitFor(
                        ExpectedConditions.visibilityOfElementLocated(getLocator()),
                        DriverUtils.getTimeOut());
                return element.findElements(buildChildLocator(xpath));
            } catch (Exception e) {
                log.error("Has error with control '{}': {}", getLocator().toString(),
                        e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
                throw e;
            }
        });
    }

    // ========== CHECKS ==========
    // No doWithRetry here to avoid missing bugs due to retries

    @Override
    public boolean isVisible() {
        return isVisible(DriverUtils.getTimeOut());
    }

    @Override
    public boolean isVisible(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0
                ? timeout : Constants.DEFAULT_TIMEOUT;
        try {
            WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()), actualTimeout);
            return true;
        } catch (TimeoutException e) {
            log.debug("isVisible() timeout for locator '{}': {}", getLocator(), e.getMessage());
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
            WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(getLocator()), actualTimeout);
            return true;
        } catch (TimeoutException e) {
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
            WaitUtils.waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()), actualTimeout);
            return true;
        } catch (TimeoutException e) {
            log.debug("isExist() - timeout for locator '{}'", getLocator());
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
        try {
            WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()), actualTimeout);
        } catch (TimeoutException e) {
            String msg = String.format("Element not visible after %d seconds: %s",
                    actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForVisibility timeout after {} seconds for control '{}': {}",
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg, e);
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
        try {
            WaitUtils.waitFor(ExpectedConditions.elementToBeClickable(getLocator()), actualTimeout);
        } catch (TimeoutException e) {
            String msg = String.format("Element not clickable after %d seconds: %s",
                    actualTimeout.getSeconds(), getLocator().toString());
            log.error("WaitForElementClickable timeout after {} seconds for control '{}': {}",
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg, e);
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
        try {
            WaitUtils.waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "Element not displayed after " + actualTimeout.getSeconds() + " seconds: " + getLocator().toString();
            log.error("waitForDisplay timeout after {} seconds for control '{}': {}",
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg, e);
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
        try {
            WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(getLocator()), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "waitForInvisibility timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.warn("waitForInvisibility timeout after {} seconds for control '{}'. Throwing.",
                    actualTimeout.getSeconds(), getLocator().toString());
            throw new RuntimeException(msg, e);
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
        try {
            WaitUtils.waitFor(ExpectedConditions.invisibilityOfElementLocated(getLocator()), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "Element still visible after " + actualTimeout.getSeconds() + " seconds: " + getLocator().toString();
            log.warn("Element '{}' still visible after {} seconds", getLocator().toString(), actualTimeout.getSeconds());
            throw new RuntimeException(msg, e);
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
        try {
            WaitUtils.waitFor(driver -> {
                try {
                    return driver.findElement(getLocator()).isEnabled();
                } catch (NoSuchElementException | StaleElementReferenceException ex) {
                    return false;
                }
            }, actualTimeout);
        } catch (TimeoutException e) {
            String msg = String.format("Element not enabled after %d seconds: %s",
                    actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForElementEnabled timeout after {} seconds for control '{}': {}",
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg, e);
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
        try {
            WaitUtils.waitFor(driver -> {
                try {
                    return !driver.findElement(getLocator()).isEnabled();
                } catch (NoSuchElementException | StaleElementReferenceException ex) {
                    return false;
                }
            }, actualTimeout);
        } catch (TimeoutException e) {
            String msg = String.format("Element not disabled after %d seconds: %s",
                    actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForElementDisabled timeout after {} seconds for control '{}': {}",
                    actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg, e);
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
        try {
            WaitUtils.waitFor(ExpectedConditions.textToBePresentInElementLocated(getLocator(), text), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "waitForTextToBePresent timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForTextToBePresent: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg, e);
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
        try {
            WaitUtils.waitFor(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementLocated(getLocator(), text)), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "waitForTextToBeNotPresent timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForTextToBeNotPresent: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg, e);
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
        try {
            WaitUtils.waitFor(ExpectedConditions.attributeContains(getLocator(), attribute, value), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "waitForValuePresentInAttribute timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForValuePresentInAttribute: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg, e);
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
        try {
            WaitUtils.waitFor(
                    ExpectedConditions.not(ExpectedConditions.attributeContains(getLocator(), attribute, value)),
                    actualTimeout);
        } catch (TimeoutException e) {
            String msg = "waitForValueNotPresentInAttribute timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForValueNotPresentInAttribute: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg, e);
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
        try {
            WebElement element = getElement();
            WaitUtils.waitFor(ExpectedConditions.stalenessOf(element), actualTimeout);
        } catch (TimeoutException e) {
            String msg = "waitForStalenessOfElement timeout after " + actualTimeout.getSeconds() + " seconds for control: " + getLocator().toString();
            log.error("waitForStalenessOfElement: Has error with control '{}'", getLocator().toString());
            throw new RuntimeException(msg, e);
        } catch (Exception e) {
            log.error("waitForStalenessOfElement: Has error with control '{}': {}",
                    getLocator().toString(), e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            throw new RuntimeException("waitForStalenessOfElement error for control: " + getLocator().toString(), e);
        }
    }

    // ========== SELECT ==========

    @Override
    public Select getSelect() {
        return new Select(getElement());
    }

    // ========== HELPERS ==========

    private By buildChildLocator(String xpath) {
        return new ByChained(By.xpath("."), By.xpath(xpath));
    }

    // ========== ASSERTIONS ==========

    @Override
    protected BaseElement self() {
        return this;
    }

}

