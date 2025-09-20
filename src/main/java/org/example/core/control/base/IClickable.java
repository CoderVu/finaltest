package org.example.core.control.base;

public interface IClickable extends IBaseControl {

	void click();

	void click(int time);

	void click(int x, int y);

	void clickByJs();

	void doubleClick();
}