package org.example.core.control.base;

public interface IEditable extends IClickable {

	void setValue(String value);

	void enter(CharSequence... value);

	void clear();
}