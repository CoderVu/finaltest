package org.example.report;

import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.openqa.selenium.support.events.WebDriverListener;
import org.testng.IExecutionListener;
import org.testng.ITestListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LogConfig implements ITestListener, IExecutionListener, WebDriverListener {

	private PrintStream originalOut;
	private PrintStream originalErr;
	private FileOutputStream logFileOut;
	private File terminalLogFile;
	private static final long DEDUP_WINDOW_MS = 300;
	private final ThreadLocal<Map<String, Long>> lastLogTimes = ThreadLocal.withInitial(HashMap::new);

	private boolean shouldLog(String messageKey) {
		try {
			Map<String, Long> map = lastLogTimes.get();
			long now = System.currentTimeMillis();
			Long last = map.get(messageKey);
			if (last == null || now - last > DEDUP_WINDOW_MS) {
				map.put(messageKey, now);
				return true;
			}
			return false;
		} catch (Throwable t) {
			return true;
		}
	}

	private boolean shouldLogLocator(String locatorKey) {
		return shouldLog("locator:" + locatorKey);
	}

	@Override
	public void onExecutionStart() {
		try {
			terminalLogFile = new File("target/allure-results/terminal.log");
			if (terminalLogFile.exists() && !terminalLogFile.delete()) {
				log.warn("Could not delete existing terminal.log; appending to it");
			}
			if (!terminalLogFile.exists()) {
				File parent = terminalLogFile.getParentFile();
				if (parent != null) parent.mkdirs();
				terminalLogFile.createNewFile();
			}
			logFileOut = new FileOutputStream(terminalLogFile, true);
			originalOut = System.out;
			originalErr = System.err;
			PrintStream teeOut = new PrintStream(new TeeOutputStream(originalOut, logFileOut), true);
			PrintStream teeErr = new PrintStream(new TeeOutputStream(originalErr, logFileOut), true);
			System.setOut(teeOut);
			System.setErr(teeErr);
			log.info("Terminal logging initialized: {}", terminalLogFile.getAbsolutePath());
		} catch (IOException ioEx) {
			log.warn("Failed to initialize terminal log capture: {}", ioEx.getMessage());
		}
	}

	@Override
	public void onExecutionFinish() {
		try {
			if (originalOut != null) System.setOut(originalOut);
			if (originalErr != null) System.setErr(originalErr);
			if (logFileOut != null) {
				logFileOut.flush();
				logFileOut.close();
			}
			log.info("Terminal logging finalized");
			lastLogTimes.remove();
		} catch (IOException ioEx) {
			log.warn("Failed to finalize terminal log capture: {}", ioEx.getMessage());
		}
	}

	@Override
	public void beforeFindElement(WebDriver driver, By locator) {
		// Suppress pre-find logs to avoid duplicates; we only log once after element is found
	}

	@Override
	public void afterFindElement(WebDriver driver, By locator, WebElement result) {
		String key = locator == null ? "null" : locator.toString();
		if (shouldLogLocator(key)) {
			String msg = "Found element: " + key;
			log.info("{}", msg);
			try { Allure.step(msg); } catch (Throwable ignored) { }
		}
	}

	public static WebDriver decorate(WebDriver originalDriver) {
		EventFiringDecorator<WebDriver> decorator = new EventFiringDecorator<>(new LogConfig());
		return decorator.decorate(originalDriver);
	}

	static class TeeOutputStream extends java.io.OutputStream {
		private final java.io.OutputStream first;
		private final java.io.OutputStream second;

		TeeOutputStream(java.io.OutputStream first, java.io.OutputStream second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public void write(int b) throws IOException {
			first.write(b);
			second.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			first.write(b);
			second.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			first.write(b, off, len);
			second.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			first.flush();
			second.flush();
		}

		@Override
		public void close() throws IOException {
			try { first.close(); } finally { second.close(); }
		}
	}
}


