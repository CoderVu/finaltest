package org.example.core.control.common.imp;

import org.example.core.control.base.imp.BaseControl;
import org.example.core.control.base.imp.Editable;
import org.example.core.control.common.ILink;
import org.openqa.selenium.By;

public class Link extends Editable implements ILink {

	public Link(String locator) {
		super(locator);
	}

	public Link(By locator) {
		super(locator);
	}

	public Link(String locator, Object... value) {
		super(locator, value);
	}

	public Link(BaseControl parent, String locator) {
		super(parent, locator);
	}

	public Link(BaseControl parent, By locator) {
		super(parent, locator);
	}

	public Link(BaseControl parent, String locator, Object... value) {
		super(parent, locator, value);
	}

	public String getReference() {
		return getAttribute("href");
	}

}