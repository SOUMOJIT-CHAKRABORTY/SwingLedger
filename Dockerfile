FROM node:24-bookworm-slim AS frontend
WORKDIR /workspace/frontend
RUN npm install --global pnpm@11.25.0
COPY frontend/package.json frontend/pnpm-lock.yaml frontend/pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile
COPY frontend/ ./
RUN node node_modules/vite/bin/vite.js build

FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /workspace/server
COPY server/pom.xml ./
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline
COPY server/src ./src
COPY --from=frontend /workspace/server/src/main/resources/static ./src/main/resources/static
RUN mvn --batch-mode --no-transfer-progress package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN groupadd --system ledger && useradd --system --gid ledger ledger
COPY --from=backend --chown=ledger:ledger /workspace/server/target/app-0.0.1-SNAPSHOT.jar app.jar
USER ledger
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=65.0", "-jar", "/app/app.jar"]
