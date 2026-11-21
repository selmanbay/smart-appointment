# Multi-stage build. The same image runs locally and when hosted (ADR-0008).
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY config config
COPY src src
RUN ./mvnw -B -q package -DskipTests -Dcheckstyle.skip -Dspotless.check.skip=true \
    && cp target/backend-*.jar app.jar

FROM eclipse-temurin:21-jre
RUN groupadd --system --gid 10001 app && useradd --system --uid 10001 --gid app app
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
