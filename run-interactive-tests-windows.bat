@echo off
cls

echo === Selenium Test Suite Windows Launcher ===
echo.

REM Check if Java is available
java -version >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 11 or higher and ensure 'java' command is available
    pause
    exit /b 1
)

REM Check if Maven is available
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set MAVEN_CMD=mvn
    goto :run_launcher
)

where mvn.cmd >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set MAVEN_CMD=mvn.cmd
    goto :run_launcher
)

echo Error: Maven is not installed or not in PATH
echo Please install Maven and ensure 'mvn' or 'mvn.cmd' command is available
pause
exit /b 1

:run_launcher
echo Using Maven command: %MAVEN_CMD%
echo Platform: Windows
echo.

REM Compile and run the Java launcher (it will handle Docker/Grid checks)
echo Compiling test launcher...
%MAVEN_CMD% compile exec:java -Dexec.mainClass="org.example.launcher.TestLauncher" -Dexec.args="" -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Error: Failed to compile or run the test launcher
    echo Falling back to direct Maven execution...
    echo.
    %MAVEN_CMD% clean test -Dinteractive.config=true
)

echo.
echo Press any key to exit...
pause >nul
    REM Verify Grid is now running
    curl -s http://localhost:4444/status >nul 2>nul
    if %ERRORLEVEL% NEQ 0 (
        echo Error: Failed to start Selenium Grid
        echo Please check Docker services manually: docker-compose logs
        pause
        exit /b 1
    )
    echo ✓ Selenium Grid started successfully!
) else (
    echo ✓ Selenium Grid is already running
)

echo ✓ Grid UI: http://localhost:4444
echo.

REM Check if Maven is available
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set MAVEN_CMD=mvn
    goto :run_launcher
)

where mvn.cmd >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set MAVEN_CMD=mvn.cmd
    goto :run_launcher
)

echo Error: Maven is not installed or not in PATH
echo Please install Maven and ensure 'mvn' or 'mvn.cmd' command is available
pause
exit /b 1

:run_launcher
echo Using Maven command: %MAVEN_CMD%
echo Platform: Windows
echo.

REM Compile and run the Java launcher
echo Compiling test launcher...
%MAVEN_CMD% compile exec:java -Dexec.mainClass="org.example.launcher.TestLauncher" -Dexec.args="" -q

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Error: Failed to compile or run the test launcher
    echo Falling back to direct Maven execution...
    echo.
    %MAVEN_CMD% clean test -Dinteractive.config=true
)

echo.
echo Press any key to exit...
pause >nul
