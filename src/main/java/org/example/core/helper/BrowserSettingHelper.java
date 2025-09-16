package org.example.core.helper;

import com.logigear.driver.DriverProperty;
import com.logigear.driver.DriverType;
import com.logigear.driver.RunningMode;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import java.io.FileReader;

public class BrowserSettingHelper {
	public static DriverProperty getDriverProperty(String file, String sectionName)
			throws Exception {
		DriverProperty property = new DriverProperty();

		Ini ini = new Ini(new FileReader(file));
		Section section = ini.get(sectionName);
		if (section == null) {
			throw new Exception(String.format("Cannot find '%s' in file '%s'", sectionName, file));
		}
		String runningMode = section.get("mode");
		String driverType = section.get("driver");
		String remoteUrl = section.get("remoteUrl");
		String capabilities = section.get("capabilities");
		String args = section.get("arguments");
		property.setDriverType(convertStringToDriverType(driverType));
		property.setRemoteUrl(remoteUrl);
		property.setRunningMode(convertStringToRunningMode(runningMode));
		property.setCapabilities(capabilities);
		property.setArguments(args);

		return property;
	}

	private static RunningMode convertStringToRunningMode(String mode) throws Exception {
		try {
			return RunningMode.valueOf(mode);
		} catch (Exception e) {
			throw new Exception(String.format("Don't allow the '%s'. Please use %s for your configuration", mode,
					RunningMode.asString()));
		}
	}

	private static DriverType convertStringToDriverType(String type) throws Exception {
		try {
			return DriverType.valueOf(type);
		} catch (Exception e) {
			throw new Exception(String.format("Don't allow the '%s'. Please use %s for your configuration", type,
					DriverType.asString()));
		}
	}
}
