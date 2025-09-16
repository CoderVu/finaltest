package org.example.core.browser.edge;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import com.logigear.driver.manager.LocalDriverManager;

public class LocalEdgeDriver extends LocalDriverManager {

	@Override
	public void createWebDriver(String key) throws Exception {
		try {
			WebDriverManager.edgedriver().setup();
			EdgeOptions ops = new EdgeOptions();
			ops.merge(getCapabilities());
			this.webDrivers.put(key, new EdgeDriver(ops));
		} catch (Exception ex) {
			System.out.println("Getting error when creating Web Driver: " + ex.getMessage());
			throw new Exception(ex.getMessage());
		}
	}
}
