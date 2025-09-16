package org.example.core.browser.firefox;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import com.logigear.driver.manager.LocalDriverManager;

import io.github.bonigarcia.wdm.WebDriverManager;

public class LocalFirefoxDriver extends LocalDriverManager {

	@Override
	public void createWebDriver(String key) throws Exception {
		try {
			WebDriverManager.firefoxdriver().setup();
			FirefoxOptions ops = new FirefoxOptions();
			ops.merge(getCapabilities());
			ops.addArguments(getArguments());
			this.webDrivers.put(key, new FirefoxDriver(ops));
		} catch (Exception ex) {
			System.out.println("Getting error when creating Web Driver: " + ex.getMessage());
			throw new Exception(ex.getMessage());
		}
	}
}
