package org.example.core.control.common.imp;

import org.example.core.control.base.imp.BaseControl;
import org.example.core.control.base.imp.Clickable;
import org.example.core.control.common.IComboBox;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.stream.Collectors;

public class ComboBox extends Clickable implements IComboBox {

	public ComboBox(String locator) {
		super(locator);
	}

	public ComboBox(By locator) {
		super(locator);
	}

	public ComboBox(String locator, Object... value) {
		super(locator, value);
	}

	public ComboBox(BaseControl parent, String locator) {
		super(parent, locator);
	}

	public ComboBox(BaseControl parent, By locator) {
		super(parent, locator);
	}

	public ComboBox(BaseControl parent, String locator, Object... value) {
		super(parent, locator, value);
	}

	private Select select() {
		return new Select(getElement());
	}

	@Override
	public void select(String text) {
		select().selectByVisibleText(text);
	}

	@Override
	public void select(int index) {
		select().selectByIndex(index);
	}

	@Override
	public String getSelected() {
		return select().getFirstSelectedOption().getText();
	}

	@Override
	public List<String> getOptions() {
		return select().getOptions().stream().map(WebElement::getText).collect(Collectors.toList());
	}

	@Override
	public int totalOptions() {
		return select().getOptions().size();
	}
}