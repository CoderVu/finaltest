package org.example.core.control.common.imp;

import lombok.extern.slf4j.Slf4j;
import org.example.core.control.base.imp.BaseControl;
import org.example.core.control.common.ITable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class Table extends BaseControl implements ITable {

    private Table(String locator) {
        super(locator);
    }

    private Table(By locator) {
        super(locator);
    }

    private Table(String locator, Object... value) {
        super(locator, value);
    }

    private Table(BaseControl parent, String locator) {
        super(parent, locator);
    }

    private Table(BaseControl parent, By locator) {
        super(parent, locator);
    }

    private Table(BaseControl parent, String locator, Object... value) {
        super(parent, locator, value);
    }

    @Override
    public int getRowCount() {
        return getRows().size();
    }

    @Override
    public int getColumnCount() {
        List<WebElement> headerCells = getHeaderCells();
        return headerCells.isEmpty() ? getFirstRowCells().size() : headerCells.size();
    }

    @Override
    public List<WebElement> getRows() {
        return getElement().findElements(By.xpath(".//tr"));
    }

    @Override
    public List<WebElement> getHeaderCells() {
        return getElement().findElements(By.xpath(".//thead//tr//th | .//tr[1]//th"));
    }

    public List<WebElement> getFirstRowCells() {
        return getElement().findElements(By.xpath(".//tr[1]//td"));
    }

    @Override
    public WebElement getCell(int row, int column) {
        validateRowColumn(row, column);
        return getElement().findElement(By.xpath(".//tr[" + (row + 1) + "]//td[" + (column + 1) + "]"));
    }

    @Override
    public String getCellText(int row, int column) {
        return getCell(row, column).getText();
    }

    @Override
    public List<String> getColumnData(int columnIndex) {
        validateColumn(columnIndex);
        List<WebElement> cells = getElement().findElements(By.xpath(".//tr//td[" + (columnIndex + 1) + "]"));
        List<String> columnData = new ArrayList<>();
        for (WebElement cell : cells) {
            columnData.add(cell.getText());
        }
        return columnData;
    }

    @Override
    public List<String> getRowData(int rowIndex) {
        validateRow(rowIndex);
        List<WebElement> cells = getElement().findElements(By.xpath(".//tr[" + (rowIndex + 1) + "]//td"));
        List<String> rowData = new ArrayList<>();
        for (WebElement cell : cells) {
            rowData.add(cell.getText());
        }
        return rowData;
    }

    @Override
    public Optional<Integer> findRowByText(String text) {
        validateNotNull(text, "Text cannot be null");
        List<WebElement> rows = getRows();
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getText().contains(text)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Integer> findColumnByHeaderText(String headerText) {
        validateNotNull(headerText, "Header text cannot be null");
        List<WebElement> headers = getHeaderCells();
        for (int i = 0; i < headers.size(); i++) {
            if (headerText.equals(headers.get(i).getText())) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean containsText(String text) {
        return getText().contains(text);
    }

    // Fluent API methods
    public Table shouldHave(TableCondition condition) {
        condition.check(this);
        return this;
    }

    public Table should(TableCondition condition) {
        return shouldHave(condition);
    }

    // Validation methods
    private void validateRow(int row) {
        if (row < 0 || row >= getRowCount()) {
            throw new IndexOutOfBoundsException("Row index " + row + " is out of bounds. Total rows: " + getRowCount());
        }
    }

    private void validateColumn(int column) {
        if (column < 0 || column >= getColumnCount()) {
            throw new IndexOutOfBoundsException("Column index " + column + " is out of bounds. Total columns: " + getColumnCount());
        }
    }

    private void validateRowColumn(int row, int column) {
        validateRow(row);
        validateColumn(column);
    }

    private void validateNotNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    // Condition interface for fluent assertions
    public interface TableCondition {
        void check(Table table);

        static TableCondition rowCount(int expectedCount) {
            return table -> {
                int actualCount = table.getRowCount();
                if (actualCount != expectedCount) {
                    throw new AssertionError("Expected " + expectedCount + 
                            " rows, but found " + actualCount + " in " + table.getLocator());
                }
            };
        }

        static TableCondition columnCount(int expectedCount) {
            return table -> {
                int actualCount = table.getColumnCount();
                if (actualCount != expectedCount) {
                    throw new AssertionError("Expected " + expectedCount + 
                            " columns, but found " + actualCount + " in " + table.getLocator());
                }
            };
        }

        static TableCondition cellText(int row, int column, String expectedText) {
            return table -> {
                String actualText = table.getCellText(row, column);
                if (!expectedText.equals(actualText)) {
                    throw new AssertionError("Expected cell[" + row + "," + column + "] text: '" + expectedText + 
                            "', but was: '" + actualText + "' in " + table.getLocator());
                }
            };
        }

        static TableCondition containsText(String expectedText) {
            return table -> {
                if (!table.containsText(expectedText)) {
                    throw new AssertionError("Table should contain text: '" + expectedText + 
                            "' but it was not found in " + table.getLocator());
                }
            };
        }
    }
}
