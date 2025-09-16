package org.example.core.control.base;

import org.example.core.control.base.imp.BaseControl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

public interface IBaseControl {

    void dragAndDrop(int xOffset, int yOffset);

    void dragAndDrop(BaseControl target);

    void focus();

    String getAttribute(String attributeName);

    WebElement getChildElement(String xpath);

    List<WebElement> getChildElements();

    List<WebElement> getChildElements(String xpath);

    String getClassName();

    WebElement getElement();

    List<WebElement> getElements();

    By getLocator();

    String getLocatorString();

    String getTagName();

    String getText();

    void setText(String text);

    String getValue();

    boolean isClickable();

    boolean isDynamicLocator();

    boolean isEnabled();

    boolean isExist();

    boolean isExist(int timeOutInSeconds);

    boolean isSelected();

    boolean isVisible();

    boolean isVisible(int timeOutInSeconds);

    void mouseHoverJScript();

    void moveTo();

    void moveTo(int x, int y);

    void moveToCenter();

    void scrollElementToCenterScreen();

    void scrollToView();

    void scrollToView(int offsetX, int offsetY);

    void setAttributeJS(String attributeName, String value);

    void setDynamicValue(Object... args);

    void submit();

    void waitForDisappear();

    void waitForDisappear(int timeOutInSeconds);

    void waitForDisplay();

    void waitForDisplay(int timeOutInSeconds);

    void waitForElementClickable();

    void waitForElementClickable(int timeOutInSeconds);

    void waitForElementDisabled(int timeOutInSecond);

    void waitForElementDisabled();

    void waitForElementEnabled(int timeOutInSecond);

    void waitForElementEnabled();

    void waitForInvisibility();

    void waitForInvisibility(int timeOutInSeconds);

    void waitForTextToBeNotPresent(String text, int timeOutInSecond);

    void waitForTextToBePresent(String text, int timeOutInSecond);

    void waitForValueNotPresentInAttribute(String attribute, String value, int timeOutInSecond);

    void waitForValuePresentInAttribute(String attribute, String value, int timeOutInSecond);

    void waitForVisibility();

    void waitForVisibility(int timeOutInSeconds);

    void waitForStalenessOfElement();

    void waitForStalenessOfElement(int timeOutInSeconds);
}
