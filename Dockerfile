# A single "app" image contains both the backend (Spring Boot) and the frontend (React built assets served by Spring Boot).
# The build context is the project root directory.

# ---------- Stage 1: build frontend ----------
FROM node:22-slim AS frontend-build
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
# Vite embeds VITE_* values into the code at build time. "/" means the API is called from the same origin (the frontend is served by Spring Boot itself),
# so CORS is not required. It does not read frontend/.env (which is excluded by .dockerignore).
ARG VITE_API_BASE_URL=/
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# ---------- Stage 2: build the JAR file (including the frontend) ----------
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
# The built frontend is copied into Spring Boot's static directory -> it is included in the JAR package.
COPY --from=frontend-build /frontend/dist ./src/main/resources/static
# Cache ~/.m2 across builds (requires BuildKit, which is enabled by default in newer Docker versions).
# -DskipTests: test contextLoads require MySQL to be running, so skip tests when building the image.
RUN --mount=type=cache,target=/root/.m2 mvn -B package -DskipTests

# ---------- Stage 3: runtime image ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring --no-create-home spring
COPY --from=backend-build /workspace/target/*.jar app.jar
USER spring
EXPOSE 8080
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
