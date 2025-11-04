package org.example.core.report;

import lombok.extern.slf4j.Slf4j;
import org.example.common.Constants;
import org.example.core.report.impl.AllureTestReporter;
import org.example.core.report.impl.ExtentTestReporter;
import org.example.core.report.impl.JenkinsTestReporter;
import org.example.core.report.strategy.AllureStrategy;
import org.example.core.report.strategy.ExtentStrategy;
import org.example.core.report.strategy.JenkinsStrategy;
import org.example.core.report.strategy.ReportStrategy;
import org.example.enums.ReportType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.function.Supplier;

@Slf4j
public final class ReportManager {

    private ReportManager() {
    }

    private static volatile ITestReporter reporterInstance;
    private static volatile ReportStrategy strategyInstance;

    // cache reporters by normalized key of types (e.g. "ALLURE,EXTENT")
    private static final ConcurrentMap<String, ITestReporter> reporterCache = new ConcurrentHashMap<>();

    /**
     * Get reporter instance based on report types loaded from Constants.
     */
    public static ITestReporter getReporter() {
        List<String> configuredTypes = Constants.getReportTypes();
        String joined = String.join(",", configuredTypes);
        return getReporter(joined);
    }

    /**
     * Parse a CSV/semicolon-separated reportTypes string into a deduplicated, ordered List<ReportType>.
     * If reportTypes is null or empty, fallback to Constants.getReportTypes().
     */
    public static List<ReportType> parseReportTypes(String reportTypes) {
        String normalizedInput = reportTypes == null ? "" : reportTypes.trim();

        if (normalizedInput.isEmpty()) {
            // fallback: read from Constants
            List<String> typesFromConstants = Constants.getReportTypes();
            normalizedInput = String.join(",", typesFromConstants);
        }

        // split on comma or semicolon, trim tokens, map to enum, remove nulls and duplicates while preserving order
        List<ReportType> types = Arrays.stream(normalizedInput.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(ReportType::fromString)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (types.isEmpty()) {
            types = Collections.singletonList(ReportType.ALLURE);
        }

        log.info("Loaded report types from Constants: {}", types);
        return types;
    }

    /**
     * Create or reuse an ITestReporter instance for given report types.
     */
    public static ITestReporter getReporter(String reportTypes) {
        List<ReportType> types = parseReportTypes(reportTypes);

        // create cache key
        String key = types.stream().map(Enum::name).collect(Collectors.joining(","));

        // return cached if present
        ITestReporter cached = reporterCache.get(key);
        if (cached != null) return cached;

        synchronized (ReportManager.class) {
            cached = reporterCache.get(key);
            if (cached != null) return cached;

            List<ITestReporter> reporters = new ArrayList<>();
            for (ReportType type : types) {
                switch (type) {
                    case EXTENT:
                        reporters.add(new ExtentTestReporter());
                        log.debug("Created ExtentTestReporter instance for type list {}", key);
                        break;
                    case JENKINS:
                        reporters.add(new JenkinsTestReporter());
                        log.debug("Created JenkinsTestReporter instance for type list {}", key);
                        break;
                    case ALLURE:
                    default:
                        reporters.add(new AllureTestReporter());
                        log.debug("Created AllureTestReporter instance for type list {}", key);
                        break;
                }
            }

            ITestReporter result;
            if (reporters.size() == 1) {
                result = reporters.get(0);
            } else {
                // Use a deduplicating wrapper to prevent duplicate identical messages across multiple reporters
                result = new DeduplicatingReporter(reporters);
            }

            reporterCache.put(key, result);
            return result;
        }
    }

    /**
     * Wrapper reporter that:
     * - deduplicates identical messages (method + message) per thread within a short window
     * - executes Supplier/Runnable actions only once and forwards result/state to other reporters
     */
    private static class DeduplicatingReporter implements ITestReporter {
        private final List<ITestReporter> delegates;
        private final ThreadLocal<Map<String, Long>> recent = ThreadLocal.withInitial(HashMap::new);
        private final long dedupWindowMs = 5000L; // 5 seconds

        DeduplicatingReporter(List<ITestReporter> delegates) {
            this.delegates = delegates;
        }

        private String normalizeMessage(String message) {
            if (message == null) return "";
            String m = message.trim();
            // remove common prefixes added by reporters or annotation aspects
            m = m.replaceFirst("(?i)^STEP:\\s*", "");
            m = m.replaceFirst("(?i)^INFO:\\s*", "");
            // remove trailing duration like " 0s", " 12ms", " 1s"
            m = m.replaceAll("\\s+\\d+\\s*(ms|s)$", "");
            return m.trim();
        }

        private boolean isDuplicate(String method, String message) {
            String norm = normalizeMessage(message);
            String key = method + ":" + norm;
            long now = System.currentTimeMillis();
            Map<String, Long> map = recent.get();
            Long last = map.get(key);
            if (last != null && (now - last) < dedupWindowMs) {
                return true;
            }
            map.put(key, now);
            return false;
        }

        @Override
        public void logStep(String message) {
            if (isDuplicate("logStep", message)) return;
            for (ITestReporter r : delegates) {
                try { r.logStep(message); } catch (Throwable ignored) {}
            }
        }

        @Override
        public void info(String message) {
            if (isDuplicate("info", message)) return;
            for (ITestReporter r : delegates) {
                try { r.info(message); } catch (Throwable ignored) {}
            }
        }

        @Override
        public void logFail(String message, Throwable error) {
            // do not deduplicate failures - forward to all
            for (ITestReporter r : delegates) {
                try { r.logFail(message, error); } catch (Throwable ignored) {}
            }
        }

        @Override
        public void attachScreenshot(String name) {
            // Forward to all delegates and let each reporter capture/attach its own screenshot.
            for (ITestReporter r : delegates) {
                try { r.attachScreenshot(name); } catch (Throwable ignored) {}
            }
        }

        @Override
        public void childStep(String name, Runnable runnable) {
            // Deduplicate child steps by normalized name
            if (isDuplicate("childStep", name)) {
                // Already executed recently — do not re-run action nor create duplicate nodes
                return;
            }

            // Execute runnable once, then create step nodes in all reporters without re-running action
            Throwable thrown = null;
            try {
                runnable.run();
            } catch (Throwable t) {
                thrown = t;
            }

            for (ITestReporter r : delegates) {
                try {
                    if (thrown == null) {
                        // report a passed child step without re-executing action
                        r.childStep(name, () -> {});
                    } else {
                        // let reporter record failure inside its childStep by throwing when invoked
                        Throwable finalThrown = thrown;
                        r.childStep(name, () -> { throw new RuntimeException(finalThrown); });
                    }
                } catch (Throwable ignored) {}
            }

            if (thrown != null) {
                if (thrown instanceof RuntimeException) throw (RuntimeException) thrown;
                throw new RuntimeException(thrown);
            }
        }

        @Override
        public <T> T childStep(String name, Supplier<T> supplier) {
            // Deduplicate supplier-based childStep by normalized name
            if (isDuplicate("childStepSupplier", name)) {
                // If duplicate, we do not have the original result here;
                // best effort: call suppliers on first occurrence only — subsequent duplicates skip.
                // Return null to caller is unsafe; so we avoid skipping the first call. For duplicates we simply return supplier.get()
                // to ensure caller gets a result (but avoid running reporter child nodes twice).
                return supplier.get();
            }

            // Execute supplier once, cache result for forwarding to delegates
            T result = supplier.get();
            for (ITestReporter r : delegates) {
                try {
                    r.childStep(name, () -> result);
                } catch (Throwable ignored) {}
            }
            return result;
        }
    }

    /**
     * Select appropriate report strategy based on the first configured ReportType.
     */
    public static ReportStrategy selectStrategy() {
        if (strategyInstance == null) {
            synchronized (ReportManager.class) {
                if (strategyInstance == null) {
                    List<ReportType> types = parseReportTypes(null);
                    ReportType primary = types.get(0); // first one is main output

                    switch (primary) {
                        case EXTENT:
                            log.info("Selected Extent strategy (Output: {})", primary.getOutputPath());
                            strategyInstance = new ExtentStrategy();
                            break;
                        case JENKINS:
                            log.info("Selected Jenkins strategy (Output: {})", primary.getOutputPath());
                            strategyInstance = new JenkinsStrategy();
                            break;
                        case ALLURE:
                        default:
                            log.info("Selected Allure strategy (Output: {})", primary.getOutputPath());
                            strategyInstance = new AllureStrategy();
                            break;
                    }
                }
            }
        }
        return strategyInstance;
    }

    /**
     * Reset all cached reporters and strategies.
     */
    public static void reset() {
        synchronized (ReportManager.class) {
            reporterInstance = null;
            strategyInstance = null;
            reporterCache.clear();
        }
    }
}
