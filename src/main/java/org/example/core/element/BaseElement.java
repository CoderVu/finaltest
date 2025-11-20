package org.example.core.element;

import lombok.extern.slf4j.Slf4j;
import org.example.core.element.locator.LocatorResolver;
import org.openqa.selenium.*;

import java.util.List;

import static org.example.core.element.util.DriverUtils.getWebDriver;

@Slf4j
public abstract class BaseElement implements IElement {
    
    protected String locator;
    protected By byLocator;
    protected String dynamicLocator;
    protected IElement parent;
    
    protected BaseElement(String locator) {
        this.locator = locator;
        this.dynamicLocator = locator;
        this.byLocator = LocatorResolver.resolve(locator);
    }
    
    protected BaseElement(By byLocator) {
        this.byLocator = byLocator;
        this.locator = byLocator.toString();
    }
    
    protected BaseElement(String locator, Object... args) {
        this.dynamicLocator = locator;
        this.locator = String.format(dynamicLocator, args);
        this.byLocator = LocatorResolver.resolve(this.locator);
    }
    
    protected BaseElement(IElement parent, String locator) {
        this.locator = locator;
        this.dynamicLocator = locator;
        this.byLocator = LocatorResolver.resolve(locator);
        this.parent = parent;
    }
    
    protected BaseElement(IElement parent, By byLocator) {
        this.byLocator = byLocator;
        this.parent = parent;
        this.locator = byLocator.toString();
    }
    
    protected BaseElement(IElement parent, String locator, Object... args) {
        this.dynamicLocator = locator;
        this.locator = String.format(dynamicLocator, args);
        this.byLocator = LocatorResolver.resolve(this.locator);
        this.parent = parent;
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
            log.error("StaleElementReferenceException '{}': {}", getLocator().toString(), 
                    e.getMessage() != null ? e.getMessage().split("\n")[0] : "");
            return getElement();
        }
    }
    
    @Override
    public List<WebElement> getElements() {
        if (parent != null) {
            return parent.getElement().findElements(getLocator());
        }
        return getWebDriver().findElements(getLocator());
    }
    
    @Override
    public boolean isDynamicLocator() {
        return this.locator != null && this.locator.toLowerCase().matches("(.*)%[s|d](.*)");
    }
    
    @Override
    public void setDynamicValue(Object... args) {
        this.locator = String.format(this.dynamicLocator, args);
        this.byLocator = LocatorResolver.resolve(this.locator);
    }
    
    protected JavascriptExecutor jsExecutor() {
        return (JavascriptExecutor) getWebDriver();
    }
    
    protected WebElement getParentElement() {
        return parent != null ? parent.getElement() : null;
    }
}

