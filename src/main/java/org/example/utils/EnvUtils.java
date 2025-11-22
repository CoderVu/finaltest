package org.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.Env;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;
@Slf4j

public final class EnvUtils {

    private EnvUtils() {}

    public static String readProperty(Env env, String propertyName) {
        Properties props = loadEnvProperties(env);
        return props.getProperty(propertyName);
    }

    private static Properties loadEnvProperties(Env env) {
        String fileName = env.name() + ".properties";

        InputStream input = EnvUtils.class.getClassLoader().getResourceAsStream(fileName);
        if (input == null) {
            throw new IllegalStateException("Missing environment properties file: " + fileName);
        }

        Properties props = new Properties();
        try {
            props.load(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return props;
    }

}

