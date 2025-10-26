package org.example.report;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@Slf4j
public class DebugConfig {
	
    private PrintStream originalOut;
    private PrintStream originalErr;
    private FileOutputStream logFileOut;
    private File terminalLogFile;

    public void startTerminalLog() {
        try {
            terminalLogFile = new File("target/allure-results/terminal.log");
            if (terminalLogFile.exists() && !terminalLogFile.delete()) {
                log.warn("Could not delete existing terminal log file: " + terminalLogFile.getAbsolutePath());
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
            log.info("Log config has been created");
        } catch (IOException ioEx) {
            log.error("Failed to initialize terminal log capture: " + ioEx.getMessage());
        }
    }

    public void stopTerminalLog() {
        try {
            if (originalOut != null) System.setOut(originalOut);
            if (originalErr != null) System.setErr(originalErr);
            if (logFileOut != null) {
                logFileOut.flush();
                logFileOut.close();
            }
            log.info("Terminal log capture finalized. Log file: " + terminalLogFile.getAbsolutePath());
        } catch (IOException ioEx) {
            log.error("Failed to finalize terminal log capture: " + ioEx.getMessage());
        }
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
