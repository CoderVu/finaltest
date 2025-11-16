package org.example.core.control.base;

import org.example.core.control.element.Element;
import org.example.enums.WaitType;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;

public interface IBaseControl {

    void dragAndDrop(int xOffset, int yOffset);

    void dragAndDrop(Element target);

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

    void waitForDisappear(int timeOutInSeconds, WaitType waitType);

    void waitForDisplay();

    void waitForDisplay(int timeOutInSeconds, WaitType waitType);

    void waitForElementVisible();

    void waitForElementVisible(int timeOutInSeconds, WaitType waitType);

    void waitForElementClickable();

    void waitForElementClickable(int timeOutInSecond);

    void waitForElementClickable(int timeOutInSeconds, WaitType waitType);

    void waitForElementDisabled(int timeOutInSecond);

    void waitForElementDisabled();

    void waitForElementDisabled(int timeOutInSeconds, WaitType waitType);

    void waitForElementEnabled(int timeOutInSecond);

    void waitForElementEnabled();

    void waitForElementEnabled(int timeOutInSeconds, WaitType waitType);

    void waitForInvisibility();

    void waitForInvisibility(int timeOutInSeconds, WaitType waitType);

    void waitForTextToBeNotPresent(String text);

    void waitForTextToBeNotPresent(String text, int timeOutInSecond, WaitType waitType);

    void waitForTextToBePresent(String text);

    void waitForTextToBePresent(String text, int timeOutInSecond, WaitType waitType);

    void waitForValueNotPresentInAttribute(String attribute, String value);

    void waitForValueNotPresentInAttribute(String attribute, String value, int timeOutInSecond, WaitType waitType);

    void waitForValuePresentInAttribute(String attribute, String value);

    void waitForValuePresentInAttribute(String attribute, String value, int timeOutInSecond, WaitType waitType);

    void waitForVisibility();

    void waitForVisibility(int timeOutInSeconds, WaitType waitType);

    void waitForStalenessOfElement();

    void waitForStalenessOfElement(int timeOutInSeconds, WaitType waitType);

    void checkCheckBox();

    void uncheckCheckBox();

    void checkCheckBoxByJs();

    void uncheckCheckBoxByJs();

    void setCheckBox(boolean value);

    void setAllCheckBoxes(boolean value);

    boolean isCheckBoxChecked();

    void selectComboBox(String text);

    void selectComboBoxByIndex(int index);

    String getComboBoxSelected();

    List<String> getComboBoxOptions();

    int getComboBoxTotalOptions();

    void switchToFrame();

    void switchToMainDocument();

    String getLinkReference();

    String getImageSource();

    String getImageAlt();

    int getTableRowCount();

    int getTableColumnCount();

    List<WebElement> getTableRows();

    List<WebElement> getTableHeaderCells();

    List<WebElement> getTableFirstRowCells();

    WebElement getTableCell(int row, int column);

    String getTableCellText(int row, int column);

    List<String> getTableColumnData(int columnIndex);

    List<String> getTableRowData(int rowIndex);

    Optional<Integer> findTableRowByText(String text);

    Optional<Integer> findTableColumnByHeaderText(String headerText);
}