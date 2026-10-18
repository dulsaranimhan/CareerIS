FROM node:22-alpine AS frontend-build
WORKDIR /workspace/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /workspace/backend
COPY backend/pom.xml ./
COPY backend/src ./src
COPY --from=frontend-build /workspace/frontend/dist ./src/main/resources/static
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app
COPY --from=backend-build /workspace/backend/target/careeris-api-0.1.0-SNAPSHOT.jar ./careeris.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/careeris.jar"]
