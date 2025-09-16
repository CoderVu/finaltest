package org.example.core.browser.firefox;

import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.logigear.driver.manager.RemoteDriverManager;

public class RemoteFirefoxDriver extends RemoteDriverManager {

	@Override
	public void createWebDriver(String key) throws Exception {
		try {
			FirefoxOptions ops = new FirefoxOptions();
			ops.merge(getCapabilities());
			ops.addArguments(getArguments());
			this.webDrivers.put(key, new RemoteWebDriver(getRemoteUrl(), ops));
		} catch (Exception ex) {
			System.out.println("Getting error when creating Web Driver: " + ex.getMessage());
			throw new Exception(ex.getMessage());
		}
	}
}
