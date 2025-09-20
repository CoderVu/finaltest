#!/bin/bash

# Unix/Mac/Linux Interactive Test Execution Script
echo "=== Selenium Test Suite Unix/Mac/Linux Launcher ==="
echo ""

# Detect specific platform
OS="$(uname -s)"
case "${OS}" in
    Linux*)     PLATFORM="Linux";;
    Darwin*)    PLATFORM="MacOS";;
    *)          PLATFORM="Unix";;
esac

echo "Platform: $PLATFORM"
echo ""

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or higher and ensure 'java' command is available"
    exit 1
fi

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven and ensure 'mvn' command is available"
    exit 1
fi

echo "Using Java: $(java -version 2>&1 | head -n1)"
echo "Using Maven: $(mvn -version 2>&1 | head -n1)"
echo ""

# Compile and run the Java launcher (it will handle Docker/Grid checks)
echo "Compiling test launcher..."
if mvn compile exec:java -Dexec.mainClass="org.example.launcher.TestLauncher" -Dexec.args="" -q; then
    echo ""
    echo "✓ Test execution completed successfully!"
else
    echo ""
    echo "⚠ Error: Failed to compile or run the test launcher"
    echo "Falling back to direct Maven execution..."
    echo ""
    mvn clean test -Dinteractive.config=true
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✓ Test execution completed successfully!"
    else
        echo ""
        echo "✗ Test execution failed"
        exit 1
    fi
fi

# Platform-specific pause
if [ "$PLATFORM" = "MacOS" ]; then
    read -p "Press Enter to exit..."
fi
    echo "✓ Selenium Grid started successfully!"
else
    echo "✓ Selenium Grid is already running"
fi

echo "✓ Grid UI: http://localhost:4444"
echo ""

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven and ensure 'mvn' command is available"
    exit 1
fi

echo "Using Java: $(java -version 2>&1 | head -n1)"
echo "Using Maven: $(mvn -version 2>&1 | head -n1)"
echo ""

# Compile and run the Java launcher
echo "Compiling test launcher..."
if mvn compile exec:java -Dexec.mainClass="org.example.launcher.TestLauncher" -Dexec.args="" -q; then
    echo ""
    echo "✓ Test execution completed successfully!"
else
    echo ""
    echo "⚠ Error: Failed to compile or run the test launcher"
    echo "Falling back to direct Maven execution..."
    echo ""
    mvn clean test -Dinteractive.config=true
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✓ Test execution completed successfully!"
    else
        echo ""
        echo "✗ Test execution failed"
        exit 1
    fi
fi

# Platform-specific pause
if [ "$PLATFORM" = "MacOS" ]; then
    read -p "Press Enter to exit..."
fi
