package org.example.core.control.element;

import org.example.core.control.base.imp.BaseControl;
import org.openqa.selenium.By;

public class Element extends BaseControl {

	public Element(String locator) {
		super(locator);
	}

	public Element(By locator) {
		super(locator);
	}

	public Element(String locator, Object... value) {
		super(locator, value);
	}

	public Element(BaseControl parent, String locator) {
		super(parent, locator);
	}

	public Element(BaseControl parent, By locator) {
		super(parent, locator);
	}

	public Element(BaseControl parent, String locator, Object... value) {
		super(parent, locator, value);
	}
}