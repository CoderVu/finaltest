package org.example.core.control.factory;

import org.example.core.control.base.imp.BaseControl;
import org.example.core.control.common.imp.Element;
import org.openqa.selenium.By;
import org.example.core.control.common.imp.Button;
import org.example.core.control.common.imp.CheckBox;
import org.example.core.control.common.imp.ComboBox;
import org.example.core.control.common.imp.Frame;
import org.example.core.control.common.imp.Image;
import org.example.core.control.common.imp.Label;
import org.example.core.control.common.imp.Link;
import org.example.core.control.common.imp.RadioButton;
import org.example.core.control.common.imp.TextBox;
import java.lang.reflect.Constructor;

import static org.example.core.control.util.Locators.*;

public class ElementFactory {

    public Element element(By by) {
        return new Element(by);
    }

    public Element element(String locator) {
        return new Element(locator);
    }

    public Element elementByCss(String selector) {
        return new Element(css(selector));
    }

    public Element elementByXpath(String xp) {
        return new Element(xpath(xp));
    }

    public Element elementById(String idValue) {
        return new Element(id(idValue));
    }

    public Element elementByName(String nameValue) {
        return new Element(name(nameValue));
    }

    public Element elementByClass(String classNameValue) {
        return new Element(className(classNameValue));
    }

    // ------- Parent-scoped Element helpers

    public Element childElement(BaseControl parent, By by) {
        return new Element(parent, by);
    }

    public Element childElementByCss(BaseControl parent, String selector) {
        return new Element(parent, css(selector));
    }

    public Element childElementByXpath(BaseControl parent, String xp) {
        return new Element(parent, xpath(xp));
    }

    public Element childElementById(BaseControl parent, String idValue) {
        return new Element(parent, id(idValue));
    }

    public Element childElementByName(BaseControl parent, String nameValue) {
        return new Element(parent, name(nameValue));
    }

    public Element childElementByClass(BaseControl parent, String classNameValue) {
        return new Element(parent, className(classNameValue));
    }

    // ------- Generic creators for any BaseControl subclass (Button, TextBox, ...)

    public <T extends BaseControl> T create(Class<T> controlType, By by) {
        try {
            Constructor<T> ctor = controlType.getDeclaredConstructor(By.class);
            ctor.setAccessible(true);
            return ctor.newInstance(by);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot instantiate control type " + controlType.getSimpleName() + " with By constructor", e);
        }
    }

    public <T extends BaseControl> T createByCss(Class<T> controlType, String selector) {
        return create(controlType, css(selector));
    }

    public <T extends BaseControl> T createByXpath(Class<T> controlType, String xp) {
        return create(controlType, xpath(xp));
    }

    public <T extends BaseControl> T createById(Class<T> controlType, String idValue) {
        return create(controlType, id(idValue));
    }

    public <T extends BaseControl> T createByName(Class<T> controlType, String nameValue) {
        return create(controlType, name(nameValue));
    }

    public <T extends BaseControl> T createByClass(Class<T> controlType, String classNameValue) {
        return create(controlType, className(classNameValue));
    }

    public <T extends BaseControl> T createChild(Class<T> controlType, BaseControl parent, By by) {
        try {
            Constructor<T> ctor = controlType.getDeclaredConstructor(BaseControl.class, By.class);
            ctor.setAccessible(true);
            return ctor.newInstance(parent, by);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot instantiate child control type " + controlType.getSimpleName() + " with (BaseControl, By) constructor", e);
        }
    }

    public <T extends BaseControl> T createChildByCss(Class<T> controlType, BaseControl parent, String selector) {
        return createChild(controlType, parent, css(selector));
    }

    public <T extends BaseControl> T createChildByXpath(Class<T> controlType, BaseControl parent, String xp) {
        return createChild(controlType, parent, xpath(xp));
    }

    public <T extends BaseControl> T createChildById(Class<T> controlType, BaseControl parent, String idValue) {
        return createChild(controlType, parent, id(idValue));
    }

    public <T extends BaseControl> T createChildByName(Class<T> controlType, BaseControl parent, String nameValue) {
        return createChild(controlType, parent, name(nameValue));
    }

    public <T extends BaseControl> T createChildByClass(Class<T> controlType, BaseControl parent, String classNameValue) {
        return createChild(controlType, parent, className(classNameValue));
    }

    // ------- Typed helpers for common controls

    public Button button(By by) { return new Button(by); }
    public Button button(String locator) { return new Button(locator); }
    public Button buttonByCss(String selector) { return new Button(css(selector)); }
    public Button buttonByXpath(String xp) { return new Button(xpath(xp)); }
    public Button buttonById(String idValue) { return new Button(id(idValue)); }
    public Button buttonByName(String nameValue) { return new Button(name(nameValue)); }
    public Button buttonByClass(String classNameValue) { return new Button(className(classNameValue)); }

    public TextBox textBox(By by) { return new TextBox(by); }
    public TextBox textBox(String locator) { return new TextBox(locator); }
    public TextBox textBoxByCss(String selector) { return new TextBox(css(selector)); }
    public TextBox textBoxByXpath(String xp) { return new TextBox(xpath(xp)); }
    public TextBox textBoxById(String idValue) { return new TextBox(id(idValue)); }
    public TextBox textBoxByName(String nameValue) { return new TextBox(name(nameValue)); }
    public TextBox textBoxByClass(String classNameValue) { return new TextBox(className(classNameValue)); }

    public Link link(By by) { return new Link(by); }
    public Link link(String locator) { return new Link(locator); }
    public Link linkByCss(String selector) { return new Link(css(selector)); }
    public Link linkByXpath(String xp) { return new Link(xpath(xp)); }
    public Link linkById(String idValue) { return new Link(id(idValue)); }
    public Link linkByName(String nameValue) { return new Link(name(nameValue)); }
    public Link linkByClass(String classNameValue) { return new Link(className(classNameValue)); }

    public Label label(By by) { return new Label(by); }
    public Label label(String locator) { return new Label(locator); }
    public Label labelByCss(String selector) { return new Label(css(selector)); }
    public Label labelByXpath(String xp) { return new Label(xpath(xp)); }
    public Label labelById(String idValue) { return new Label(id(idValue)); }
    public Label labelByName(String nameValue) { return new Label(name(nameValue)); }
    public Label labelByClass(String classNameValue) { return new Label(className(classNameValue)); }

    public Image image(By by) { return new Image(by); }
    public Image image(String locator) { return new Image(locator); }
    public Image imageByCss(String selector) { return new Image(css(selector)); }
    public Image imageByXpath(String xp) { return new Image(xpath(xp)); }
    public Image imageById(String idValue) { return new Image(id(idValue)); }
    public Image imageByName(String nameValue) { return new Image(name(nameValue)); }
    public Image imageByClass(String classNameValue) { return new Image(className(classNameValue)); }

    public Frame frame(By by) { return new Frame(by); }
    public Frame frame(String locator) { return new Frame(locator); }
    public Frame frameByCss(String selector) { return new Frame(css(selector)); }
    public Frame frameByXpath(String xp) { return new Frame(xpath(xp)); }
    public Frame frameById(String idValue) { return new Frame(id(idValue)); }
    public Frame frameByName(String nameValue) { return new Frame(name(nameValue)); }
    public Frame frameByClass(String classNameValue) { return new Frame(className(classNameValue)); }

    public RadioButton radioButton(By by) { return new RadioButton(by); }
    public RadioButton radioButton(String locator) { return new RadioButton(locator); }
    public RadioButton radioButtonByCss(String selector) { return new RadioButton(css(selector)); }
    public RadioButton radioButtonByXpath(String xp) { return new RadioButton(xpath(xp)); }
    public RadioButton radioButtonById(String idValue) { return new RadioButton(id(idValue)); }
    public RadioButton radioButtonByName(String nameValue) { return new RadioButton(name(nameValue)); }
    public RadioButton radioButtonByClass(String classNameValue) { return new RadioButton(className(classNameValue)); }

    public CheckBox checkBox(By by) { return new CheckBox(by); }
    public CheckBox checkBox(String locator) { return new CheckBox(locator); }
    public CheckBox checkBoxByCss(String selector) { return new CheckBox(css(selector)); }
    public CheckBox checkBoxByXpath(String xp) { return new CheckBox(xpath(xp)); }
    public CheckBox checkBoxById(String idValue) { return new CheckBox(id(idValue)); }
    public CheckBox checkBoxByName(String nameValue) { return new CheckBox(name(nameValue)); }
    public CheckBox checkBoxByClass(String classNameValue) { return new CheckBox(className(classNameValue)); }

    public ComboBox comboBox(By by) { return new ComboBox(by); }
    public ComboBox comboBox(String locator) { return new ComboBox(locator); }
    public ComboBox comboBoxByCss(String selector) { return new ComboBox(css(selector)); }
    public ComboBox comboBoxByXpath(String xp) { return new ComboBox(xpath(xp)); }
    public ComboBox comboBoxById(String idValue) { return new ComboBox(id(idValue)); }
    public ComboBox comboBoxByName(String nameValue) { return new ComboBox(name(nameValue)); }
    public ComboBox comboBoxByClass(String classNameValue) { return new ComboBox(className(classNameValue)); }
}


