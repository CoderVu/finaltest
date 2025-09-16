package org.example.core.control.common.imp;

import org.example.core.control.base.imp.BaseControl;
import org.example.core.control.base.imp.Clickable;
import org.example.core.control.common.ILabel;
import org.openqa.selenium.By;

public class Label extends Clickable implements ILabel {

	public Label(String locator) {
		super(locator);
	}

	public Label(By locator) {
		super(locator);
	}

	public Label(String locator, Object... value) {
		super(locator, value);
	}

	public Label(BaseControl parent, String locator) {
		super(parent, locator);
	}

	public Label(BaseControl parent, By locator) {
		super(parent, locator);
	}

	public Label(BaseControl parent, String locator, Object... value) {
		super(parent, locator, value);
	}
}
