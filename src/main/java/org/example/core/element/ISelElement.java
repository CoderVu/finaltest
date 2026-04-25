package org.example.core.element;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;

public interface ISelElement {
    // Locator
    By getLocator();
    
    // Core element access
    WebElement getElement();
    List<WebElement> getElements();
    
    // Actions
    void click();
    void click(int x, int y);
    void clickByJs();
    void doubleClick();
    void setText(String text);
    default void type(String text) {
        setText(text);
    }
    void clear();
    void submit();
    void focus();
    void checkCheckBoxByJs();
    
    // Drag & Drop
    void dragAndDrop(int xOffset, int yOffset);
    void dragAndDrop(ISelElement target);
    
    // Move/Hover
    void moveTo();
    default void hover() {
        moveTo();
    }
    void moveTo(int x, int y);
    void moveToCenter();
    void mouseHoverJScript();
    
    // Scroll
    void scrollElementToCenterScreen();
    void scrollToView();
    void scrollToView(int offsetX, int offsetY);
    
    // Getters
    String getText();
    String waitForText(Duration timeout);
    String waitForValue(Duration timeout);
    String waitForAttribute(String attributeName, Duration timeout);
    String waitForClassName(Duration timeout);
    String waitForTagName(Duration timeout);
    int waitForCount(Duration timeout);
    String getValue();
    String getAttribute(String attributeName);
    String getClassName();
    String getTagName();
    WebElement getChildElement(String xpath);
    List<WebElement> getChildElements();
    List<WebElement> getChildElements(String xpath);
    
    // Checks
    boolean waitForVisible(Duration timeout);
    boolean waitForEnabled(Duration timeout);
    boolean waitForExist(Duration timeout);
    boolean waitForSelected(Duration timeout);
    boolean waitForClickable(Duration timeout);
    boolean waitForEditable(Duration timeout);
    boolean waitForReadOnly(Duration timeout);
    boolean isVisible();
    boolean isEnabled();
    boolean isSelected();
    boolean isClickable();
    boolean isExist();
    
    // Other
    void setAttributeJS(String attributeName, String value);
    Select getSelect();
}

