# ---- Build stage ----
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /workspace

# Download dependencies first so they are cached between builds
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B -q package -DskipTests \
    && java -Djarmode=layertools -jar target/employee-service-*.jar extract --destination target/extracted

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app

# Non-root user. Group 0 keeps the image compatible with OpenShift random UIDs.
RUN useradd --uid 1001 --gid 0 --no-create-home --shell /usr/sbin/nologin app

COPY --from=build --chown=1001:0 /workspace/target/extracted/dependencies/ ./
COPY --from=build --chown=1001:0 /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build --chown=1001:0 /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=1001:0 /workspace/target/extracted/application/ ./

USER 1001
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError" \
    SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.JarLauncher"]
