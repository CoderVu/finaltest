package org.example.core.driver.manager;

import com.logigear.driver.DriverProperty;
import com.logigear.helper.JsonHelper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DriverManager {
	
	public DriverManager() {
		webDrivers = new HashMap<String, WebDriver>();
		driverProperty = new HashMap<String, DriverProperty>();
	}

	protected Map<String, WebDriver> webDrivers;
	protected Map<String, DriverProperty> driverProperty;

	private String capabilities;
	private String arguments;
	private String mainWindow;
	
	public WebDriver getWebDriver(String key) {
		return webDrivers.get(key);
	}
	
	public DriverProperty getDriverProperty(String key) {
		return driverProperty.get(key);
	}
	
	protected void addWebDriver(Map<String, WebDriver> drivers) {
		webDrivers.putAll(drivers);
	}
	
	protected void addDriverProperty(String key, DriverProperty property) {
		driverProperty.put(key, property);
	}

	protected abstract void createWebDriver(String key) throws Exception;
	
	protected DesiredCapabilities getCapabilities() {
		return JsonHelper.convertJsonToCapabilities(this.capabilities);
	}

	public void setCapabilities(String caps) {
		this.capabilities = caps;
	}

	protected List<String> getArguments() {
		return JsonHelper.convertJsonToArguments(this.arguments);
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

	public void quitAll() {
		for (String key : webDrivers.keySet())
			getWebDriver(key).quit();
	}

	public String getMainWindow() {
		return mainWindow;
	}

	public void setMainWindow(String mainWindow) {
		this.mainWindow = mainWindow;
	}
}
