package org.example.core.report;

import lombok.extern.slf4j.Slf4j;
import java.io.*;

/**
 * Simple console logger that redirects System.out/System.err to a single suite-level file.
 * Start it once in @BeforeSuite and stop it in @AfterSuite to produce one shared log for all browsers.
 */
@Slf4j
public class ConsoleConfig {

	// suite-wide streams
	private static volatile boolean suiteLogStarted = false;
	private static PrintStream originalOut;
	private static PrintStream originalErr;
	private static PrintStream teeOut;
	private static FileOutputStream fileOutputStream;

	/**
	 * Start suite-level console capture. If already started, this is a no-op.
	 */
	public void startTerminalLog() {
		synchronized (ConsoleConfig.class) {
			if (suiteLogStarted) {
				log.debug("ConsoleConfig: suite log already started");
				return;
			}
			try {
				originalOut = System.out;
				originalErr = System.err;

				File file = new File("target/console-suite.log");
				file.getParentFile().mkdirs();
				fileOutputStream = new FileOutputStream(file, true);

				// tee -> write to original console AND file
				TeeOutputStream tos = new TeeOutputStream(originalOut, fileOutputStream);
				teeOut = new PrintStream(tos, true);

				System.setOut(teeOut);
				System.setErr(teeOut);

				suiteLogStarted = true;
				log.info("ConsoleConfig - Suite console log started: {}", file.getAbsolutePath());
			} catch (Exception e) {
				log.warn("ConsoleConfig - Failed to start suite console log: {}", e.getMessage());
				// ensure partial resources cleaned
				try { if (fileOutputStream != null) fileOutputStream.close(); } catch (Exception ignored) {}
			}
		}
	}

	/**
	 * Stop suite-level console capture and restore original System.out/System.err.
	 */
	public void stopTerminalLog() {
		synchronized (ConsoleConfig.class) {
			if (!suiteLogStarted) {
				return;
			}
			try {
				if (originalOut != null) {
					System.setOut(originalOut);
				}
				if (originalErr != null) {
					System.setErr(originalErr);
				}
				// closing teeOut will close the fileOutputStream via TeeOutputStream.close()
				if (teeOut != null) {
					teeOut.close();
				}
				log.info("ConsoleConfig - Suite console log stopped");
			} catch (Exception e) {
				log.warn("ConsoleConfig - Failed to stop suite console log: {}", e.getMessage());
			} finally {
				suiteLogStarted = false;
				originalOut = null;
				originalErr = null;
				teeOut = null;
				fileOutputStream = null;
			}
		}
	}

	/**
	 * OutputStream that writes to two outputs. On close it closes only the second (file) stream
	 * to avoid closing the original System.out/System.err.
	 */
	private static class TeeOutputStream extends OutputStream {
		private final OutputStream out1; // original console (do NOT close)
		private final OutputStream out2; // file (close on close)

		public TeeOutputStream(OutputStream out1, OutputStream out2) {
			this.out1 = out1;
			this.out2 = out2;
		}

		@Override
		public void write(int b) throws IOException {
			try { out1.write(b); } catch (IOException ignored) {}
			out2.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			try { out1.write(b, off, len); } catch (IOException ignored) {}
			out2.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			try { out1.flush(); } catch (IOException ignored) {}
			out2.flush();
		}

		@Override
		public void close() throws IOException {
			// do not close out1 (original console), only close file output
			try { out2.close(); } catch (IOException ignored) {}
		}
	}
}
