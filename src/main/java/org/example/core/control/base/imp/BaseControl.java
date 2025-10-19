package org.example.core.control.base.imp;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.core.control.base.IBaseControl;
import org.example.core.control.util.DriverUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Condition.*;
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

    protected WebDriver getDriver() {
        return DriverUtils.getWebDriver();
    }

    protected JavascriptExecutor jsExecutor() {
        return (JavascriptExecutor) getDriver();
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
        Actions actions = new Actions(getDriver());
        actions.dragAndDropBy(getElement(), xOffset, yOffset).build().perform();
    }

    @Override
    public void dragAndDrop(BaseControl target) {
        Actions actions = new Actions(getDriver());
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
                element = getDriver().findElement(getLocator());
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
        return getDriver().findElements(getLocator());
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
        try {
            $(getLocator()).shouldBe(and("clickable", enabled, visible), Duration.ofSeconds(DriverUtils.getTimeOut()));
            return true;
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

    @Override
    public boolean isExist(int timeOutInSeconds) {
        int actualTimeout = Math.min(timeOutInSeconds, (int)(Constants.DEFAULT_TIMEOUT / 1000)); // Giới hạn tối đa 20 giây
        try {
            $(getLocator()).shouldBe(exist, Duration.ofSeconds(actualTimeout));
            return true;
        } catch (Exception e) {
            log.debug("IsExisted timeout after {} seconds for control '{}': {}", 
                actualTimeout, getLocator().toString(), e.getMessage());
            return false;
        }
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

    @Override
    public boolean isVisible(int timeOutInSeconds) {
        int actualTimeout = Math.min(timeOutInSeconds, (int)(Constants.DEFAULT_TIMEOUT / 1000)); // Giới hạn tối đa 20 giây
        try {
            $(getLocator()).shouldBe(visible, Duration.ofSeconds(actualTimeout));
            return true;
        } catch (Exception e) {
            log.debug("IsVisible timeout after {} seconds for control '{}': {}", 
                actualTimeout, getLocator().toString(), e.getMessage());
            return false;
        }
    }

    @Override
    public void mouseHoverJScript() {
        String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";
        jsExecutor().executeScript(mouseOverScript, getElement());

    }

    @Override
    public void moveTo() {
        Actions actions = new Actions(getDriver());
        actions.moveToElement(getElement()).build().perform();
    }

    @Override
    public void moveTo(int x, int y) {
        WebElement element = getElement();
        int absX = element.getLocation().x + x;
        int absY = element.getLocation().y + y;

        Actions actions = new Actions(getDriver());
        actions.moveByOffset(absX, absY).build().perform();
    }

    @Override
    public void moveToCenter() {
        WebElement element = getElement();
        int x = element.getLocation().x + element.getSize().width / 2;
        int y = element.getLocation().y + element.getSize().height / 2;

        Actions actions = new Actions(getDriver());
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

    /**
     * Set value for dynamic control.
     *
     * @param args
     * @Example TextBox myTextBox = new TextBox("//input[@value='%s']"); </br>
     * myTextBox.setDynamicValue("example");
     */
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
        waitForDisappear(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForDisappear(int timeOutInSeconds) {
        int actualTimeout = Math.min(timeOutInSeconds, (int)(Constants.DEFAULT_TIMEOUT / 1000)); // Giới hạn tối đa 20 giây
        try {
            log.info("Wait for control disappear {} with timeout {} seconds", getLocator().toString(), actualTimeout);
            $(getLocator()).shouldBe(disappear, Duration.ofSeconds(actualTimeout));
        } catch (Exception e) {
            log.warn("waitForDisappear timeout after {} seconds for control '{}'. Continuing execution.", 
                actualTimeout, getLocator().toString());
            // Không throw exception, chỉ log warning và tiếp tục
        }
    }

    @Override
    public void waitForDisplay() {
        waitForDisplay(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForDisplay(int timeOutInSeconds) {
        int actualTimeout = Math.min(timeOutInSeconds, (int)(Constants.DEFAULT_TIMEOUT / 1000)); // Giới hạn tối đa 20 giây
        try {
            log.info("Wait for control display {} with timeout {} seconds", getLocator().toString(), actualTimeout);
            $(getLocator()).shouldBe(exist, Duration.ofSeconds(actualTimeout));
        } catch (Exception e) {
            log.error("WaitForDisplay timeout after {} seconds for control '{}': {}", 
                actualTimeout, getLocator().toString(), e.getMessage().split("\n")[0]);
            throw new RuntimeException(String.format("Element not found after %d seconds: %s", actualTimeout, getLocator().toString()));
        }
    }

    public void waitForElementVisible() {
        waitForElementVisible(DriverUtils.getTimeOut());
    }

    public void waitForElementVisible(int timeOutInSeconds) {
        int actualTimeout = Math.min(timeOutInSeconds, (int)(Constants.DEFAULT_TIMEOUT / 1000)); // Giới hạn tối đa 20 giây
        try {
            log.info("Wait for element visible {} with timeout {} seconds", getLocator().toString(), actualTimeout);
            $(getLocator()).shouldBe(visible, Duration.ofSeconds(actualTimeout));
        } catch (Exception e) {
            log.error("waitForElementVisible timeout after {} seconds for control '{}': {}", 
                actualTimeout, getLocator().toString(), e.getMessage().split("\n")[0]);
            throw new RuntimeException(String.format("Element not visible after %d seconds: %s", actualTimeout, getLocator().toString()));
        }
    }

    @Override
    public void waitForElementClickable() {
        waitForElementClickable(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForElementClickable(int timeOutInSecond) {
        int actualTimeout = Math.min(timeOutInSecond, (int)(Constants.DEFAULT_TIMEOUT / 1000)); // Giới hạn tối đa 20 giây
        try {
            log.info("Wait for element clickable {} with timeout {} seconds", getLocator().toString(), actualTimeout);
            $(getLocator()).shouldBe(and("clickable", enabled, visible), Duration.ofSeconds(actualTimeout));
        } catch (Exception e) {
            log.error("WaitForElementClickable timeout after {} seconds for control '{}': {}", 
                actualTimeout, getLocator().toString(), e.getMessage().split("\n")[0]);
            throw new RuntimeException(String.format("Element not clickable after %d seconds: %s", actualTimeout, getLocator().toString()));
        }
    }

    @Override
    public void waitForElementDisabled(int timeOutInSecond) {
        try {
            $(getLocator()).shouldBe(disabled, Duration.ofSeconds(timeOutInSecond));
        } catch (Exception e) {
            log.error(String.format("waitForElementDisabled: Has error with control '%s': %s",
                    getLocator().toString(), e.getMessage().split("\n")[0]));
        }
    }

    @Override
    public void waitForElementDisabled() {
        waitForElementDisabled(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForElementEnabled(int timeOutInSecond) {
        try {
            $(getLocator()).shouldBe(enabled, Duration.ofSeconds(timeOutInSecond));
        } catch (Exception e) {
            log.error(String.format("waitForElementEnabled: Has error with control '%s': %s",
                    getLocator().toString(), e.getMessage().split("\n")[0]));
        }
    }

    @Override
    public void waitForElementEnabled() {
        waitForElementEnabled(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForInvisibility() {
        waitForInvisibility(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForInvisibility(int timeOutInSeconds) {
        int actualTimeout = Math.min(timeOutInSeconds, (int)(Constants.DEFAULT_TIMEOUT / 1000)); // Giới hạn tối đa 20 giây
        try {
            log.info("Wait for invisibility of {} with timeout {} seconds", getLocator().toString(), actualTimeout);
            $(getLocator()).shouldBe(hidden, Duration.ofSeconds(actualTimeout));
            log.info("Element {} is now invisible or removed from DOM", getLocator().toString());
        } catch (Exception e) {
            log.warn("waitForInvisibility timeout after {} seconds for control '{}'. Continuing execution.", 
                actualTimeout, getLocator().toString());
            // Không throw exception, chỉ log warning và tiếp tục
        }
    }

    @Override
    public void waitForTextToBeNotPresent(String text, int timeOutInSecond) {
        try {
            log.info(String.format("Wait for text not to be present in %s", getLocator().toString()));
            $(getLocator()).shouldNotHave(text(text), Duration.ofSeconds(timeOutInSecond));
        } catch (Exception e) {
            log.error(String.format("waitForTextToBeNotPresent: Has error with control '%s': %s",
                    getLocator().toString(), e.getMessage().split("\n")[0]));
        }
    }

    @Override
    public void waitForTextToBePresent(String text, int timeOutInSecond) {
        try {
            log.info(String.format("Wait for text to be present in %s", getLocator().toString()));
            $(getLocator()).shouldHave(text(text), Duration.ofSeconds(timeOutInSecond));
        } catch (Exception e) {
            log.error(String.format("waitForTextToBePresent: Has error with control '%s': %s",
                    getLocator().toString(), e.getMessage().split("\n")[0]));
        }
    }

    @Override
    public void waitForValueNotPresentInAttribute(String attribute, String value, int timeOutInSecond) {
        try {
            log.info(String.format("Wait for %s not to be present in %s attribute of %s", value, attribute, getLocator().toString()));
            $(getLocator()).shouldNotHave(attribute(attribute, value), Duration.ofSeconds(timeOutInSecond));
        } catch (Exception e) {
            log.error(String.format("waitForValueNotPresentInAttribute: Has error with control '%s': %s",
                    getLocator().toString(), e.getMessage().split("\n")[0]));
        }
    }

    @Override
    public void waitForValuePresentInAttribute(String attribute, String value, int timeOutInSecond) {
        try {
            log.info(String.format("Wait for %s to be present in %s attribute of %s", value, attribute, getLocator().toString()));
            $(getLocator()).shouldHave(attribute(attribute, value), Duration.ofSeconds(timeOutInSecond));
        } catch (Exception e) {
            log.error(String.format("waitForValuePresentInAttribute: Has error with control '%s': %s",
                    getLocator().toString(), e.getMessage().split("\n")[0]));
        }
    }

    @Override
    public void waitForVisibility() {
        waitForVisibility(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForVisibility(int timeOutInSeconds) {
        int actualTimeout = Math.min(timeOutInSeconds, (int)(Constants.DEFAULT_TIMEOUT / 1000)); // Giới hạn tối đa 20 giây
        try {
            log.info("Wait for control's visibility {} with timeout {} seconds", getLocator().toString(), actualTimeout);
            $(getLocator()).shouldBe(visible, Duration.ofSeconds(actualTimeout));
        } catch (Exception e) {
            log.error("waitForVisibility timeout after {} seconds for control '{}': {}", 
                actualTimeout, getLocator().toString(), e.getMessage().split("\n")[0]);
            throw new RuntimeException(String.format("Element not visible after %d seconds: %s", actualTimeout, getLocator().toString()));
        }
    }

    @Override
    public void waitForStalenessOfElement() {
        waitForStalenessOfElement(DriverUtils.getTimeOut());
    }

    @Override
    public void waitForStalenessOfElement(int timeOutInSeconds) {
        try {
            log.info(String.format("Wait for control staleness %s", getLocator().toString()));
            // Selenide doesn't have direct equivalent for staleness, using disappear instead
            $(getLocator()).shouldBe(disappear, Duration.ofSeconds(timeOutInSeconds));
        } catch (Exception e) {
            log.error(String.format("waitForStalenessOfElement: Has error with control '%s': %s", getLocator().toString(),
                    e.getMessage().split("\n")[0]));
        }
    }
}