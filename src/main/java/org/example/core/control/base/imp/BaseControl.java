package org.example.core.control.base.imp;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.core.control.base.IBaseControl;
import org.example.core.control.util.DriverUtils;
import org.example.enums.WaitType;
import org.example.utils.WaitUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import java.time.Duration;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openqa.selenium.support.ui.Select;
import static org.example.core.control.util.DriverUtils.getWebDriver;

@Slf4j
public class BaseControl implements IBaseControl {

    private String locator;
    private By byLocator;
    private String dynamicLocator;
    private BaseControl parent;

    public BaseControl(String locator) {
        this.locator = locator;
        this.dynamicLocator = locator;
        this.byLocator = getByLocator();
    }

    public BaseControl(By byLocator) {
        this.byLocator = byLocator;
    }

    public BaseControl(String locator, Object... args) {
        this.dynamicLocator = locator;
        this.locator = String.format(dynamicLocator, args);
        this.byLocator = getByLocator();
    }

    public BaseControl(BaseControl parent, String locator) {
        this.locator = locator;
        this.dynamicLocator = locator;
        this.byLocator = getByLocator();
        this.parent = parent;
    }

    public BaseControl(BaseControl parent, By byLocator) {
        this.byLocator = byLocator;
        this.parent = parent;
    }

    public BaseControl(BaseControl parent, String locator, Object... args) {
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

    @Override
    public void dragAndDrop(int xOffset, int yOffset) {
        Actions actions = new Actions(getWebDriver());
        actions.dragAndDropBy(getElement(), xOffset, yOffset).build().perform();
    }

    @Override
    public void dragAndDrop(BaseControl target) {
        Actions actions = new Actions(getWebDriver());
        actions.dragAndDrop(getElement(), target.getElement()).build().perform();
    }

    public void focus() {
        DriverUtils.execJavaScript("arguments[0].focus();", getElement());
    }

    @Override
    public String getAttribute(String attributeName) {
        try {
            log.debug(String.format("Get attribute '%s' of element %s", attributeName, getLocator().toString()));
            return getElement().getAttribute(attributeName);
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }

    }

    @Override
    public WebElement getChildElement(String xpath) {
        return getElement().findElement(By.xpath(xpath));
    }

    @Override
    public List<WebElement> getChildElements() {
        return getChildElements("./*");
    }

    @Override
    public List<WebElement> getChildElements(String xpath) {
        return getElement().findElements(By.xpath(xpath));
    }

    @Override
    public String getClassName() {
        return getAttribute("class");
    }

    @Override
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

    @Override
    public List<WebElement> getElements() {
        if (parent != null)
            return parent.getElement().findElements(getLocator());
        return getWebDriver().findElements(getLocator());
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseControl> List<T> getListElements(Class<?> clazz) {
        String js = "function getElementTreeXPath(e){for(var n=[];e&&1==e.nodeType;e=e.parentNode){for(var o=0,r=e.previousSibling;r;r=r.previousSibling)r.nodeType!=Node.DOCUMENT_TYPE_NODE&&r.nodeName==e.nodeName&&++o;var t=e.nodeName.toLowerCase(),a=o?'['+(o+1)+']':'[1]';n.splice(0,0,t+a)}return n.length?'/'+n.join('/'):null} return getElementTreeXPath(arguments[0]);";
        List<T> result = new ArrayList<T>();
        List<WebElement> list = getElements();
        for (WebElement webEle : list) {
            try {
                String xpath = (String) DriverUtils.execJavaScript(js, webEle);
                Constructor<?> ctor = clazz.getDeclaredConstructor(By.class);
                ctor.setAccessible(true);
                T element = (T) ctor.newInstance(By.xpath(xpath));
                result.add(element);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseControl> List<T> getListElements(Class<?> clazz, String locator) {
        List<T> result = new ArrayList<>();
        List<WebElement> list = getElement().findElements(By.xpath(locator));
        for (WebElement webEle : list) {
            try {
                String js = "function getElementTreeXPath(e){for(var n=[];e&&1==e.nodeType;e=e.parentNode){for(var o=0,r=e.previousSibling;r;r=r.previousSibling)r.nodeType!=Node.DOCUMENT_TYPE_NODE&&r.nodeName==e.nodeName&&++o;var t=e.nodeName.toLowerCase(),a=o?'['+(o+1)+']':'[1]';n.splice(0,0,t+a)}return n.length?'/'+n.join('/'):null} return getElementTreeXPath(arguments[0]);";
                String xpath = (String) DriverUtils.execJavaScript(js, webEle);
                Constructor<?> ctor = clazz.getDeclaredConstructor(By.class);
                ctor.setAccessible(true);
                T element = (T) ctor.newInstance(By.xpath(xpath));
                result.add(element);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public By getLocator() {
        return this.byLocator;
    }

    @Override
    public String getLocatorString() {
        return this.locator;
    }

    @Override
    public String getTagName() {
        return getElement().getTagName();
    }

    @Override
    public String getText() {
        try {
            log.debug(String.format("Get text of element %s", getLocator().toString()));
            return getElement().getText();
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
        }
    }

    @Override
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

    @Override
    public String getValue() {
        try {
            log.debug(String.format("Get value of element %s", getLocator().toString()));
            return getElement().getAttribute("value");
        } catch (Exception e) {
            log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
            throw e;
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
    public boolean isDynamicLocator() {
        return this.locator != null && this.locator.toLowerCase().matches("(.*)%[s|d](.*)");
    }

    @Override
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

    @Override
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

    @Override
    @Deprecated
    public boolean isExist(int timeOutInSeconds) {
        return isExist(Duration.ofSeconds(timeOutInSeconds));
    }


    @Override
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

    @Override
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

    @Override
    @Deprecated
    public boolean isVisible(int timeOutInSeconds) {
        return isVisible(Duration.ofSeconds(timeOutInSeconds));
    }

    @Override
    public void mouseHoverJScript() {
        String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
        jsExecutor().executeScript(mouseOverScript, getElement());

    }

    @Override
    public void moveTo() {
        Actions actions = new Actions(getWebDriver());
        actions.moveToElement(getElement()).build().perform();
    }

    @Override
    public void moveTo(int x, int y) {
        WebElement element = getElement();
        int absX = element.getLocation().x + x;
        int absY = element.getLocation().y + y;

        Actions actions = new Actions(getWebDriver());
        actions.moveByOffset(absX, absY).build().perform();
    }

    @Override
    public void moveToCenter() {
        WebElement element = getElement();
        int x = element.getLocation().x + element.getSize().width / 2;
        int y = element.getLocation().y + element.getSize().height / 2;

        Actions actions = new Actions(getWebDriver());
        actions.moveByOffset(x, y).build().perform();
    }

    @Override
    public void scrollElementToCenterScreen() {
        DriverUtils.waitForAutoScrollingStopped();
        String js = "Element.prototype.documentOffsetTop=function(){return this.offsetTop+(this.offsetParent?this.offsetParent.documentOffsetTop():0)};var top=arguments[0].documentOffsetTop()-window.innerHeight/2;window.scrollTo(0,top);";
        DriverUtils.execJavaScript(js, getElement());
        log.info("Scroll element {} to center of screen", getLocator().toString());
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void setDynamicValue(Object... args) {
        this.locator = String.format(this.dynamicLocator, args);
        this.byLocator = getByLocator();
    }

    @Override
    public void submit() {
        getElement().submit();
    }

    @Override
    public void waitForDisappear() {
        waitForDisappear(DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForDisappear(Duration timeout, WaitType waitType) {
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
            if (waitType == WaitType.STRICT) {
                log.warn("Element '{}' still visible after {} seconds", getLocator().toString(), actualTimeout.getSeconds());
                throw new RuntimeException(msg);
            } else {
                log.warn("SOFT wait - continuing despite: {}", msg);
            }
        }
    }

    @Override
    @Deprecated
    public void waitForDisappear(int timeOutInSeconds, WaitType waitType) {
        waitForDisappear(Duration.ofSeconds(timeOutInSeconds), waitType);
    }


    @Override
    public void waitForDisplay() {
        waitForDisplay(DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForDisplay(Duration timeout, WaitType waitType) {
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
            if (waitType == WaitType.STRICT) {
                throw new RuntimeException(msg);
            } else {
                log.warn("SOFT wait - continuing despite: {}", msg);
            }
        }
    }

    @Override
    @Deprecated
    public void waitForDisplay(int timeOutInSeconds, WaitType waitType) {
        waitForDisplay(Duration.ofSeconds(timeOutInSeconds), waitType);
    }

    public void waitForElementVisible() {
        waitForElementVisible(DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForElementVisible(Duration timeout) {
        waitForElementVisible(timeout, WaitType.STRICT);
    }

    public void waitForElementVisible(WaitType waitType) {
        waitForElementVisible(DriverUtils.getTimeOut(), waitType);
    }

    public void waitForElementVisible(Duration timeout, WaitType waitType) {
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
            if (waitType == WaitType.STRICT) {
                throw new RuntimeException(msg);
            } else {
                log.warn("SOFT wait - continuing despite: {}", msg);
            }
        }
    }

    @Deprecated
    public void waitForElementVisible(int timeOutInSeconds) {
        waitForElementVisible(Duration.ofSeconds(timeOutInSeconds), WaitType.STRICT);
    }

    @Deprecated
    public void waitForElementVisible(int timeOutInSeconds, WaitType waitType) {
        waitForElementVisible(Duration.ofSeconds(timeOutInSeconds), waitType);
    }

    @Override
    public void waitForElementClickable() {
        waitForElementClickable(DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForElementClickable(Duration timeout) {
        waitForElementClickable(timeout, WaitType.STRICT);
    }

    public void waitForElementClickable(WaitType waitType) {
        waitForElementClickable(DriverUtils.getTimeOut(), waitType);
    }

    public void waitForElementClickable(Duration timeout, WaitType waitType) {
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
            if (waitType == WaitType.STRICT) {
                throw new RuntimeException(msg);
            } else {
                log.warn("SOFT wait - continuing despite: {}", msg);
            }
        }
    }

    @Override
    @Deprecated
    public void waitForElementClickable(int timeOutInSecond) {
        waitForElementClickable(Duration.ofSeconds(timeOutInSecond), WaitType.STRICT);
    }

    @Deprecated
    public void waitForElementClickable(int timeOutInSecond, WaitType waitType) {
        waitForElementClickable(Duration.ofSeconds(timeOutInSecond), waitType);
    }

    @Override
    public void waitForElementDisabled() {
        waitForElementDisabled(DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForElementDisabled(Duration timeout) {
        waitForElementDisabled(timeout, WaitType.STRICT);
    }

    public void waitForElementDisabled(WaitType waitType) {
        waitForElementDisabled(DriverUtils.getTimeOut(), waitType);
    }

    public void waitForElementDisabled(Duration timeout, WaitType waitType) {
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
            if (waitType == WaitType.STRICT) {
                throw new RuntimeException(msg);
            } else {
                log.warn("SOFT wait - continuing despite: {}", msg);
            }
        }
    }

    @Override
    @Deprecated
    public void waitForElementDisabled(int timeOutInSecond) {
        waitForElementDisabled(Duration.ofSeconds(timeOutInSecond), WaitType.STRICT);
    }

    @Deprecated
    public void waitForElementDisabled(int timeOutInSecond, WaitType waitType) {
        waitForElementDisabled(Duration.ofSeconds(timeOutInSecond), waitType);
    }

    @Override
    public void waitForElementEnabled() {
        waitForElementEnabled(DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForElementEnabled(Duration timeout) {
        waitForElementEnabled(timeout, WaitType.STRICT);
    }

    public void waitForElementEnabled(WaitType waitType) {
        waitForElementEnabled(DriverUtils.getTimeOut(), waitType);
    }

    public void waitForElementEnabled(Duration timeout, WaitType waitType) {
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
            if (waitType == WaitType.STRICT) {
                throw new RuntimeException(msg);
            } else {
                log.warn("SOFT wait - continuing despite: {}", msg);
            }
        }
    }

    @Override
    @Deprecated
    public void waitForElementEnabled(int timeOutInSecond) {
        waitForElementEnabled(Duration.ofSeconds(timeOutInSecond), WaitType.STRICT);
    }

    @Deprecated
    public void waitForElementEnabled(int timeOutInSecond, WaitType waitType) {
        waitForElementEnabled(Duration.ofSeconds(timeOutInSecond), waitType);
    }

    @Override
    public void waitForInvisibility() {
        waitForInvisibility(DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForInvisibility(Duration timeout, WaitType waitType) {
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
            if (waitType == WaitType.STRICT) {
                log.warn("waitForInvisibility timeout after {} seconds for control '{}'. Throwing.", actualTimeout.getSeconds(), getLocator().toString());
                throw new RuntimeException(msg);
            } else {
                log.warn("SOFT wait - continuing despite: {}", msg);
            }
        } else {
            log.info("Element {} is now invisible or removed from DOM", getLocator().toString());
        }
    }

    @Override
    @Deprecated
    public void waitForInvisibility(int timeOutInSeconds, WaitType waitType) {
        waitForInvisibility(Duration.ofSeconds(timeOutInSeconds), waitType);
    }

    @Override
    public void waitForTextToBeNotPresent(String text) {
        waitForTextToBeNotPresent(text, DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForTextToBeNotPresent(String text, Duration timeout, WaitType waitType) {
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
            if (waitType == WaitType.STRICT) {
                throw new RuntimeException(msg);
            } else {
                log.warn("SOFT wait - continuing despite: {}", msg);
            }
        }
    }

    @Override
    @Deprecated
    public void waitForTextToBeNotPresent(String text, int timeOutInSecond, WaitType waitType) {
        waitForTextToBeNotPresent(text, Duration.ofSeconds(timeOutInSecond), waitType);
    }

    @Override
    public void waitForTextToBePresent(String text) {
        waitForTextToBePresent(text, DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForTextToBePresent(String text, Duration timeout, WaitType waitType) {
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
            if (waitType == WaitType.STRICT) {
                throw new RuntimeException(msg);
            } else {
                log.warn("SOFT wait - continuing despite: {}", msg);
            }
        }
    }

    @Override
    @Deprecated
    public void waitForTextToBePresent(String text, int timeOutInSecond, WaitType waitType) {
        waitForTextToBePresent(text, Duration.ofSeconds(timeOutInSecond), waitType);
    }

    @Override
    public void waitForValueNotPresentInAttribute(String attribute, String value) {
        waitForValueNotPresentInAttribute(attribute, value, DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForValueNotPresentInAttribute(String attribute, String value, Duration timeout, WaitType waitType) {
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
            if (waitType == WaitType.STRICT) {
                throw new RuntimeException(msg);
            } else {
                log.warn("SOFT wait - continuing despite: {}", msg);
            }
        }
    }

    @Override
    @Deprecated
    public void waitForValueNotPresentInAttribute(String attribute, String value, int timeOutInSecond, WaitType waitType) {
        waitForValueNotPresentInAttribute(attribute, value, Duration.ofSeconds(timeOutInSecond), waitType);
    }

    @Override
    public void waitForValuePresentInAttribute(String attribute, String value) {
        waitForValuePresentInAttribute(attribute, value, DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForValuePresentInAttribute(String attribute, String value, Duration timeout, WaitType waitType) {
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
            if (waitType == WaitType.STRICT) {
                throw new RuntimeException(msg);
            } else {
                log.warn("SOFT wait - continuing despite: {}", msg);
            }
        }
    }

    @Override
    @Deprecated
    public void waitForValuePresentInAttribute(String attribute, String value, int timeOutInSecond, WaitType waitType) {
        waitForValuePresentInAttribute(attribute, value, Duration.ofSeconds(timeOutInSecond), waitType);
    }

    @Override
    public void waitForVisibility() {
        waitForVisibility(DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForVisibility(Duration timeout, WaitType waitType) {
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
            if (waitType == WaitType.STRICT) {
                throw new RuntimeException(msg);
            } else {
                // SOFT: warn and continue
                log.warn("SOFT wait - continuing despite timeout: {}", msg);
            }
        }
    }

    @Override
    @Deprecated
    public void waitForVisibility(int timeOutInSeconds, WaitType waitType) {
        waitForVisibility(Duration.ofSeconds(timeOutInSeconds), waitType);
    }

    @Override
    public void waitForStalenessOfElement() {
        waitForStalenessOfElement(DriverUtils.getTimeOut(), WaitType.STRICT);
    }

    public void waitForStalenessOfElement(Duration timeout, WaitType waitType) {
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
                if (waitType == WaitType.STRICT) {
                    throw new RuntimeException(msg);
                } else {
                    log.warn("SOFT wait - continuing despite: {}", msg);
                }
            }
        } catch (Exception e) {
            log.error(String.format("waitForStalenessOfElement: Has error with control '%s': %s", getLocator().toString(),
                    e.getMessage().split("\n")[0]));
            if (waitType == WaitType.STRICT) {
                throw new RuntimeException("waitForStalenessOfElement error for control: " + getLocator().toString(), e);
            } else {
                log.warn("SOFT wait - continuing despite exception: {}", e.getMessage().split("\n")[0]);
            }
        }
    }

    @Override
    @Deprecated
    public void waitForStalenessOfElement(int timeOutInSeconds, WaitType waitType) {
        waitForStalenessOfElement(Duration.ofSeconds(timeOutInSeconds), waitType);
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
                    waitForDisplay(DriverUtils.getTimeOut(), WaitType.STRICT);
                }

                scrollElementToCenterScreen();
                waitForElementClickable(DriverUtils.getTimeOut(), WaitType.STRICT);

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

