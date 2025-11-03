package org.example.core.report;

/**
 * Small utility to mark that a soft-assert failure was already handled on the current thread.
 * This helps reporting listeners avoid duplicate screenshots/logs during test teardown.
 */
public final class FailureTracker {
	// thread-local flag; using Boolean to allow remove() to clear value
	private static final ThreadLocal<Boolean> SOFT_HANDLED = ThreadLocal.withInitial(() -> Boolean.FALSE);

	private FailureTracker() {}

	public static void markHandledForCurrentThread() {
		SOFT_HANDLED.set(Boolean.TRUE);
	}

	public static boolean isHandledForCurrentThread() {
		Boolean v = SOFT_HANDLED.get();
		return v != null && v;
	}

	public static void clearForCurrentThread() {
		SOFT_HANDLED.remove();
	}
}
