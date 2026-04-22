package org.example.utils;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.example.utils.DriverUtils.getWebDriver;

@Slf4j
public final class CookieUtils {

    private CookieUtils() {
    }

    public static boolean exists(Path cookieFile) {
        return cookieFile != null && Files.exists(cookieFile);
    }

    public static void saveCookies(Path cookieFile) {
        if (cookieFile == null) {
            throw new IllegalArgumentException("cookieFile must not be null");
        }
        WebDriver driver = getWebDriver();
        List<String> lines = new ArrayList<>();
        for (Cookie cookie : driver.manage().getCookies()) {
            long expiryEpoch = cookie.getExpiry() == null ? -1L : cookie.getExpiry().getTime();
            String line = String.join("\t",
                    safe(cookie.getName()),
                    safe(cookie.getValue()),
                    safe(cookie.getDomain()),
                    safe(cookie.getPath()),
                    String.valueOf(expiryEpoch),
                    String.valueOf(cookie.isSecure()),
                    String.valueOf(cookie.isHttpOnly()));
            lines.add(line);
        }

        try {
            if (cookieFile.getParent() != null) {
                Files.createDirectories(cookieFile.getParent());
            }
            Files.write(cookieFile, lines, StandardCharsets.UTF_8);
            log.info("Saved {} cookies to {}", lines.size(), cookieFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save cookies to " + cookieFile, e);
        }
    }

    public static int loadCookies(Path cookieFile) {
        if (!exists(cookieFile)) {
            return 0;
        }
        WebDriver driver = getWebDriver();
        List<String> lines;
        try {
            lines = Files.readAllLines(cookieFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read cookie file " + cookieFile, e);
        }

        int loaded = 0;
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            String[] parts = line.split("\t", -1);
            if (parts.length < 7) {
                continue;
            }

            Date expiry = parseExpiry(parts[4]);
            Cookie cookie = new Cookie(parts[0], parts[1], parts[2], parts[3], expiry, Boolean.parseBoolean(parts[5]), Boolean.parseBoolean(parts[6]));
            try {
                driver.manage().addCookie(cookie);
                loaded++;
            } catch (Exception e) {
                log.debug("Skip cookie '{}' due to: {}", cookie.getName(), e.getMessage());
            }
        }
        log.info("Loaded {} cookies from {}", loaded, cookieFile);
        return loaded;
    }

    private static Date parseExpiry(String rawEpoch) {
        try {
            long epoch = Long.parseLong(rawEpoch);
            return epoch <= 0 ? null : new Date(epoch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("\t", " ").replace("\n", " ");
    }
}
