# finaltest — Quick Start (clean & concise)

Selenium + TestNG automation framework with multi-browser support and Extent reporting.

Tech: Java 17+, Maven, Selenium WebDriver, TestNG, ExtentReports.

Prerequisites
- Java 17+ and Maven 3.8+
- Installed browsers: Chrome, Firefox, Edge
- (Optional) Docker for Selenium Grid

Quick examples
- Run default tests:
  mvn clean test

- Run one browser:
  mvn clean test -Dbrowser=chrome
  mvn clean test -Dbrowser=firefox

- Enable WebDriverManager (download drivers automatically):
  mvn clean test -Dbrowser=chrome -Duse.wdm=true

- Headless:
  mvn clean test -Dbrowser=chrome -Dheadless=true

Remote / Grid
- Run on remote Grid:
  mvn clean test -Dremote.enabled=true -Dremote.url=http://GRID_HOST:4444/wd/hub -Dbrowser=firefox

- Specify remote browser/platform:
  mvn clean test -Dremote.enabled=true -Dremote.url=http://GRID_HOST:4444/wd/hub -Dbrowser=chrome -Dplatform.name=LINUX

IntelliJ VM options example
-Dbrowser=chrome -Duse.wdm=true -Dheadless=false

Common system properties
- -Dbrowser=chrome|firefox|edge
- -Duse.wdm=true|false
- -Dheadless=true|false
- -Dremote.enabled=true|false
- -Dremote.url=http://host:4444/wd/hub
- -Dplatform.name=LINUX|WINDOWS|MAC
- -DreportType=extent (default)

Reports

## ExtentReports (default)

- Each run creates `target/extent-report/<timestamp>/index_<timestamp>.html`.
- Screenshots live in `target/extent-report/<timestamp>/screenshots/`.
- Open the HTML file directly in your browser to view results.

Troubleshooting (fast)
- WebDriverManager network/parse errors → run without it: -Duse.wdm=false (Selenium Manager will attempt driver).
- CDP warnings for Chrome → add matching selenium-devtools artifact if you need DevTools.
- Driver/browser mismatch → update browser, enable -Duse.wdm=true, or supply -Dbrowser.version.

Notes
- Check `org.example.configure.Config` for exact property names and defaults.
- Look at build logs for generated artifacts (testng.xml, extent report path).
