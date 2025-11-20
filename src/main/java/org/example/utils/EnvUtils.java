package org.example.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public final class EnvUtils {

    private static final Properties PROPS = new Properties();

    private EnvUtils() {}

    public static void load(String filePath) {
        synchronized (EnvUtils.class) {
            PROPS.clear();
            try (InputStream inputStream = locateResource(filePath)) {
                if (inputStream == null) {
                    throw new IllegalStateException("Properties file not found: " + filePath);
                }
                PROPS.load(inputStream);
                log.info("Loaded properties from {}", filePath);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load: " + filePath, e);
            }
        }
    }

    public static boolean isLoaded() {
        return !PROPS.isEmpty();
    }

    public static String get(String key) {
        String value = PROPS.getProperty(key);
        if (value != null) {
            return value.trim();
        }
        value = System.getProperty(key);
        return value != null ? value.trim() : null;
    }

    private static InputStream locateResource(String filePath) {
        InputStream classpathStream = EnvUtils.class.getClassLoader().getResourceAsStream(filePath);
        if (classpathStream != null) {
            return classpathStream;
        }

        File file = new File("src/test/resources/" + filePath);
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (Exception e) {
                log.warn("Cannot read file: {}", file.getAbsolutePath());
            }
        }
        return null;
    }
}

