@echo off
echo ========================================
echo    GRID VNC TESTING (Chrome/Firefox/Edge)
echo ========================================
echo.
echo Configuration:
echo   - URL: https://tiki.vn (all environments)
echo   - Browsers: Chrome, Firefox, Edge (parallel)
echo   - Mode: Selenium Grid with VNC support
echo.
echo Checking if Selenium Grid is running...
curl -s http://localhost:4444/wd/hub/status > nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Selenium Grid is not running!
    echo Please start Grid first: .\selenium-grid.bat start
    echo.
    pause
    exit /b 1
)

echo Grid is running. Starting remote tests with VNC...
echo.
echo Grid Console: http://localhost:4444/ui
echo VNC Viewers (for debugging):
echo   Chrome:  http://localhost:7900
echo   Firefox: http://localhost:7901  
echo   Edge:    http://localhost:7902
echo.
echo Note: VNC allows you to watch tests running in real-time
echo.

mvn clean test -Pgrid-vnc

echo.
echo ========================================
echo Remote Grid VNC testing completed!
echo ========================================
echo.
echo Reports available at:
echo   - Allure: target/allure-results/
echo   - Selenide: target/selenide-reports/
echo.
pause
