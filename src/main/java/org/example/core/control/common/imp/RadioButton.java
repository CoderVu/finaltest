package org.example.core.control.common.imp;

import org.example.core.control.base.imp.BaseControl;
import org.example.core.control.base.imp.Editable;
import org.example.core.control.common.IRadioButton;
import org.openqa.selenium.By;

public class RadioButton extends Editable implements IRadioButton {

	public RadioButton(String locator) {
		super(locator);
	}

	public RadioButton(By locator) {
		super(locator);
	}

	public RadioButton(String locator, Object... value) {
		super(locator, value);
	}

	public RadioButton(BaseControl parent, String locator) {
		super(parent, locator);
	}

	public RadioButton(BaseControl parent, By locator) {
		super(parent, locator);
	}

	public RadioButton(BaseControl parent, String locator, Object... value) {
		super(parent, locator, value);
	}

	@Override
	public void check() {
		click();
	}

	@Override
	public boolean isChecked() {
		return isSelected();
	}
}