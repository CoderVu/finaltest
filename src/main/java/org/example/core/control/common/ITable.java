package org.example.core.control.common;

import org.example.core.control.base.IBaseControl;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;

public interface ITable extends IBaseControl {
    
    int getRowCount();
    
    int getColumnCount();
    
    List<WebElement> getRows();
    
    List<WebElement> getHeaderCells();
    
    WebElement getCell(int row, int column);
    
    String getCellText(int row, int column);
    
    List<String> getColumnData(int columnIndex);
    
    List<String> getRowData(int rowIndex);
    
    Optional<Integer> findRowByText(String text);
    
    Optional<Integer> findColumnByHeaderText(String headerText);
    
    boolean containsText(String text);
}
