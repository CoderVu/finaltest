@echo off
echo ========================================
echo    LOCAL TESTING (Chrome/Firefox/Edge)
echo ========================================
echo.
echo Configuration:
echo   - URL: https://tiki.vn (all environments)
echo   - Browsers: Chrome, Firefox, Edge (parallel)
echo   - Mode: Local execution
echo.
echo Starting local tests with all browsers...
echo.

mvn clean test

echo.
echo ========================================
echo Local testing completed!
echo ========================================
echo.
echo Reports available at:
echo   - Allure: target/allure-results/
echo   - Selenide: target/selenide-reports/
echo.
pause
