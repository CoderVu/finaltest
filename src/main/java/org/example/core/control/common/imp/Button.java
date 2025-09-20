package org.example.core.control.common.imp;

import org.example.core.control.base.imp.BaseControl;
import org.example.core.control.base.imp.Clickable;
import org.example.core.control.common.IButton;
import org.openqa.selenium.By;

public class Button extends Clickable implements IButton {
	public Button(String locator) {
		super(locator);
	}

	public Button(By locator) {
		super(locator);
	}

	public Button(String locator, Object... value) {
		super(locator, value);
	}

	public Button(BaseControl parent, String locator) {
		super(parent, locator);
	}

	public Button(BaseControl parent, By locator) {
		super(parent, locator);
	}

	public Button(BaseControl parent, String locator, Object... value) {
		super(parent, locator, value);
	}
}