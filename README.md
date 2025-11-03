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

Reports
- Allure (default): run tests then:
  mvn allure:report
  mvn allure:serve

- ExtentReports: mvn clean test -DreportType=extent

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
