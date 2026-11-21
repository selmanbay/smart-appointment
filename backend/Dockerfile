# Multi-stage build from the repository root, so the API contract (api/openapi.yaml) is packaged
# for Swagger UI. Build: docker build -f backend/Dockerfile .   (ADR-0008, ADR-0014)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace/backend
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY api /workspace/api
COPY backend/config config
COPY backend/src src
RUN ./mvnw -B -q package -DskipTests -Dcheckstyle.skip -Dspotless.check.skip=true \
    && cp target/backend-*.jar app.jar

FROM eclipse-temurin:21-jre
RUN groupadd --system --gid 10001 app && useradd --system --uid 10001 --gid app app
WORKDIR /app
COPY --from=build /workspace/backend/app.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
