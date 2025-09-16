package org.example.core.control.common.imp;

import org.example.core.control.base.imp.BaseControl;
import org.example.core.control.base.imp.Editable;
import org.example.core.control.common.ITextBox;
import org.openqa.selenium.By;

public class TextBox extends Editable implements ITextBox {

	public TextBox(String locator) {
		super(locator);
	}

	public TextBox(By locator) {
		super(locator);
	}

	public TextBox(String locator, Object... value) {
		super(locator, value);
	}

	public TextBox(BaseControl parent, String locator) {
		super(parent, locator);
	}

	public TextBox(BaseControl parent, By locator) {
		super(parent, locator);
	}

	public TextBox(BaseControl parent, String locator, Object... value) {
		super(parent, locator, value);
	}
}
