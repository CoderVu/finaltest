# finaltest — Quick Start (clean & concise)

Selenium + TestNG automation framework with multi-browser support and multiple reporting options.

Tech: Java 17+, Maven, Selenium WebDriver, TestNG, Allure, ExtentReports.

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

- Specify remote browser/version/platform:
  mvn clean test -Dremote.enabled=true -Dremote.url=http://GRID_HOST:4444/wd/hub -Dbrowser=chrome -Dbrowser.version=141.0 -Dplatform.name=LINUX

IntelliJ VM options example
-Dbrowser=chrome -Duse.wdm=true -Dheadless=false

Common system properties
- -Dbrowser=chrome|firefox|edge
- -Duse.wdm=true|false
- -Dheadless=true|false
- -Dremote.enabled=true|false
- -Dremote.url=http://host:4444/wd/hub
- -Dbrowser.version=xxx
- -Dplatform.name=LINUX|WINDOWS|MAC
- -Dallure.report.folder={timestamp}|latest|custom-name (default: latest)

Reports

## Allure Reports (Default)

Allure reports are saved with timestamps to preserve historical runs. Each test execution can create a unique folder by passing a timestamp parameter.

### Run Tests with Timestamp (Recommended)

**PowerShell (Windows):**
```powershell
# Create timestamp and run tests with report generation
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
mvn clean test allure:report "-Dallure.report.folder=$timestamp"
```

**Bash (Linux/macOS):**
```bash
# Create timestamp and run tests with report generation
export TIMESTAMP=$(date +%Y%m%d_%H%M%S)
mvn clean test allure:report -Dallure.report.folder=$TIMESTAMP
```

**One-liner (PowerShell):**
```powershell
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"; mvn clean test allure:report "-Dallure.report.folder=$timestamp"
```

**One-liner (Bash):**
```bash
TIMESTAMP=$(date +%Y%m%d_%H%M%S) mvn clean test allure:report -Dallure.report.folder=$TIMESTAMP
```

**Result structure:**
```
target/allure-reports/
  ├── 20241215_143022/  (run 1)
  ├── 20241215_150530/  (run 2)
  ├── 20241215_160145/  (run 3)
  └── latest/            (default if not specified)
```

### Run Tests with Default Folder (latest)

**Use "latest" folder (overwrites previous):**
```bash
mvn clean test allure:report -Dallure.report.folder=latest
```

Or simply (since "latest" is the default):
```bash
mvn clean test allure:report
```

### Use Custom Folder Name

```bash
mvn clean test allure:report -Dallure.report.folder=my-custom-name
```

### View Reports

**View specific timestamped report:**
```bash
# Generate and serve report for specific timestamp
mvn allure:serve -Dallure.results.directory=target/allure-results/20241215_143022

# Or use Allure CLI directly (Windows CMD/PowerShell)
allure serve target/allure-results/20241215_143022

# Or generate static report
mvn allure:report "-Dallure.results.directory=target/allure-results/20241215_143022" "-Dallure.report.directory=target/allure-reports/20241215_143022"
# Then open: target/allure-reports/20241215_143022/index.html
```

**View latest report:**
```bash
# If you used "latest" folder
mvn allure:serve -Dallure.results.directory=target/allure-results/latest
```

### Report Locations

- **Results (raw data):** `target/allure-results/{timestamp}/`
- **Reports (generated HTML):** `target/allure-reports/{timestamp}/`
- **History:** Automatically merged from `src/test/resources/allure/history/` (if exists)

## ExtentReports

```bash
mvn clean test -DreportType=extent
```

Output: `target/extent/index.html`

Troubleshooting (fast)
- WebDriverManager network/parse errors → run without it: -Duse.wdm=false (Selenium Manager will attempt driver).
- CDP warnings for Chrome → add matching selenium-devtools artifact if you need DevTools.
- Driver/browser mismatch → update browser, enable -Duse.wdm=true, or supply -Dbrowser.version.

Enable @Step (AspectJ AOP)
- Why steps may not appear:
  - The @Step annotation is implemented with an AspectJ aspect (StepAspect). If AspectJ weaving is not enabled, the aspect won't run and steps won't be created in the reporter. You do not need Spring for this — use either load-time weaving (javaagent) or compile-time weaving.

- Quick options to enable AOP for plain Selenium/TestNG projects (no Spring required):

  1) Load-time (runtime) weaving — recommended for local/IDE runs
     - Add aspectjweaver to your test/runtime classpath (pom.xml):
       <dependency>
         <groupId>org.aspectj</groupId>
         <artifactId>aspectjweaver</artifactId>
         <version>1.9.9.1</version>
         <scope>test</scope>
       </dependency>

     - Start the JVM with AspectJ javaagent. Example IntelliJ VM options:
       -javaagent:${user.home}/.m2/repository/org/aspectj/aspectjweaver/1.9.9.1/aspectjweaver-1.9.9.1.jar

     - Or configure Maven Surefire to add the agent at test runtime (pom.xml):
       <plugin>
         <groupId>org.apache.maven.plugins</groupId>
         <artifactId>maven-surefire-plugin</artifactId>
         <configuration>
           <argLine>-javaagent:${settings.localRepository}/org/aspectj/aspectjweaver/1.9.9.1/aspectjweaver-1.9.9.1.jar ${argLine}</argLine>
         </configuration>
       </plugin>

  2) Compile-time / post-compile weaving (CI-friendly)
     - Use aspectj-maven-plugin to weave aspects during build (recommended for CI to avoid passing agents):
       <plugin>
         <groupId>org.codehaus.mojo</groupId>
         <artifactId>aspectj-maven-plugin</artifactId>
         <version>1.14.0</version>
         <configuration>
           <complianceLevel>17</complianceLevel>
           <source>17</source>
           <target>17</target>
         </configuration>
         <executions>
           <execution>
             <goals>
               <goal>compile</goal>
               <goal>test-compile</goal>
             </goals>
           </execution>
         </executions>
       </plugin>

- Quick verification (no Spring):
  - Add a debug log in StepAspect.aroundStep (already added in project) and run a test.
  - If you see "StepAspect invoked for: ..." in logs, weaving is active and @Step will produce report steps.
  - If not, enable load-time agent or use the aspectj-maven-plugin to weave at compile/test-compile.

Notes
- Use load-time weaving during local runs (IntelliJ) by adding the -javaagent argument; use compile-time weaving for CI pipelines to avoid setting agents.
- After enabling weaving, @Step methods will be intercepted and reporter.withinStep will create steps in Allure/Extent immediately.
- Check org.example.configure.Config for exact property names and defaults.
- Look at build logs for generated artifacts (testng.xml, allure results).
