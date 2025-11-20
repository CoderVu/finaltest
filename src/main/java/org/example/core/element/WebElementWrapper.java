package org.example.core.element;

import lombok.extern.slf4j.Slf4j;
import org.example.core.element.component.ElementActions;
import org.example.core.element.component.ElementChecks;
import org.example.core.element.component.ElementGetters;
import org.example.core.element.component.ElementScrolls;
import org.example.core.element.component.ElementWaits;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;

@Slf4j
public class WebElementWrapper extends BaseElement {
    
    protected ElementActions actions;
    protected ElementWaits waits;
    protected ElementScrolls scrolls;
    protected ElementGetters getters;
    protected ElementChecks checks;
    
    public WebElementWrapper(String locator) {
        super(locator);
        this.actions = new ElementActions(this);
        this.waits = new ElementWaits(this);
        this.scrolls = new ElementScrolls(this);
        this.getters = new ElementGetters(this);
        this.checks = new ElementChecks(this);
    }
    
    public WebElementWrapper(By byLocator) {
        super(byLocator);
        this.actions = new ElementActions(this);
        this.waits = new ElementWaits(this);
        this.scrolls = new ElementScrolls(this);
        this.getters = new ElementGetters(this);
        this.checks = new ElementChecks(this);
    }
    
    public WebElementWrapper(String locator, Object... args) {
        super(locator, args);
        this.actions = new ElementActions(this);
        this.waits = new ElementWaits(this);
        this.scrolls = new ElementScrolls(this);
        this.getters = new ElementGetters(this);
        this.checks = new ElementChecks(this);
    }
    
    public WebElementWrapper(IElement parent, String locator) {
        super(parent, locator);
        this.actions = new ElementActions(this);
        this.waits = new ElementWaits(this);
        this.scrolls = new ElementScrolls(this);
        this.getters = new ElementGetters(this);
        this.checks = new ElementChecks(this);
    }
    
    public WebElementWrapper(IElement parent, By byLocator) {
        super(parent, byLocator);
        this.actions = new ElementActions(this);
        this.waits = new ElementWaits(this);
        this.scrolls = new ElementScrolls(this);
        this.getters = new ElementGetters(this);
        this.checks = new ElementChecks(this);
    }
    
    public WebElementWrapper(IElement parent, String locator, Object... args) {
        super(parent, locator, args);
        this.actions = new ElementActions(this);
        this.waits = new ElementWaits(this);
        this.scrolls = new ElementScrolls(this);
        this.getters = new ElementGetters(this);
        this.checks = new ElementChecks(this);
    }
    
    // Actions - delegate to ElementActions
    @Override
    public void click() {
        actions.click();
    }
    
    @Override
    public void click(int times) {
        actions.click(times);
    }
    
    @Override
    public void click(int x, int y) {
        actions.click(x, y);
    }
    
    @Override
    public void clickByJs() {
        actions.clickByJs();
    }
    
    @Override
    public void doubleClick() {
        actions.doubleClick();
    }
    
    @Override
    public void setText(String text) {
        actions.setText(text);
    }
    
    @Override
    public void clear() {
        actions.clear();
    }
    
    @Override
    public void enter(CharSequence... value) {
        actions.enter(value);
    }
    
    @Override
    public void sendKeys(Keys key) {
        actions.sendKeys(key);
    }
    
    @Override
    public void submit() {
        actions.submit();
    }
    
    @Override
    public void focus() {
        actions.focus();
    }
    
    @Override
    public void dragAndDrop(int xOffset, int yOffset) {
        actions.dragAndDrop(xOffset, yOffset);
    }
    
    @Override
    public void dragAndDrop(IElement target) {
        actions.dragAndDrop(target);
    }
    
    @Override
    public void moveTo() {
        actions.moveTo();
    }
    
    @Override
    public void moveTo(int x, int y) {
        actions.moveTo(x, y);
    }
    
    @Override
    public void moveToCenter() {
        actions.moveToCenter();
    }
    
    @Override
    public void mouseHoverJScript() {
        actions.mouseHoverJScript();
    }
    
    @Override
    public void setAttributeJS(String attributeName, String value) {
        actions.setAttributeJS(attributeName, value);
    }
    
    @Override
    public void checkCheckBoxByJs() {
        actions.checkCheckBoxByJs();
    }
    
    // Scrolls - delegate to ElementScrolls
    @Override
    public void scrollElementToCenterScreen() {
        scrolls.scrollElementToCenterScreen();
    }
    
    @Override
    public void scrollToView() {
        scrolls.scrollToView();
    }
    
    @Override
    public void scrollToView(int offsetX, int offsetY) {
        scrolls.scrollToView(offsetX, offsetY);
    }
    
    // Getters - delegate to ElementGetters
    @Override
    public String getText() {
        return getters.getText();
    }
    
    @Override
    public String getValue() {
        return getters.getValue();
    }
    
    @Override
    public String getAttribute(String attributeName) {
        return getters.getAttribute(attributeName);
    }
    
    @Override
    public String getClassName() {
        return getters.getClassName();
    }
    
    @Override
    public String getTagName() {
        return getters.getTagName();
    }
    
    @Override
    public WebElement getChildElement(String xpath) {
        return getters.getChildElement(xpath);
    }
    
    @Override
    public List<WebElement> getChildElements() {
        return getters.getChildElements();
    }
    
    @Override
    public List<WebElement> getChildElements(String xpath) {
        return getters.getChildElements(xpath);
    }
    
    // Checks - delegate to ElementChecks
    @Override
    public boolean isVisible() {
        return checks.isVisible();
    }
    
    @Override
    public boolean isVisible(Duration timeout) {
        return checks.isVisible(timeout);
    }
    
    @Override
    public boolean isEnabled() {
        return checks.isEnabled();
    }
    
    @Override
    public boolean isSelected() {
        return checks.isSelected();
    }
    
    @Override
    public boolean isClickable() {
        return checks.isClickable();
    }
    
    @Override
    public boolean isExist() {
        return checks.isExist();
    }
    
    @Override
    public boolean isExist(Duration timeout) {
        return checks.isExist(timeout);
    }
    
    // Waits - delegate to ElementWaits
    @Override
    public void waitForVisibility() {
        waits.waitForVisibility();
    }
    
    @Override
    public void waitForVisibility(Duration timeout) {
        waits.waitForVisibility(timeout);
    }
    
    @Override
    public void waitForElementVisible() {
        waits.waitForElementVisible();
    }
    
    @Override
    public void waitForElementVisible(Duration timeout) {
        waits.waitForElementVisible(timeout);
    }
    
    @Override
    public void waitForElementClickable() {
        waits.waitForElementClickable();
    }
    
    @Override
    public void waitForElementClickable(Duration timeout) {
        waits.waitForElementClickable(timeout);
    }
    
    @Override
    public void waitForDisplay() {
        waits.waitForDisplay();
    }
    
    @Override
    public void waitForDisplay(Duration timeout) {
        waits.waitForDisplay(timeout);
    }
    
    @Override
    public void waitForInvisibility() {
        waits.waitForInvisibility();
    }
    
    @Override
    public void waitForInvisibility(Duration timeout) {
        waits.waitForInvisibility(timeout);
    }
    
    @Override
    public void waitForDisappear() {
        waits.waitForDisappear();
    }
    
    @Override
    public void waitForDisappear(Duration timeout) {
        waits.waitForDisappear(timeout);
    }
    
    @Override
    public void waitForElementEnabled() {
        waits.waitForElementEnabled();
    }
    
    @Override
    public void waitForElementEnabled(Duration timeout) {
        waits.waitForElementEnabled(timeout);
    }
    
    @Override
    public void waitForElementDisabled() {
        waits.waitForElementDisabled();
    }
    
    @Override
    public void waitForElementDisabled(Duration timeout) {
        waits.waitForElementDisabled(timeout);
    }
    
    @Override
    public void waitForTextToBePresent(String text) {
        waits.waitForTextToBePresent(text);
    }
    
    @Override
    public void waitForTextToBePresent(String text, Duration timeout) {
        waits.waitForTextToBePresent(text, timeout);
    }
    
    @Override
    public void waitForTextToBeNotPresent(String text) {
        waits.waitForTextToBeNotPresent(text);
    }
    
    @Override
    public void waitForTextToBeNotPresent(String text, Duration timeout) {
        waits.waitForTextToBeNotPresent(text, timeout);
    }
    
    @Override
    public void waitForValuePresentInAttribute(String attribute, String value) {
        waits.waitForValuePresentInAttribute(attribute, value);
    }
    
    @Override
    public void waitForValuePresentInAttribute(String attribute, String value, Duration timeout) {
        waits.waitForValuePresentInAttribute(attribute, value, timeout);
    }
    
    @Override
    public void waitForValueNotPresentInAttribute(String attribute, String value) {
        waits.waitForValueNotPresentInAttribute(attribute, value);
    }
    
    @Override
    public void waitForValueNotPresentInAttribute(String attribute, String value, Duration timeout) {
        waits.waitForValueNotPresentInAttribute(attribute, value, timeout);
    }
    
    @Override
    public void waitForStalenessOfElement() {
        waits.waitForStalenessOfElement();
    }
    
    @Override
    public void waitForStalenessOfElement(Duration timeout) {
        waits.waitForStalenessOfElement(timeout);
    }
    
    // Other methods
    @Override
    public Select getSelect() {
        return new Select(getElement());
    }
}

