# Đề xuất Refactoring Element Package theo Design Pattern

## 📋 Phân tích hiện trạng

**Vấn đề:**
- `Element.java` quá lớn (~1082 lines) - vi phạm Single Responsibility Principle
- Tất cả logic (actions, waits, scrolls, gets) nằm trong 1 class
- Khó maintain và test
- Không dễ mở rộng

## 🎯 Đề xuất cấu trúc mới

### Option 1: Strategy Pattern + Facade (Khuyến nghị)

```
core/element/
├── IElement.java                          # Interface chính
├── BaseElement.java                       # Abstract base class
├── WebElementWrapper.java                 # Implementation chính
│
├── factory/
│   └── ElementFactory.java                # Factory Pattern (giữ nguyên)
│
├── locator/
│   ├── LocatorResolver.java               # Resolve locator string -> By
│   └── LocatorStrategy.java              # Strategy cho các loại locator
│
├── action/
│   ├── IElementAction.java               # Interface cho actions
│   ├── ClickAction.java                  # Click strategy
│   ├── TypeAction.java                   # Type/SetText strategy
│   ├── SelectAction.java                 # Select dropdown strategy
│   └── DragDropAction.java               # Drag & drop strategy
│
├── wait/
│   ├── IWaitStrategy.java                # Interface cho wait strategies
│   ├── VisibilityWaitStrategy.java       # Wait for visibility
│   ├── ClickableWaitStrategy.java        # Wait for clickable
│   └── InvisibilityWaitStrategy.java    # Wait for invisibility
│
├── scroll/
│   ├── IScrollStrategy.java              # Interface cho scroll
│   └── ScrollStrategy.java               # Scroll implementations
│
├── decorator/
│   ├── ElementDecorator.java             # Base decorator
│   ├── WaitableElement.java              # Decorator thêm wait capability
│   └── ScrollableElement.java           # Decorator thêm scroll capability
│
└── util/
    └── DriverUtils.java                   # Utility (giữ nguyên)
```

### Option 2: Component-based (Đơn giản hơn)

```
core/element/
├── IElement.java                         # Interface
├── BaseElement.java                       # Base implementation
├── WebElementWrapper.java                 # Main wrapper
│
├── factory/
│   └── ElementFactory.java
│
├── component/
│   ├── ElementActions.java               # Tất cả actions (click, type, etc.)
│   ├── ElementWaits.java                 # Tất cả wait methods
│   ├── ElementScrolls.java               # Tất cả scroll methods
│   ├── ElementGetters.java               # Tất cả get methods
│   └── ElementChecks.java                # Tất cả is* methods
│
├── locator/
│   └── LocatorResolver.java
│
└── util/
    └── DriverUtils.java
```

## 🏗️ Chi tiết Implementation

### 1. Interface & Base Classes

```java
// IElement.java
public interface IElement {
    void click();
    void setText(String text);
    String getText();
    boolean isVisible();
    // ... core methods
}

// BaseElement.java
public abstract class BaseElement implements IElement {
    protected By locator;
    protected Element parent;
    
    protected abstract WebElement findElement();
    // ... common logic
}

// WebElementWrapper.java
public class WebElementWrapper extends BaseElement {
    private final ElementActions actions;
    private final ElementWaits waits;
    private final ElementScrolls scrolls;
    
    // Delegate to components
    @Override
    public void click() {
        actions.click(this);
    }
}
```

### 2. Component Classes

```java
// ElementActions.java
public class ElementActions {
    public void click(IElement element) { ... }
    public void setText(IElement element, String text) { ... }
    public void select(IElement element, String value) { ... }
}

// ElementWaits.java
public class ElementWaits {
    public void waitForVisibility(IElement element, Duration timeout) { ... }
    public void waitForClickable(IElement element, Duration timeout) { ... }
}
```

### 3. Locator Resolver

```java
// LocatorResolver.java
public class LocatorResolver {
    public static By resolve(String locator) {
        // Parse "xpath=//div" -> By.xpath("//div")
    }
}
```

## 📝 Naming Conventions

### Classes
- **Interface**: `IElement`, `IElementAction`, `IWaitStrategy`
- **Base/Abstract**: `BaseElement`, `ElementDecorator`
- **Implementation**: `WebElementWrapper`, `ClickAction`
- **Factory**: `ElementFactory`
- **Strategy**: `*Strategy` suffix
- **Component**: `Element*` prefix (ElementActions, ElementWaits)

### Packages
- `action/` - Các action strategies
- `wait/` - Các wait strategies  
- `scroll/` - Scroll functionality
- `locator/` - Locator resolution
- `decorator/` - Decorator pattern
- `factory/` - Factory pattern
- `util/` - Utilities

## ✅ Lợi ích

1. **Single Responsibility**: Mỗi class chỉ làm 1 việc
2. **Open/Closed**: Dễ thêm action/wait mới mà không sửa code cũ
3. **Testability**: Dễ test từng component riêng
4. **Maintainability**: Code ngắn gọn, dễ đọc
5. **Reusability**: Components có thể reuse

## 🚀 Migration Plan

1. **Phase 1**: Tạo interface và base classes
2. **Phase 2**: Tách actions ra component
3. **Phase 3**: Tách waits ra component
4. **Phase 4**: Tách scrolls và gets
5. **Phase 5**: Refactor ElementFactory nếu cần
6. **Phase 6**: Update tất cả usages

## 💡 Recommendation

**Chọn Option 2 (Component-based)** vì:
- Đơn giản hơn, dễ implement
- Phù hợp với codebase hiện tại
- Vẫn đạt được mục tiêu tách biệt concerns
- Dễ migrate từ code hiện tại

