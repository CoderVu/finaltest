@echo off
echo ========================================
echo    SELENIUM TEST RUNNER MENU
echo ========================================
echo.
echo Configuration:
echo   - URL: https://tiki.vn (all environments)
echo   - Browsers: Chrome, Firefox, Edge (parallel by default)
echo   - Tests: TC002_VerifyUserCanSeeMoreItems
echo.
echo Choose execution mode:
echo.
echo 1. Local Testing (all browsers parallel)
echo 2. Grid Testing (all browsers parallel)
echo 3. Grid VNC Testing (all browsers with VNC)
echo 4. Single Browser Testing (Chrome only)
echo 5. Single Browser Testing (Firefox only)
echo 6. Single Browser Testing (Edge only)
echo 7. Grid Single Browser Testing (Chrome only)
echo 8. Grid Single Browser Testing (Firefox only)
echo 9. Grid Single Browser Testing (Edge only)
echo 0. Exit
echo.
set /p choice="Enter your choice (0-9): "

if "%choice%"=="1" (
    echo.
    echo Starting Local Testing...
    call run-local.bat
) else if "%choice%"=="2" (
    echo.
    echo Starting Grid Testing...
    call run-grid.bat
) else if "%choice%"=="3" (
    echo.
    echo Starting Grid VNC Testing...
    call run-grid-vnc.bat
) else if "%choice%"=="4" (
    echo.
    echo Starting Chrome Only Testing...
    mvn clean test -Dbrowser=chrome
) else if "%choice%"=="5" (
    echo.
    echo Starting Firefox Only Testing...
    mvn clean test -Dbrowser=firefox
) else if "%choice%"=="6" (
    echo.
    echo Starting Edge Only Testing...
    mvn clean test -Dbrowser=edge
) else if "%choice%"=="7" (
    echo.
    echo Starting Grid Chrome Only Testing...
    mvn clean test -Premote-grid -Dbrowser=chrome
) else if "%choice%"=="8" (
    echo.
    echo Starting Grid Firefox Only Testing...
    mvn clean test -Premote-grid -Dbrowser=firefox
) else if "%choice%"=="9" (
    echo.
    echo Starting Grid Edge Only Testing...
    mvn clean test -Premote-grid -Dbrowser=edge
) else if "%choice%"=="0" (
    echo.
    echo Goodbye!
    exit /b 0
) else (
    echo.
    echo Invalid choice! Please run the script again.
    pause
    exit /b 1
)

echo.
echo ========================================
echo Test execution completed!
echo ========================================
pause
