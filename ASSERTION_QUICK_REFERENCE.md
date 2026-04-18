# Assertion Framework - Quick Reference

## Overview
Three-tier assertion framework: **Immediate** → **Awaited** → **Element-based** with fluent API.

---

## Tier 1: Immediate Assertions (No Retry)

**Use when**: Value is immediately available (not dynamic/UI)

```java
import static org.example.core.assertion.Assertions.*;

// Basic assertions
assertTrue(condition, "message");
assertFalse(condition, "message");
assertEquals(actual, expected, "message");
assertNotEquals(actual, expected, "message");
```

**Examples:**
```java
Assertions.assertEquals(user.getName(), "John", "Username should match");
Assertions.assertTrue(config.isEnabled(), "Config should be enabled");
```

---

## Tier 2: Await Assertions (With Retry)

**Use when**: Value becomes available over time (dynamic conditions)

```java
import static org.example.core.assertion.AwaitAssert.*;

// General assertions with retry logic
assertTrue(() -> condition, "message");
assertFalse(() -> condition, "message");
assertEquals(() -> supplier.get(), expected, "message");
assertNotEquals(() -> supplier.get(), expected, "message");
assertGreaterThan(() -> value, threshold, "message");
assertLessThan(() -> value, threshold, "message");
assertGreaterThanOrEqual(() -> value, threshold, "message");
assertLessThanOrEqual(() -> value, threshold, "message");
```

**Examples:**
```java
// Retry-friendly: supplier called multiple times
AwaitAssert.assertEquals(
    () -> homePage.getHotelCount(),
    5,
    "Hotel count should eventually be 5"
);

AwaitAssert.assertTrue(
    () -> pageTitle.contains("Success"),
    "Page title should eventually contain 'Success'"
);
```

---

## Tier 3: Element Assertions (Fluent API with Retry)

**Use when**: Asserting on UI elements with retry, negation, and custom timing

```java
import static org.example.core.assertion.AwaitAssert.expect;

// Basic usage
expect(element).toBeVisible();
expect(element).toBeHidden();
expect(element).toBeEnabled();
expect(element).toBeDisabled();
expect(element).toHaveText("expected text");
expect(element).toContainText("substring");
expect(element).toHaveAttribute("attr", "value");
expect(element).toHaveValue("expected value");
```

**Examples:**
```java
// Simple fluent assertions
expect(submitButton).toBeEnabled();
expect(errorMessage).toHaveText("Invalid email");
expect(modal).toBeVisible();

// With custom timeout
expect(element).withTimeout(10000).toBeVisible();
expect(element).withTimeout(Duration.ofSeconds(10)).toBeEnabled();

// With custom polling interval
expect(element).withInterval(100).toBeVisible();

// With negation (immutable, returns new instance)
expect(element).not().toBeDisabled();
expect(element).not().toContainText("Error");
expect(element).not().toBeHidden();

// Chaining (each method returns new immutable instance)
expect(element)
    .withTimeout(5000)
    .withInterval(200)
    .not()
    .toHaveText("Loading...");
```

---

## Key Differences

| Feature | Assertions | AwaitAssert | expect() |
|---------|-----------|-----------|----------|
| Retry | ❌ | ✅ | ✅ |
| Fails Immediately | ✅ | ❌ | ❌ |
| Fluent API | ❌ | ❌ | ✅ |
| Negation Support | ❌ | ❌ | ✅ |
| Custom Timeout | ❌ | ❌ | ✅ |
| Custom Interval | ❌ | ❌ | ✅ |
| Element-Focused | ❌ | ❌ | ✅ |
| Thread-Safe | ✅ | ✅ | ✅ |

---

## Failure Messages

### Immediate Assertion Failure
```
java.lang.AssertionError: Username should match | expected=John actual=Jane
```

### Await Assertion Failure
```
java.lang.AssertionError: Hotel count should eventually be 5 | lastActual=3 | attempts=12, timeout=5000ms, elapsed=4998ms
```

### Element Assertion Failure
```
java.lang.AssertionError: Element should be visible: By.xpath(//button[@id='submit']) 
| attempts=25, timeout=5000ms, elapsed=4999ms
```

---

## Common Patterns

### Wait for Element to Appear
```java
expect(loadingSpinner).withTimeout(5000).toBeVisible();
```

### Wait for Element to Disappear
```java
expect(loadingSpinner).withTimeout(5000).toBeHidden();
```

### Wait for Text to Change
```java
AwaitAssert.assertEquals(
    () -> statusLabel.getText(),
    "Ready",
    "Status should change to Ready"
);
```

### Negative Assertion (Element Should NOT have class)
```java
expect(button).not().toHaveAttribute("disabled", "true");
```

### Retry with Custom Timing
```java
expect(element)
    .withTimeout(10000)      // Wait up to 10 seconds
    .withInterval(500)       // Poll every 500ms
    .toContainText("Success");
```

### Multiple Conditions
```java
// Each assertion independently retries
expect(button).toBeEnabled();
expect(button).toHaveText("Click Me");
expect(button).not().toHaveAttribute("disabled", "");
```

---

## Best Practices

✅ **DO:**
- Use `Assertions.*` for static/immediate values
- Use `AwaitAssert.assertEquals()` for supplier-based conditions
- Use `expect()` for element-based assertions
- Chain configuration in fluent API: `withTimeout().withInterval().not().toBeVisible()`
- Keep timeout reasonable (3-10 seconds typical)
- Use custom interval for fast/slow operations

❌ **DON'T:**
- Use `expect()` for non-UI assertions (use `AwaitAssert.*` instead)
- Overthink which tier to use - start with immediate, upgrade to await if needed
- Chain multiple assertions expecting fail-fast (use separate lines)
- Set extremely long timeouts (>30 seconds)
- Retry when values are truly static (use `Assertions.*`)

---

## Migration from Old API

### Old `Expect` Class (Deleted)
```java
// OLD - Now removed
import static org.example.core.assertion.Expect.equalsTo;
equalsTo(() -> supplier.get(), expected, message);

// NEW - Use AwaitAssert instead
import static org.example.core.assertion.AwaitAssert.assertEquals;
assertEquals(() -> supplier.get(), expected, message);
```

### Old `Assertions.get()` Pattern (Still Works, Updated)
```java
// OLD
Assertions.get().assertEquals(actual, expected, msg);

// NEW - Direct static call
Assertions.assertEquals(actual, expected, msg);
```

---

## Troubleshooting

**Q: "Assertion passed after X attempts" - Is this expected?**  
A: Yes! If it eventually passes, that's success. The message indicates how many retries were needed.

**Q: Can I use `expect()` for non-UI values?**  
A: Technically yes, but use `AwaitAssert.assertEquals()` instead for better clarity.

**Q: Does `not()` mutate the expectation?**  
A: No! It returns a new immutable instance. Safe to chain and reuse.

**Q: How do I debug flaky tests?**  
A: Enable TRACE logging to see each retry attempt, then adjust timeout/interval.

**Q: Thread safety - Can I use across threads?**  
A: Yes! All classes are stateless and thread-safe. No ThreadLocal magic needed.

---

## API Evolution Path

```
Assertions.assertTrue()          # Static, immediate
     ↓
AwaitAssert.assertTrue()         # Supplier-based, retries
     ↓
expect(element)                  # Fluent, element-focused, full control
  .withTimeout()
  .withInterval()
  .not()
  .toBeVisible()
```

Pick the right tier for your assertion needs!
