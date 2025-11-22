package org.example.core.element;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

public interface IElementWrapper {
    // Locator
    By getLocator();
    
    // Core element access
    WebElement getElement();
    List<WebElement> getElements();
    
    // Actions
    void click();
    void click(int times);
    void click(int x, int y);
    void clickByJs();
    void doubleClick();
    void setText(String text);
    void clear();
    void enter(CharSequence... value);
    void sendKeys(Keys key);
    void submit();
    void focus();
    void checkCheckBoxByJs();
    
    // Drag & Drop
    void dragAndDrop(int xOffset, int yOffset);
    void dragAndDrop(IElementWrapper target);
    
    // Move/Hover
    void moveTo();
    void moveTo(int x, int y);
    void moveToCenter();
    void mouseHoverJScript();
    
    // Scroll
    void scrollElementToCenterScreen();
    void scrollToView();
    void scrollToView(int offsetX, int offsetY);
    
    // Getters
    String getText();
    String getValue();
    String getAttribute(String attributeName);
    String getClassName();
    String getTagName();
    WebElement getChildElement(String xpath);
    List<WebElement> getChildElements();
    List<WebElement> getChildElements(String xpath);
    
    // Checks
    boolean isVisible();
    boolean isVisible(Duration timeout);
    boolean isEnabled();
    boolean isSelected();
    boolean isClickable();
    boolean isExist();
    boolean isExist(Duration timeout);
    
    // Waits
    void waitForVisibility();
    void waitForVisibility(Duration timeout);
    void waitForElementVisible();
    void waitForElementVisible(Duration timeout);
    void waitForElementClickable();
    void waitForElementClickable(Duration timeout);
    void waitForDisplay();
    void waitForDisplay(Duration timeout);
    void waitForInvisibility();
    void waitForInvisibility(Duration timeout);
    void waitForDisappear();
    void waitForDisappear(Duration timeout);
    void waitForElementEnabled();
    void waitForElementEnabled(Duration timeout);
    void waitForElementDisabled();
    void waitForElementDisabled(Duration timeout);
    void waitForTextToBePresent(String text);
    void waitForTextToBePresent(String text, Duration timeout);
    void waitForTextToBeNotPresent(String text);
    void waitForTextToBeNotPresent(String text, Duration timeout);
    void waitForValuePresentInAttribute(String attribute, String value);
    void waitForValuePresentInAttribute(String attribute, String value, Duration timeout);
    void waitForValueNotPresentInAttribute(String attribute, String value);
    void waitForValueNotPresentInAttribute(String attribute, String value, Duration timeout);
    void waitForStalenessOfElement();
    void waitForStalenessOfElement(Duration timeout);
    
    // Other
    void setAttributeJS(String attributeName, String value);
    org.openqa.selenium.support.ui.Select getSelect();
}

