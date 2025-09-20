package org.example.core.control.common;

import org.example.core.control.base.IEditable;

public interface ICheckBox extends IEditable {

	void check();

	void uncheck();
	
	void checkByJs();
	
	void uncheckByJs();

	void set(boolean value);

	void setAll(boolean value);

	boolean isChecked();
}