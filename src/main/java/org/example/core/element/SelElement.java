package org.example.core.element;

import lombok.extern.slf4j.Slf4j;
import org.example.utils.DriverUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.pagefactory.ByChained;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static org.example.configure.Config.getMaxActionRetries;
import static org.example.utils.DriverUtils.getWebDriver;

@Slf4j
public class SelElement implements ISelElement {

    protected final By byLocator;

    public SelElement(By byLocator) {
        this.byLocator = byLocator;
    }

    public static SelElement $(By byLocator) {
        return new SelElement(byLocator);
    }

    public static SelElement $(String xpathLocator, Object... args) {
        return new SelElement(xpathLocator, args);
    }

    public SelElement(String xpathLocator, Object... args) {
        this(By.xpath(formatLocator(xpathLocator, args)));
    }

    private static String formatLocator(String locator, Object... args) {
        if (args == null || args.length == 0) {
            return locator;
        }
        return String.format(locator, args);
    }

    private <T> T doWithRetry(String actionName, Supplier<T> action) {
        int maxAttempts = Math.max(1, getMaxActionRetries());
        int attempt = 1;
        RuntimeException lastError = null;

        do {
            try {
                return action.get();
            } catch (StaleElementReferenceException
                     | NoSuchElementException
                     | ElementNotInteractableException
                     | TimeoutException
                     | MoveTargetOutOfBoundsException e) {
                lastError = e;
                log.warn("Retry {}/{} for '{}' due to {}", attempt, maxAttempts, actionName, e.getClass().getSimpleName());
                DriverUtils.delay(0.3);
                attempt++;
            }
        } while (attempt <= maxAttempts);

        throw new RuntimeException("Failed after " + maxAttempts + " attempts for " + actionName + " at " + getLocator(), lastError);
    }

    private void doWithRetry(String actionName, Runnable action) {
        doWithRetry(actionName, () -> {
            action.run();
            return null;
        });
    }

    private <T> T waitFor(ExpectedCondition<T> condition) {
        return waitFor(condition, DriverUtils.getTimeOut());
    }

    private <T> T waitFor(ExpectedCondition<T> condition, Duration timeout) {
        Duration actualTimeout = Objects.requireNonNullElse(timeout, DriverUtils.getTimeOut());
        return new WebDriverWait(getWebDriver(), actualTimeout).until(condition);
    }

    @Override
    public By getLocator() {
        return this.byLocator;
    }

    @Override
    public WebElement getElement() {
        log.debug("Get element for locator: {}", byLocator);
        return doWithRetry("getElement", () -> getWebDriver().findElement(byLocator));
    }

    @Override
    public List<WebElement> getElements() {
        log.debug("Get elements for locator: {}", byLocator);
        return doWithRetry("getElements", () -> getWebDriver().findElements(byLocator));
    }

    protected JavascriptExecutor jsExecutor() {
        return (JavascriptExecutor) getWebDriver();
    }

    // ========== ACTIONS ==========

    @Override
    public void click() {
        doWithRetry("click", () -> {
            log.info("Click on {}", getLocator());
            WebElement element = waitFor(ExpectedConditions.elementToBeClickable(getLocator()));
            element.click();
        });
    }

    @Override
    public void click(int x, int y) {
        doWithRetry("click(offset)", () -> {
            log.info("Click at offset ({}, {}) on {}", x, y, getLocator());
            WebElement element = waitFor(ExpectedConditions.elementToBeClickable(getLocator()));
            new Actions(getWebDriver()).moveToElement(element, x, y).click().build().perform();
        });
    }

    @Override
    public void clickByJs() {
        doWithRetry("clickByJs", () -> {
            log.info("Click by JS on {}", getLocator());
            WebElement element = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            jsExecutor().executeScript("arguments[0].click();", element);
        });
    }

    @Override
    public void doubleClick() {
        doWithRetry("doubleClick", () -> {
            log.info("Double click on {}", getLocator());
            WebElement element = waitFor(ExpectedConditions.elementToBeClickable(getLocator()));
            new Actions(getWebDriver()).doubleClick(element).build().perform();
        });
    }

    @Override
    public void setText(String text) {
        doWithRetry("setText", () -> {
            log.info("Set text '{}' on {}", text, getLocator());
            WebElement element = waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            element.clear();
            element.sendKeys(text);
        });
    }

    @Override
    public void clear() {
        doWithRetry("clear", () -> {
            log.info("Clear text on {}", getLocator());
            WebElement element = waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()));
            element.clear();
        });
    }

    @Override
    public void submit() {
        doWithRetry("submit", () -> {
            log.info("Submit on {}", getLocator());
            WebElement element = waitFor(ExpectedConditions.elementToBeClickable(getLocator()));
            element.submit();
        });
    }

    @Override
    public void focus() {
        doWithRetry("focus", () -> {
            log.info("Focus on {}", getLocator());
            WebElement element = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            DriverUtils.execJavaScript("arguments[0].focus();", element);
        });
    }

    @Override
    public void dragAndDrop(int xOffset, int yOffset) {
        doWithRetry("dragAndDrop(offset)", () -> {
            log.info("Drag and drop by offset ({}, {}) on {}", xOffset, yOffset, getLocator());
            WebElement element = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            new Actions(getWebDriver()).dragAndDropBy(element, xOffset, yOffset).build().perform();
        });
    }

    @Override
    public void dragAndDrop(ISelElement target) {
        doWithRetry("dragAndDrop(target)", () -> {
            log.info("Drag element {} to target {}", getLocator(), target.getLocator());
            WebElement sourceElement = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            WebElement targetElement = waitFor(ExpectedConditions.presenceOfElementLocated(target.getLocator()));
            new Actions(getWebDriver()).dragAndDrop(sourceElement, targetElement).build().perform();
        });
    }

    @Override
    public void moveTo() {
        doWithRetry("moveTo", () -> {
            log.info("Move to {}", getLocator());
            WebElement element = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            new Actions(getWebDriver()).moveToElement(element).build().perform();
        });
    }

    @Override
    public void moveTo(int x, int y) {
        doWithRetry("moveTo(offset)", () -> {
            log.info("Move to offset ({}, {}) on {}", x, y, getLocator());
            WebElement element = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            new Actions(getWebDriver()).moveToElement(element, x, y).build().perform();
        });
    }

    @Override
    public void moveToCenter() {
        doWithRetry("moveToCenter", () -> {
            log.info("Move to center of {}", getLocator());
            WebElement element = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            jsExecutor().executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
            new Actions(getWebDriver()).moveToElement(element).build().perform();
        });
    }

    @Override
    public void mouseHoverJScript() {
        doWithRetry("mouseHoverJScript", () -> {
            log.info("Mouse hover by JS on {}", getLocator());
            WebElement element = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            String script = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
            jsExecutor().executeScript(script, element);
        });
    }

    @Override
    public void setAttributeJS(String attributeName, String value) {
        doWithRetry("setAttributeJS", () -> {
            log.info("Set attribute '{}'='{}' for {}", attributeName, value, getLocator());
            WebElement element = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            jsExecutor().executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);", element, attributeName, value);
        });
    }

    @Override
    public void checkCheckBoxByJs() {
        doWithRetry("checkCheckBoxByJs", () -> {
            log.info("Check checkbox by JS for {}", getLocator());
            WebElement element = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            jsExecutor().executeScript("arguments[0].checked=true; arguments[0].dispatchEvent(new Event('change'));", element);
        });
    }

    // ========== SCROLLS ==========

    @Override
    public void scrollElementToCenterScreen() {
        doWithRetry("scrollElementToCenterScreen", () -> {
            log.info("Scroll element to center of screen: {}", getLocator());
            WebElement element = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            jsExecutor().executeScript("arguments[0].scrollIntoView({block: 'center', behavior: 'smooth'});", element);
        });
    }

    @Override
    public void scrollToView() {
        doWithRetry("scrollToView", () -> {
            log.info("Scroll element into view: {}", getLocator());
            WebElement element = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            jsExecutor().executeScript("arguments[0].scrollIntoView(true);", element);
        });
    }

    @Override
    public void scrollToView(int offsetX, int offsetY) {
        doWithRetry("scrollToView(offset)", () -> {
            log.info("Scroll element into view with offset ({}, {}): {}", offsetX, offsetY, getLocator());
            WebElement element = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            String script = String.format("arguments[0].scrollIntoView(true); window.scrollBy(%d, %d);", offsetX, offsetY);
            jsExecutor().executeScript(script, element);
        });
    }

    // ========== GETTERS ==========

    @Override
    public String getText() {
        log.info("Get text for locator: {}", getLocator());
        return doWithRetry("getText", () -> waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator())).getText());
    }

    @Override
    public String waitForText(Duration timeout) {
        log.info("Wait for text for locator: {}", getLocator());
        return waitFor(driver -> {
            WebElement element = driver.findElement(getLocator());
            if (!element.isDisplayed()) {
                return null;
            }
            return element.getText();
        }, timeout);
    }

    @Override
    public String waitForValue(Duration timeout) {
        return waitFor(driver -> {
            WebElement element = driver.findElement(getLocator());
            if (!element.isDisplayed()) {
                return null;
            }
            return element.getAttribute("value");
        }, timeout);
    }

    @Override
    public String waitForAttribute(String attributeName, Duration timeout) {
        return waitFor(driver -> {
            WebElement element = driver.findElement(getLocator());
            if (!element.isDisplayed()) {
                return null;
            }
            return element.getAttribute(attributeName);
        }, timeout);
    }

    @Override
    public String waitForClassName(Duration timeout) {
        return waitForAttribute("class", timeout);
    }

    @Override
    public String waitForTagName(Duration timeout) {
        return waitFor(driver -> {
            WebElement element = driver.findElement(getLocator());
            if (!element.isDisplayed()) {
                return null;
            }
            return element.getTagName();
        }, timeout);
    }

    @Override
    public int waitForCount(Duration timeout) {
        return waitFor(driver -> driver.findElements(getLocator()).size(), timeout);
    }

    @Override
    public String getValue() {
        log.info("Get value for locator: {}", getLocator());
        return doWithRetry("getValue", () -> waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator())).getAttribute("value"));
    }

    @Override
    public String getAttribute(String attributeName) {
        log.info("Get attribute '{}' for locator: {}", attributeName, getLocator());
        return doWithRetry("getAttribute", () -> waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator())).getAttribute(attributeName));
    }

    @Override
    public String getClassName() {
        return doWithRetry("getClassName", () -> waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator())).getAttribute("class"));
    }

    @Override
    public String getTagName() {
        return doWithRetry("getTagName", () -> waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator())).getTagName());
    }

    @Override
    public WebElement getChildElement(String xpath) {
        return doWithRetry("getChildElement", () -> waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator())).findElement(buildChildLocator(xpath)));
    }

    @Override
    public List<WebElement> getChildElements() {
        return getChildElements("./*");
    }

    @Override
    public List<WebElement> getChildElements(String xpath) {
        return doWithRetry("getChildElements", () -> waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator())).findElements(buildChildLocator(xpath)));
    }

    // ========== CHECKS ==========

    @Override
    public boolean waitForVisible(Duration timeout) {
        try {
            waitFor(ExpectedConditions.visibilityOfElementLocated(getLocator()), timeout);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean waitForEnabled(Duration timeout) {
        try {
            return waitFor(driver -> {
                WebElement element = driver.findElement(getLocator());
                return (element.isDisplayed() && element.isEnabled()) ? Boolean.TRUE : null;
            }, timeout);
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean waitForExist(Duration timeout) {
        try {
            return waitFor(driver -> !driver.findElements(getLocator()).isEmpty() ? Boolean.TRUE : null, timeout);
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean waitForSelected(Duration timeout) {
        try {
            return waitFor(driver -> {
                WebElement element = driver.findElement(getLocator());
                return element.isSelected() ? Boolean.TRUE : null;
            }, timeout);
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean waitForClickable(Duration timeout) {
        try {
            waitFor(ExpectedConditions.elementToBeClickable(getLocator()), timeout);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean waitForEditable(Duration timeout) {
        try {
            return waitFor(driver -> {
                WebElement element = driver.findElement(getLocator());
                boolean readOnly = Boolean.parseBoolean(element.getAttribute("readonly"));
                boolean disabled = Boolean.parseBoolean(element.getAttribute("disabled"));
                return (element.isDisplayed() && element.isEnabled() && !readOnly && !disabled) ? Boolean.TRUE : null;
            }, timeout);
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean waitForReadOnly(Duration timeout) {
        try {
            return waitFor(driver -> {
                WebElement element = driver.findElement(getLocator());
                boolean readOnly = Boolean.parseBoolean(element.getAttribute("readonly"));
                boolean disabled = Boolean.parseBoolean(element.getAttribute("disabled"));
                return (readOnly || disabled) ? Boolean.TRUE : null;
            }, timeout);
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public boolean isVisible() {
        return waitForVisible(Duration.ofMillis(300));
    }

    @Override
    public boolean isEnabled() {
        return waitForEnabled(Duration.ofMillis(300));
    }

    @Override
    public boolean isSelected() {
        return waitForSelected(Duration.ofMillis(300));
    }

    @Override
    public boolean isClickable() {
        return waitForClickable(Duration.ofMillis(300));
    }

    @Override
    public boolean isExist() {
        return waitForExist(Duration.ofMillis(300));
    }

    // ========== SELECT ==========

    @Override
    public Select getSelect() {
        return new Select(getElement());
    }

    private WebElement findSelectElement(WebElement root) {
        if ("select".equalsIgnoreCase(root.getTagName())) {
            return root;
        }

        List<By> selectLocators = List.of(
                By.xpath(".//select[@rel='items']"),
                By.xpath(".//select[contains(@class, 'select')]"),
                By.xpath(".//select[@id]"),
                By.xpath(".//select[@name]"),
                By.xpath(".//select")
        );

        for (By locator : selectLocators) {
            List<WebElement> selects = root.findElements(locator);
            if (!selects.isEmpty()) {
                WebElement select = selects.get(0);
                if (select.isDisplayed() && select.isEnabled()) {
                    return select;
                }
            }
        }
        throw new NoSuchElementException("No <select> element found inside element: " + getLocator());
    }

    public void selectOptionByText(String text) {
        doWithRetry("selectOptionByText", () -> {
            WebElement root = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            new Select(findSelectElement(root)).selectByVisibleText(text);
        });
    }

    public void selectOptionByValue(String value) {
        doWithRetry("selectOptionByValue", () -> {
            WebElement root = waitFor(ExpectedConditions.presenceOfElementLocated(getLocator()));
            new Select(findSelectElement(root)).selectByValue(value);
        });
    }

    // ========== HELPERS ==========

    private By buildChildLocator(String xpath) {
        return new ByChained(By.xpath("."), By.xpath(xpath));
    }
}

