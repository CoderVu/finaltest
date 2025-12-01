package org.example.core.retry;

import lombok.Builder;
import lombok.Data;

/**
 * Result of retry decision.
 * Framework agnostic - can be used with any testing framework.
 */
@Data
@Builder
public class RetryResult {
    /**
     * Whether the test should be retried
     */
    private boolean shouldRetry;

    /**
     * Current attempt number (1, 2, 3...)
     */
    private int currentAttempt;

    /**
     * Maximum allowed attempts
     */
    private int maxAttempts;

    /**
     * Is this a retry? (true if attempt > 1)
     */
    private boolean isRetry;

    /**
     * Number of retries so far (0, 1, 2...)
     */
    private int retryNumber;

    /**
     * Remaining attempts
     */
    private int remainingAttempts;

    /**
     * Full test name (for logging/identification)
     */
    private String fullTestName;
}



