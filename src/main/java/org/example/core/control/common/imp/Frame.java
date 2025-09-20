package org.example.core.control.common.imp;

import org.example.core.control.base.imp.BaseControl;
import org.example.core.control.common.IFrame;
import org.openqa.selenium.By;

public class Frame extends BaseControl implements IFrame {

	public Frame(String locator) {
		super(locator);
	}

	public Frame(By locator) {
		super(locator);
	}

	public Frame(String locator, Object... value) {
		super(locator, value);
	}

	public Frame(BaseControl parent, String locator) {
		super(parent, locator);
	}

	public Frame(BaseControl parent, By locator) {
		super(parent, locator);
	}

	public Frame(BaseControl parent, String locator, Object... value) {
		super(parent, locator, value);
	}

	@Override
	public void switchTo() {
		getDriver().switchTo().frame(getElement());
	}

	@Override
	public void switchToMainDocument() {
		getDriver().switchTo().defaultContent();
	}

}