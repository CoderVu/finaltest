# Hướng Dẫn Cấu Hình Driver: Local vs Remote

## Tổng Quan

Framework tự động phân biệt **Local** và **Remote** execution dựa trên các System Properties.

## 🔄 Logic Phân Biệt Local vs Remote

Framework kiểm tra trong `DriverManager.initDriverInternal()`:

```java
if (Config.isRemoteEnabled() || Config.isGridEnabled()) {
    initRemoteDriver();  // Chạy REMOTE
} else {
    initLocalDriver();   // Chạy LOCAL (mặc định)
}
```

## 📋 Bảng Tham Số Hệ Thống

| Tham Số | Bắt Buộc? | Giá Trị | Mô Tả |
|---------|-----------|---------|-------|
| **`remote.enabled`** | ❌ (cho Remote) | `true` / `false` | Enable remote execution |
| **`grid.enabled`** | ❌ (cho Remote) | `true` / `false` | Enable Grid execution (tương tự remote.enabled) |
| **`remote.url`** | ✅ (nếu Remote) | URL string | URL của Selenium Grid Hub (VD: `http://localhost:4444/wd/hub`) |
| **`browser.version`** | ❌ | Version string | Chỉ định browser version cho Remote (VD: `120.0.6099.109`) |
| **`platform.name`** | ❌ | `linux` / `windows` / `mac` | Override platform cho Remote (mặc định: `linux`) |
| **`headless`** | ❌ | `true` / `false` | Chạy headless mode (dùng cho cả Local và Remote) |

> **Lưu ý**: Chỉ cần 1 trong 2: `remote.enabled=true` HOẶC `grid.enabled=true` là đủ để kích hoạt Remote mode.

---

## 🏠 Chạy LOCAL (Mặc Định)

### Cách 1: Không set gì cả
```bash
# Không cần truyền tham số nào, framework sẽ mặc định chạy LOCAL
mvn test
```

### Cách 2: Set explicit = false
```bash
mvn test -Dremote.enabled=false -Dgrid.enabled=false
```

### Ví dụ Local với Maven:
```bash
# Chạy local Chrome
mvn test -Dbrowser=chrome

# Chạy local Firefox headless
mvn test -Dbrowser=firefox -Dheadless=true
```

---

## 🌐 Chạy REMOTE (Selenium Grid)

### Yêu Cầu Bắt Buộc:
1. **Phải có ít nhất 1 flag enabled**: `remote.enabled=true` HOẶC `grid.enabled=true`
2. **Phải có `remote.url`**: URL của Grid Hub

### Ví dụ 1: Remote cơ bản
```bash
mvn test \
  -Dremote.enabled=true \
  -Dremote.url=http://localhost:4444/wd/hub \
  -Dbrowser=chrome
```

### Ví dụ 2: Remote với Grid flag
```bash
mvn test \
  -Dgrid.enabled=true \
  -Dremote.url=http://192.168.1.100:4444/wd/hub \
  -Dbrowser=firefox
```

### Ví dụ 3: Remote với Browser Version
```bash
mvn test \
  -Dremote.enabled=true \
  -Dremote.url=http://localhost:4444/wd/hub \
  -Dbrowser=chrome \
  -Dbrowser.version=120.0.6099.109
```

### Ví dụ 4: Remote với Platform Override
```bash
mvn test \
  -Dgrid.enabled=true \
  -Dremote.url=http://localhost:4444/wd/hub \
  -Dbrowser=edge \
  -Dplatform.name=windows
```

### Ví dụ 5: Remote Headless
```bash
mvn test \
  -Dremote.enabled=true \
  -Dremote.url=http://localhost:4444/wd/hub \
  -Dbrowser=chrome \
  -Dheadless=true
```

---

## 🔍 Chi Tiết Logic Trong Code

### 1. Kiểm Tra Remote Flag (`Config.java`)
```java
public static boolean isRemoteEnabled() {
    String enabled = System.getProperty(Constants.REMOTE_ENABLED_PROPERTY, "false");
    return Boolean.parseBoolean(enabled);  // Mặc định = false
}

public static boolean isGridEnabled() {
    String enabled = System.getProperty(Constants.GRID_ENABLED_PROPERTY, "false");
    return Boolean.parseBoolean(enabled);  // Mặc định = false
}
```

### 2. Lấy Remote URL (`Config.java`)
```java
public static String getRemoteUrl() {
    return System.getProperty(Constants.REMOTE_URL_PROPERTY, "");  // Mặc định = ""
}
```

### 3. Validation khi Remote (`DriverManager.java`)
```java
protected void initRemoteDriver() {
    String remoteUrl = Config.getRemoteUrl();
    if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
        // ❌ SẼ NÉM EXCEPTION nếu không có remote.url
        throw new IllegalStateException(
            "Remote URL is not configured. Please set remote.url property."
        );
    }
    // ... tạo RemoteWebDriver
}
```

### 4. Resolve Browser Version (cho Remote)
Framework tự động resolve browser version từ:
1. **TestNG parameter**: `browserVersion` trong testng.xml
2. **System Property**: `browser.version`
3. **Mặc định**: `null` (Grid sẽ chọn version available)

### 5. Resolve Platform Name (cho Remote)
Framework tự động resolve platform từ:
1. **System Property**: `platform.name` (nếu có)
2. **Mặc định cho Remote**: `LINUX` (vì Grid thường chạy trên Docker/Linux containers)
3. **Mặc định cho Local**: Auto-detect từ OS của máy chạy test

---

## ⚠️ Lưu Ý Quan Trọng

### ✅ Khi nào dùng `remote.enabled` vs `grid.enabled`?
- **Giống nhau**: Cả 2 đều enable remote execution
- **Khuyến nghị**: Dùng 1 trong 2, không cần dùng cả 2
- **Thông thường**: Dùng `grid.enabled=true` khi chạy với Selenium Grid

### ✅ Remote URL Format
```
http://<host>:<port>/wd/hub
```
Ví dụ:
- `http://localhost:4444/wd/hub`
- `http://192.168.1.100:4444/wd/hub`
- `http://selenium-grid.example.com:4444/wd/hub`

### ✅ Thứ Tự Ưu Tiên
1. **Remote/Local decision**: `remote.enabled` HOẶC `grid.enabled` → quyết định mode
2. **Browser version**: TestNG parameter → System property → null
3. **Platform**: System property → Default (LINUX cho Remote, auto-detect cho Local)

---

## 🧪 Test Cases

### Test Case 1: Local Execution
```bash
mvn test -Dbrowser=chrome
# ✅ Không set remote flags → Chạy LOCAL
```

### Test Case 2: Remote với remote.enabled
```bash
mvn test \
  -Dremote.enabled=true \
  -Dremote.url=http://localhost:4444/wd/hub \
  -Dbrowser=chrome
# ✅ Chạy REMOTE
```

### Test Case 3: Remote với grid.enabled
```bash
mvn test \
  -Dgrid.enabled=true \
  -Dremote.url=http://localhost:4444/wd/hub \
  -Dbrowser=chrome
# ✅ Chạy REMOTE
```

### Test Case 4: Remote thiếu URL → ERROR
```bash
mvn test -Dremote.enabled=true -Dbrowser=chrome
# ❌ Sẽ throw exception: "Remote URL is not configured"
```

---

## 📝 Tóm Tắt

| Scenario | `remote.enabled` | `grid.enabled` | `remote.url` | Kết Quả |
|----------|-----------------|---------------|--------------|---------|
| Local | `false` hoặc không set | `false` hoặc không set | - | ✅ LOCAL |
| Remote | `true` | - | `http://...:4444/wd/hub` | ✅ REMOTE |
| Remote | - | `true` | `http://...:4444/wd/hub` | ✅ REMOTE |
| Remote (ERROR) | `true` | - | Không set | ❌ EXCEPTION |

---

## 🔗 Liên Kết Code

- **Quyết định Local/Remote**: `DriverManager.initDriverInternal()` (line 51-56)
- **Check Remote Enabled**: `Config.isRemoteEnabled()` (line 148-150)
- **Check Grid Enabled**: `Config.isGridEnabled()` (line 171-173)
- **Get Remote URL**: `Config.getRemoteUrl()` (line 156-158)
- **Init Remote Driver**: `DriverManager.initRemoteDriver()` (line 67-82)

