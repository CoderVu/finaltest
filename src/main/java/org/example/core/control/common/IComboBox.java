package org.example.core.control.common;

import org.example.core.control.base.IClickable;

import java.util.List;

public interface IComboBox extends IClickable {
	void select(String text);

	void select(int index);

	String getSelected();

	List<String> getOptions();

	int totalOptions();
}
