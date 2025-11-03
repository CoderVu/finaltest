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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
public final class ReportManager {

    private ReportManager() {}

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
                // create dynamic proxy implementing ITestReporter that delegates calls to all reporters
                result = (ITestReporter) Proxy.newProxyInstance(
                        ITestReporter.class.getClassLoader(),
                        new Class[]{ITestReporter.class},
                        (proxy, method, args) -> {
                            Object firstNonNull = null;
                            for (ITestReporter r : reporters) {
                                try {
                                    Object res = method.invoke(r, args);
                                    if (firstNonNull == null && res != null) {
                                        firstNonNull = res;
                                    }
                                } catch (InvocationTargetException ite) {
                                    throw ite.getTargetException();
                                }
                            }
                            return firstNonNull;
                        }
                );
            }

            reporterCache.put(key, result);
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
