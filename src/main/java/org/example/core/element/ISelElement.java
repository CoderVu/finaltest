package org.example.core.element;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

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
    String getValue();
    String getAttribute(String attributeName);
    String getClassName();
    String getTagName();
    WebElement getChildElement(String xpath);
    List<WebElement> getChildElements();
    List<WebElement> getChildElements(String xpath);
    
    // Other
    void setAttributeJS(String attributeName, String value);
    Select getSelect();
}

