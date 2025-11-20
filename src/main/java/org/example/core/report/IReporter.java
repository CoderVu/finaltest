package org.example.core.report;

import org.example.enums.ReportType;

/**
 * Interface định nghĩa các phương thức báo cáo test.
 * Tương tự IDriverManager trong package driver.
 */
public interface IReporter {
    /**
     * Ghi log một bước thực thi.
     * @param message nội dung bước
     */
    void logStep(String message);
    
    /**
     * Ghi log thông tin.
     * @param message nội dung thông tin
     */
    void info(String message);
    
    /**
     * Ghi log lỗi/thất bại.
     * @param message nội dung lỗi
     * @param error exception nếu có
     */
    void logFail(String message, Throwable error);
    
    /**
     * Đính kèm screenshot.
     * @param name tên file screenshot
     */
    void attachScreenshot(String name);
    
    /**
     * Tạo bước con với Runnable.
     * @param name tên bước
     * @param runnable action cần thực thi
     */
    void childStep(String name, Runnable runnable);
    
    /**
     * Tạo bước con với Supplier (có return value).
     * @param name tên bước
     * @param supplier action cần thực thi
     * @param <T> kiểu dữ liệu trả về
     * @return kết quả từ supplier
     */
    <T> T childStep(String name, java.util.function.Supplier<T> supplier);
    
    /**
     * Lấy loại report.
     * @return ReportType
     */
    ReportType getReportType();

    /**
     * Kiểm tra xem có đang trong một step không.
     * @return true nếu đang trong step, false nếu không
     */
    default boolean isInStep() {
        return false;
    }
}
