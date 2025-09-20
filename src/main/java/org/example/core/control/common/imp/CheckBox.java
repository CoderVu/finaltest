package org.example.core.control.common.imp;

import org.example.core.control.base.imp.BaseControl;
import org.example.core.control.base.imp.Editable;
import org.example.core.control.common.ICheckBox;
import org.example.core.control.util.DriverUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class CheckBox extends Editable implements ICheckBox {

	public CheckBox(String locator) {
		super(locator);
	}

	public CheckBox(By locator) {
		super(locator);
	}

	public CheckBox(String locator, Object... value) {
		super(locator, value);
	}

	public CheckBox(BaseControl parent, String locator) {
		super(parent, locator);
	}

	public CheckBox(BaseControl parent, By locator) {
		super(parent, locator);
	}

	public CheckBox(BaseControl parent, String locator, Object... value) {
		super(parent, locator, value);
	}

	@Override
	public void check() {
		if (!isSelected()) {
			click();
			DriverUtils.delay(1);
		}
	}

	@Override
	public void uncheck() {
		if (isSelected()) {
			click();
			DriverUtils.delay(1);
		}
	}

	@Override
	public void checkByJs() {
		jsExecutor().executeScript("arguments[0].checked=true; arguments[0].dispatchEvent(new Event('change'));", getElement());
	}

	@Override
	public void uncheckByJs() {
		jsExecutor().executeScript("arguments[0].checked=false; arguments[0].dispatchEvent(new Event('change'));", getElement());
	}

	@Override
	public void set(boolean value) {
		if (value && !isSelected()) {
			check();
		} else if (!value && isSelected()) {
			uncheck();
		}
	}

	@Override
	public void setAll(boolean value) {
		for (WebElement el : getElements()) {
			boolean selected = el.isSelected();
			if (value != selected) {
				el.click();
				DriverUtils.delay(1);
			}
		}
	}

	@Override
	public boolean isChecked() {
		return isSelected();
	}
}