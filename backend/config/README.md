# Backend quality tooling

Configuration for the backend quality gates (ADR-0007). All of these are wired into `pom.xml`, so `./mvnw verify` fails on any violation and CI enforces it.

| Tool | Purpose | Config |
|---|---|---|
| Spotless + google-java-format | Formatting (no style debates) | `pom.xml` |
| Checkstyle | Naming, size, complexity, coupling, hygiene | `config/checkstyle.xml` |
| ArchUnit | Feature boundaries, layering, no cycles, SOLID-related rules | `src/test/java/com/smartappointment/ArchitectureTest.java` |
| JaCoCo | Coverage report; threshold added once code exists | `pom.xml` |

## pom.xml plugins

```xml
<plugin>
  <groupId>com.diffplug.spotless</groupId>
  <artifactId>spotless-maven-plugin</artifactId>
  <configuration>
    <java>
      <googleJavaFormat/>
      <removeUnusedImports/>
    </java>
  </configuration>
  <executions>
    <execution><goals><goal>check</goal></goals><phase>verify</phase></execution>
  </executions>
</plugin>

<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-checkstyle-plugin</artifactId>
  <configuration>
    <configLocation>config/checkstyle.xml</configLocation>
    <includeTestSourceDirectory>true</includeTestSourceDirectory>
    <consoleOutput>true</consoleOutput>
    <failOnViolation>true</failOnViolation>
    <violationSeverity>error</violationSeverity>
  </configuration>
  <dependencies>
    <dependency>
      <groupId>com.puppycrawl.tools</groupId>
      <artifactId>checkstyle</artifactId>
      <version>10.21.4</version>
    </dependency>
  </dependencies>
  <executions>
    <execution><goals><goal>check</goal></goals><phase>verify</phase></execution>
  </executions>
</plugin>
```

Plugin versions are pinned in `pom.xml` (CONTRIBUTING.md section 7). `config/checkstyle.xml` is run with Checkstyle 14.4.0.

## ArchUnit rules

Implemented in `src/test/java/com/smartappointment/ArchitectureTest.java` (rule 2 in `FeatureRules.java`):

1. Feature packages are free of cycles: `slices().matching("com.smartappointment.(*)..").should().beFreeOfCycles()`.
2. A feature does not access another feature's `..repository..` or entity classes; only its service.
3. Controllers (`@RestController`) depend only on services and DTOs, never on repositories.
4. `estimation..` depends only on `java..` (no Spring, no persistence).
5. No field injection: no fields annotated `@Autowired`.
6. Entities (`@Entity`) are not used as return types of controller methods.
7. Only `common.ratelimit`, `common.web` and `common.security` contain servlet filters.
8. No class outside `common.config` reads environment variables directly (`System.getenv`).

## Commands

```bash
./mvnw spotless:apply          # format
./mvnw checkstyle:check        # naming / size / design rules
./mvnw verify                  # everything, as in CI
```
