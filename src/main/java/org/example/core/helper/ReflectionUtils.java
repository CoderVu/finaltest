package org.example.core.helper;

import com.logigear.common.Constants;
import com.logigear.driver.DriverProperty;
import com.logigear.driver.RunningMode;

import java.lang.reflect.Method;

public class ReflectionUtils {

	public static Object initWebDriver(String driverKey, DriverProperty property) {

		RunningMode mode = property.isRemoteMode() ? RunningMode.Remote
				: RunningMode.Local;
		String packageName = String.format(Constants.BROWSER_PACKAGE_NAME,
				property.getDriverType().toString().toLowerCase());
		String className = String.format(Constants.BROWSER_CLASS_NAME, mode,
				property.getDriverType().toString());

		try {
			Method method;
			String fullClassName = packageName + "." + className;
			Class<?> clzz = Class.forName(fullClassName);
			Object obj = clzz.newInstance();

			if (property.isRemoteMode()) {
				// Set Remote Url
				Method setRemoteUrlMethod = clzz.getSuperclass()
						.getDeclaredMethod("setRemoteUrl",
								String.class);
				setRemoteUrlMethod.invoke(obj, property.getRemoteUrl());

			}

			// Set Capability
			Method setCapabilitiesMethod = clzz
					.getSuperclass()
					.getSuperclass()
					.getDeclaredMethod("setCapabilities",
							String.class);
			setCapabilitiesMethod.invoke(obj, property.getCapabilities());

			// Set Arguments
			Method setArguments = clzz
					.getSuperclass()
					.getSuperclass()
					.getDeclaredMethod("setArguments",
							String.class);
			setArguments.invoke(obj, property.getArguments());

			// Create Web Driver
			method = clzz
					.getDeclaredMethod("createWebDriver", String.class);
			method.invoke(obj, driverKey);
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
