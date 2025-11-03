package org.example.core.report;


public final class FailureTracker {
	
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
