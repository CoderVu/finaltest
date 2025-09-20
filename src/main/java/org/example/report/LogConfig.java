package org.example.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class LogConfig {
	
    private PrintStream originalOut;
    private PrintStream originalErr;
    private FileOutputStream logFileOut;
    private File terminalLogFile;

    public void startTerminalLog() {
        try {
            terminalLogFile = new File("target/allure-results/terminal.log");
            if (terminalLogFile.exists() && !terminalLogFile.delete()) {
                System.out.println("Could not delete existing terminal.log; appending to it");
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
            System.out.println("Terminal logging initialized: " + terminalLogFile.getAbsolutePath());
        } catch (IOException ioEx) {
            System.out.println("Failed to initialize terminal log capture: " + ioEx.getMessage());
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
            System.out.println("Terminal logging finalized");
        } catch (IOException ioEx) {
            System.out.println("Failed to finalize terminal log capture: " + ioEx.getMessage());
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
