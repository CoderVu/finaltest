package org.example.core.control.base.imp;

import org.example.core.control.base.IEditable;
import org.openqa.selenium.By;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public
 class Editable extends Clickable implements IEditable {

	public Editable(String locator) {
		super(locator);
	}

	public Editable(By locator) {
		super(locator);
	}

	public Editable(String locator, Object... value) {
		super(locator, value);
	}

	public Editable(BaseControl parent, String locator) {
		super(parent, locator);
	}

	public Editable(BaseControl parent, By locator) {
		super(parent, locator);
	}

	public Editable(BaseControl parent, String locator, Object... value) {
		super(parent, locator, value);
	}

	@Override
	public void enter(CharSequence... value) {
		try {
			log.debug(String.format("Enter '%s' for %s", value, getLocator().toString()));
			getElement().sendKeys(value);
		} catch (Exception e) {
			log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
			throw e;
		}
	}

	@Override
	public void setValue(String value) {
		try {
			String js = String.format("arguments[0].value='%s';", value);
			log.debug(String.format("Set value '%s' for %s", value, getLocator().toString()));
			jsExecutor().executeScript(js, getElement());
		} catch (Exception e) {
			log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
			throw e;
		}
	}

	@Override
	public void clear() {
		try {
			log.debug(String.format("Clean text for %s", getLocator().toString()));
			getElement().clear();
		} catch (Exception e) {
			log.error(String.format("Has error with control '%s': %s", getLocator().toString(), e.getMessage().split("\n")[0]));
			throw e;
		}
	}
	
}
