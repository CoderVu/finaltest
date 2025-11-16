package org.example.core.element;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.core.element.util.DriverUtils;
import org.example.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openqa.selenium.support.ui.Select;
import static org.example.core.element.util.DriverUtils.getWebDriver;

@Slf4j
public class Element {

    private String locator;
    private By byLocator;
    private String dynamicLocator;
    private Element parent;

	public Element(String locator) {
        this.locator = locator;
        this.dynamicLocator = locator;
        this.byLocator = getByLocator();
    }

    public Element(By byLocator) {
        this.byLocator = byLocator;
    }

    public Element(String locator, Object... args) {
        this.dynamicLocator = locator;
        this.locator = String.format(dynamicLocator, args);
        this.byLocator = getByLocator();
    }

    public Element(Element parent, String locator) {
        this.locator = locator;
        this.dynamicLocator = locator;
        this.byLocator = getByLocator();
        this.parent = parent;
    }

    public Element(Element parent, By byLocator) {
        this.byLocator = byLocator;
        this.parent = parent;
    }

    public Element(Element parent, String locator, Object... args) {
        this.dynamicLocator = locator;
        this.locator = String.format(dynamicLocator, args);
        this.byLocator = getByLocator();
        this.parent = parent;
    }


    protected JavascriptExecutor jsExecutor() {
        return (JavascriptExecutor) getWebDriver();
    }

    private By getByLocator() {
        String body = this.locator.replaceAll("[\\w\\s]*=(.*)", "$1").trim();
        String type = this.locator.replaceAll("([\\w\\s]*)=.*", "$1").trim();
        switch (type.toLowerCase()) {
            case "css":
                return By.cssSelector(body);
            case "id":
                return By.id(body);
            case "class":
                return By.className(body);
            case "link":
                return By.linkText(body);
            case "xpath":
                return By.xpath(body);
            case "text":
                return By.xpath(String.format("//*[contains(text(), '%s')]", body));
            case "name":
                return By.name(body);
            default:
                return By.xpath(locator);
        }
    }

    private WebElement getParent() {
        return parent.getElement();
    }

    public void dragAndDrop(int xOffset, int yOffset) {
        Actions actions = new Actions(getWebDriver());
        actions.dragAndDropBy(getElement(), xOffset, yOffset).build().perform();
    }

    public void dragAndDrop(Element target) {
        Actions actions = new Actions(getWebDriver());
        actions.dragAndDrop(getElement(), target.getElement()).build().perform();
    }

    public void focus() {
        DriverUtils.execJavaScript("arguments[0].focus();", getElement());
    }

    public String getAttribute(String attributeName) {
        try {
            log.debug(String.format("Get attribute '%s' of element %s", attributeName, getLocator().toString()));
            return getElement().getAttribute(attributeName);
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }

    }

    public WebElement getChildElement(String xpath) {
        return getElement().findElement(By.xpath(xpath));
    }

    public List<WebElement> getChildElements() {
        return getChildElements("./*");
    }

    public List<WebElement> getChildElements(String xpath) {
        return getElement().findElements(By.xpath(xpath));
    }

    public String getClassName() {
        return getAttribute("class");
    }

    public WebElement getElement() {
        WebElement element = null;
        try {
            if (parent != null) {
                WebElement eleParent = parent.getElement();
                element = eleParent.findElement(getLocator());
            } else {
                element = getWebDriver().findElement(getLocator());
            }
            return element;
        } catch (StaleElementReferenceException e) {
            log.error(
                    String.format("StaleElementReferenceException '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            return getElement();
        }
    }

    public List<WebElement> getElements() {
        if (parent != null)
            return parent.getElement().findElements(getLocator());
        return getWebDriver().findElements(getLocator());
    }


    public By getLocator() {
        return this.byLocator;
    }

    public String getLocatorString() {
        return this.locator;
    }

    public String getTagName() {
        return getElement().getTagName();
    }

    public String getText() {
        try {
            log.debug(String.format("Get text of element %s", getLocator().toString()));
            return getElement().getText();
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }
    }

    public void setText(String text) {
        getElement().sendKeys(text);
    }

    public void sendKeys(Keys key) {
        try {
            log.debug("Sending key {} to element {}", key, getLocator().toString());
            getElement().sendKeys(key);
        } catch (Exception e) {
            log.error("Has error sending key to control '{}': {}", getLocator().toString(), e.getMessage().split("\n")[0]);
            throw e;
        }
    }

    public String getValue() {
        try {
            log.debug(String.format("Get value of element %s", getLocator().toString()));
            return getElement().getAttribute("value");
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }

    }

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

    public boolean isDynamicLocator() {
        return this.locator != null && this.locator.toLowerCase().matches("(.*)%[s|d](.*)");
    }

    public boolean isEnabled() {
        try {
            log.debug(String.format("is control enabled or not: %s", getLocator().toString()));
            return getElement().isEnabled();
        } catch (Exception e) {
            log.error(String.format("IsEnabled: Has error with control '%s': %s", getLocator().toString(),
                    e.getMessage().split("\n")[0]));
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
            return WaitUtils.waitForCondition(getWebDriver(), getLocator(), e -> true, actualTimeout, log);
        } catch (Exception e) {
            log.debug("isExist() - Exception for locator '{}': {}", getLocator(), e.getMessage());
            return false;
        }
    }

    @Deprecated
    public boolean isExist(int timeOutInSeconds) {
        return isExist(Duration.ofSeconds(timeOutInSeconds));
    }


    public boolean isSelected() {
        try {
            log.debug(String.format("is control selected or not: %s", getLocator().toString()));
            return getElement().isSelected();
        } catch (Exception e) {
            log.error(String.format("IsSelected: Has error with control '%s': %s", getLocator().toString(),
                    e.getMessage().split("\n")[0]));
            return false;
        }
    }

    public boolean isVisible() {
        return isVisible(DriverUtils.getTimeOut());
    }

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

    @Deprecated
    public boolean isVisible(int timeOutInSeconds) {
        return isVisible(Duration.ofSeconds(timeOutInSeconds));
    }

    public void mouseHoverJScript() {
        String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
        jsExecutor().executeScript(mouseOverScript, getElement());

    }

    public void moveTo() {
        Actions actions = new Actions(getWebDriver());
        actions.moveToElement(getElement()).build().perform();
    }

    public void moveTo(int x, int y) {
        WebElement element = getElement();
        int absX = element.getLocation().x + x;
        int absY = element.getLocation().y + y;

        Actions actions = new Actions(getWebDriver());
        actions.moveByOffset(absX, absY).build().perform();
    }

    public void moveToCenter() {
        WebElement element = getElement();
        int x = element.getLocation().x + element.getSize().width / 2;
        int y = element.getLocation().y + element.getSize().height / 2;

        Actions actions = new Actions(getWebDriver());
        actions.moveByOffset(x, y).build().perform();
    }

    public void scrollElementToCenterScreen() {
        DriverUtils.waitForAutoScrollingStopped();
        String js = "Element.prototype.documentOffsetTop=function(){return this.offsetTop+(this.offsetParent?this.offsetParent.documentOffsetTop():0)};var top=arguments[0].documentOffsetTop()-window.innerHeight/2;window.scrollTo(0,top);";
        DriverUtils.execJavaScript(js, getElement());
        log.info("Scroll element {} to center of screen", getLocator().toString());
    }

    public void scrollToView() {
        try {
            jsExecutor().executeScript("arguments[0].scrollIntoView(true);", getElement());
        } catch (JavascriptException e) {
            WebElement element = getElement();
            int x = element.getRect().x;
            int y = element.getRect().y;
            String js = String.format("window.scrollTo(%s, %s);", x, y);
            jsExecutor().executeScript(js);
        }
    }

    public void scrollToView(int offsetX, int offsetY) {
        try {
            jsExecutor().executeScript("arguments[0].scrollIntoView(true);", getElement());
        } catch (JavascriptException e) {
            WebElement element = getElement();
            int x = element.getRect().x + offsetX;
            int y = element.getRect().y + offsetY;
            String js = String.format("window.scrollTo(%s, %s);", x, y);
            jsExecutor().executeScript(js);
        }
    }

    public void setAttributeJS(String attributeName, String value) {
        try {
            log.debug(String.format("Set attribute for %s", getLocator().toString()));
            jsExecutor().executeScript(String.format("arguments[0].setAttribute('%s','%s');", attributeName, value),
                    getElement());
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }

    }

    public void setDynamicValue(Object... args) {
        this.locator = String.format(this.dynamicLocator, args);
        this.byLocator = getByLocator();
    }

    public void submit() {
        getElement().submit();
    }

    public void waitForDisappear() {
        waitForDisappear(DriverUtils.getTimeOut());
    }

    public void waitForDisappear(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        WebDriver driver = getWebDriver();
        log.info("Wait for control to disappear {} with timeout {} seconds", getLocator().toString(), actualTimeout.getSeconds());

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

    @Deprecated
    public void waitForDisappear(int timeOutInSeconds) {
        waitForDisappear(Duration.ofSeconds(timeOutInSeconds));
    }


    public void waitForDisplay() {
        waitForDisplay(DriverUtils.getTimeOut());
    }

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
            log.error("waitForDisplay timeout after {} seconds for control '{}': {}", actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }

    @Deprecated
    public void waitForDisplay(int timeOutInSeconds) {
        waitForDisplay(Duration.ofSeconds(timeOutInSeconds));
    }

    public void waitForElementVisible() {
        waitForElementVisible(DriverUtils.getTimeOut());
    }

    public void waitForElementVisible(Duration timeout) {
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
            String msg = String.format("Element not visible after %d seconds: %s", actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForElementVisible timeout after {} seconds for control '{}': {}", actualTimeout.getSeconds(), getLocator(), msg);
            throw new RuntimeException(msg);
        }
    }

    @Deprecated
    public void waitForElementVisible(int timeOutInSeconds) {
        waitForElementVisible(Duration.ofSeconds(timeOutInSeconds));
    }

    public void waitForElementClickable() {
        waitForElementClickable(DriverUtils.getTimeOut());
    }

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
            String msg = String.format("Element not clickable after %d seconds: %s", actualTimeout.getSeconds(), getLocator().toString());
            log.error("WaitForElementClickable timeout after {} seconds for control '{}': {}", actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }

    @Deprecated
    public void waitForElementClickable(int timeOutInSecond) {
        waitForElementClickable(Duration.ofSeconds(timeOutInSecond));
    }

    public void waitForElementDisabled() {
        waitForElementDisabled(DriverUtils.getTimeOut());
    }

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
            String msg = String.format("Element not disabled after %d seconds: %s", actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForElementDisabled timeout after {} seconds for control '{}': {}", actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }

    @Deprecated
    public void waitForElementDisabled(int timeOutInSecond) {
        waitForElementDisabled(Duration.ofSeconds(timeOutInSecond));
    }

    public void waitForElementEnabled() {
        waitForElementEnabled(DriverUtils.getTimeOut());
    }

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
            String msg = String.format("Element not enabled after %d seconds: %s", actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForElementEnabled timeout after {} seconds for control '{}': {}", actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }

    @Deprecated
    public void waitForElementEnabled(int timeOutInSecond) {
        waitForElementEnabled(Duration.ofSeconds(timeOutInSecond));
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
            log.warn("waitForInvisibility timeout after {} seconds for control '{}'. Throwing.", actualTimeout.getSeconds(), getLocator().toString());
            throw new RuntimeException(msg);
        } else {
            log.info("Element {} is now invisible or removed from DOM", getLocator().toString());
        }
    }

    @Deprecated
    public void waitForInvisibility(int timeOutInSeconds) {
        waitForInvisibility(Duration.ofSeconds(timeOutInSeconds));
    }

    public void waitForTextToBeNotPresent(String text) {
        waitForTextToBeNotPresent(text, DriverUtils.getTimeOut());
    }

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
            log.error(String.format("waitForTextToBeNotPresent: Has error with control '%s'", getLocator().toString()));
            throw new RuntimeException(msg);
        }
    }

    @Deprecated
    public void waitForTextToBeNotPresent(String text, int timeOutInSecond) {
        waitForTextToBeNotPresent(text, Duration.ofSeconds(timeOutInSecond));
    }

    public void waitForTextToBePresent(String text) {
        waitForTextToBePresent(text, DriverUtils.getTimeOut());
    }

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
            log.error(String.format("waitForTextToBePresent: Has error with control '%s'", getLocator().toString()));
            throw new RuntimeException(msg);
        }
    }

    @Deprecated
    public void waitForTextToBePresent(String text, int timeOutInSecond) {
        waitForTextToBePresent(text, Duration.ofSeconds(timeOutInSecond));
    }

    public void waitForValueNotPresentInAttribute(String attribute, String value) {
        waitForValueNotPresentInAttribute(attribute, value, DriverUtils.getTimeOut());
    }

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
            log.error(String.format("waitForValueNotPresentInAttribute: Has error with control '%s'", getLocator().toString()));
            throw new RuntimeException(msg);
        }
    }

    @Deprecated
    public void waitForValueNotPresentInAttribute(String attribute, String value, int timeOutInSecond) {
        waitForValueNotPresentInAttribute(attribute, value, Duration.ofSeconds(timeOutInSecond));
    }

    public void waitForValuePresentInAttribute(String attribute, String value) {
        waitForValuePresentInAttribute(attribute, value, DriverUtils.getTimeOut());
    }

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
            log.error(String.format("waitForValuePresentInAttribute: Has error with control '%s'", getLocator().toString()));
            throw new RuntimeException(msg);
        }
    }

    @Deprecated
    public void waitForValuePresentInAttribute(String attribute, String value, int timeOutInSecond) {
        waitForValuePresentInAttribute(attribute, value, Duration.ofSeconds(timeOutInSecond));
    }

    public void waitForVisibility() {
        waitForVisibility(DriverUtils.getTimeOut());
    }

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
            String msg = String.format("Element not visible after %d seconds: %s", actualTimeout.getSeconds(), getLocator().toString());
            log.error("waitForVisibility timeout after {} seconds for control '{}': {}", actualTimeout.getSeconds(), getLocator().toString(), msg);
            throw new RuntimeException(msg);
        }
    }

    @Deprecated
    public void waitForVisibility(int timeOutInSeconds) {
        waitForVisibility(Duration.ofSeconds(timeOutInSeconds));
    }

    public void waitForStalenessOfElement() {
        waitForStalenessOfElement(DriverUtils.getTimeOut());
    }

    public void waitForStalenessOfElement(Duration timeout) {
        Duration actualTimeout = timeout.compareTo(Constants.DEFAULT_TIMEOUT) < 0 
                ? timeout : Constants.DEFAULT_TIMEOUT;
        WebDriver driver = getWebDriver();
        try {
            log.info(String.format("Wait for control staleness %s", getLocator().toString()));
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
                log.error(String.format("waitForStalenessOfElement: Has error with control '%s'", getLocator().toString()));
                throw new RuntimeException(msg);
            }
        } catch (Exception e) {
            log.error(String.format("waitForStalenessOfElement: Has error with control '%s': %s", getLocator().toString(),
                    e.getMessage().split("\n")[0]));
            throw new RuntimeException("waitForStalenessOfElement error for control: " + getLocator().toString(), e);
        }
    }

    @Deprecated
    public void waitForStalenessOfElement(int timeOutInSeconds) {
        waitForStalenessOfElement(Duration.ofSeconds(timeOutInSeconds));
    }

    public void click() {
        click(1);
    }

    public void click(int times) {
        if (times <= 0) return;

        int attemptsLeft = times;
        Exception lastException = null;

        while (attemptsLeft > 0) {
            try {
                log.debug("Click on {}", getLocator());

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
                return; // ok
            } catch (Exception e) {
                lastException = e;
                String msg = e.getMessage() == null ? "" : e.getMessage().split("\n")[0];

                boolean intercepted = msg.contains("Other element would receive the click")
                        || msg.contains("Element is not clickable at point")
                        || msg.contains("element click intercepted");

                attemptsLeft--;

                if (!intercepted) {
                    log.info("Non-intercepted click error on '{}': {}. Using JS click.", getLocator(), msg);
                    clickByJs();
                    return;
                }

                if (attemptsLeft == 0) {
                    log.info("Final click attempt failed on '{}': {}. Trying JS click.", getLocator(), msg);
                    try {
                        clickByJs();
                        return;
                    } catch (Exception jsEx) {
                        throw new RuntimeException("Click failed after retries on: " + getLocator(), lastException);
                    }
                }

                DriverUtils.delay(0.5);
                log.info("Retry click on '{}': {} ({} attempts left)", getLocator(), msg, attemptsLeft);
            }
        }

        throw new RuntimeException("Click failed after retries on: " + getLocator(), lastException);
    }

    public void click(int x, int y) {
        try {
            log.debug(String.format("Click on %s", getLocator().toString()));
            new Actions(getWebDriver()).moveToElement(getElement(), x, y).click().build().perform();
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }
    }

    public void clickByJs() {
        try {
            log.debug(String.format("Click by js on %s", getLocator().toString()));
            jsExecutor().executeScript("arguments[0].click();", getElement());
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }
    }

    public void doubleClick() {
        try {
            log.debug(String.format("Double click on %s", getLocator().toString()));
            new Actions(getWebDriver()).doubleClick(getElement()).build().perform();
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }
    }

    public void enter(CharSequence... value) {
        try {
            log.debug(String.format("Enter '%s' for %s", value, getLocator().toString()));
            getElement().sendKeys(value);
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }
    }

    public void setValue(String value) {
        try {
            String js = String.format("arguments[0].value='%s';", value);
            log.debug(String.format("Set value '%s' for %s", value, getLocator().toString()));
            jsExecutor().executeScript(js, getElement());
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }
    }

    public void clear() {
        try {
            log.debug(String.format("Clean text for %s", getLocator().toString()));
            getElement().clear();
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }
    }

    public void checkCheckBox() {
        if (!isSelected()) {
            click();
            DriverUtils.delay(1);
        }
    }

    public void uncheckCheckBox() {
        if (isSelected()) {
            click();
            DriverUtils.delay(1);
        }
    }

    public void checkCheckBoxByJs() {
        jsExecutor().executeScript("arguments[0].checked=true; arguments[0].dispatchEvent(new Event('change'));", getElement());
    }

    public void uncheckCheckBoxByJs() {
        jsExecutor().executeScript("arguments[0].checked=false; arguments[0].dispatchEvent(new Event('change'));", getElement());
    }

    public void setCheckBox(boolean value) {
        if (value && !isSelected()) {
            checkCheckBox();
        } else if (!value && isSelected()) {
            uncheckCheckBox();
        }
    }

    public void setAllCheckBoxes(boolean value) {
        for (WebElement el : getElements()) {
            boolean selected = el.isSelected();
            if (value != selected) {
                el.click();
                DriverUtils.delay(1);
            }
        }
    }

    public boolean isCheckBoxChecked() {
        return isSelected();
    }

    private Select getSelect() {
        return new Select(getElement());
    }

    public void selectComboBox(String text) {
        getSelect().selectByVisibleText(text);
    }

    public void selectComboBoxByIndex(int index) {
        getSelect().selectByIndex(index);
    }

    public String getComboBoxSelected() {
        return getSelect().getFirstSelectedOption().getText();
    }

    public List<String> getComboBoxOptions() {
        return getSelect().getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
    }

    public int getComboBoxTotalOptions() {
        return getSelect().getOptions().size();
    }

    public void switchToFrame() {
        getWebDriver().switchTo().frame(getElement());
    }

    public void switchToMainDocument() {
        getWebDriver().switchTo().defaultContent();
    }

    public String getLinkReference() {
        return getAttribute("href");
    }

    public String getImageSource() {
        return getAttribute("src");
    }

    public String getImageAlt() {
        return getAttribute("alt");
    }

    public int getTableRowCount() {
        return getTableRows().size();
    }

    public int getTableColumnCount() {
        List<WebElement> headerCells = getTableHeaderCells();
        return headerCells.isEmpty() ? getTableFirstRowCells().size() : headerCells.size();
    }

    public List<WebElement> getTableRows() {
        return getElement().findElements(By.xpath(".//tr"));
    }

    public List<WebElement> getTableHeaderCells() {
        return getElement().findElements(By.xpath(".//thead//tr//th | .//tr[1]//th"));
    }

    public List<WebElement> getTableFirstRowCells() {
        return getElement().findElements(By.xpath(".//tr[1]//td"));
    }

    public WebElement getTableCell(int row, int column) {
        validateTableRowColumn(row, column);
        return getElement().findElement(By.xpath(".//tr[" + (row + 1) + "]//td[" + (column + 1) + "]"));
    }

    public String getTableCellText(int row, int column) {
        return getTableCell(row, column).getText();
    }

    public List<String> getTableColumnData(int columnIndex) {
        validateTableColumn(columnIndex);
        List<WebElement> cells = getElement().findElements(By.xpath(".//tr//td[" + (columnIndex + 1) + "]"));
        List<String> columnData = new ArrayList<>();
        for (WebElement cell : cells) {
            columnData.add(cell.getText());
        }
        return columnData;
    }

    public List<String> getTableRowData(int rowIndex) {
        validateTableRow(rowIndex);
        List<WebElement> cells = getElement().findElements(By.xpath(".//tr[" + (rowIndex + 1) + "]//td"));
        List<String> rowData = new ArrayList<>();
        for (WebElement cell : cells) {
            rowData.add(cell.getText());
        }
        return rowData;
    }

    public Optional<Integer> findTableRowByText(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        List<WebElement> rows = getTableRows();
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getText().contains(text)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> findTableColumnByHeaderText(String headerText) {
        if (headerText == null) {
            throw new IllegalArgumentException("Header text cannot be null");
        }
        List<WebElement> headers = getTableHeaderCells();
        for (int i = 0; i < headers.size(); i++) {
            if (headerText.equals(headers.get(i).getText())) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    private void validateTableRow(int row) {
        if (row < 0 || row >= getTableRowCount()) {
            throw new IndexOutOfBoundsException("Row index " + row + " is out of bounds. Total rows: " + getTableRowCount());
        }
    }

    private void validateTableColumn(int column) {
        if (column < 0 || column >= getTableColumnCount()) {
            throw new IndexOutOfBoundsException("Column index " + column + " is out of bounds. Total columns: " + getTableColumnCount());
        }
    }

    private void validateTableRowColumn(int row, int column) {
        validateTableRow(row);
        validateTableColumn(column);
    }
}
