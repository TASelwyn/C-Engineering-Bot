# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /src

# Gradle wrapper + build scripts first so dependency resolution is cached separately from source changes
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Wooclapper lives on GitHub Packages, so the build needs credentials (passed as BuildKit secrets, never baked into layers)
RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=secret,id=github_actor,env=GITHUB_ACTOR \
    --mount=type=secret,id=github_token,env=GITHUB_TOKEN \
    ./gradlew --no-daemon dependencies > /dev/null

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=secret,id=github_actor,env=GITHUB_ACTOR \
    --mount=type=secret,id=github_token,env=GITHUB_TOKEN \
    ./gradlew --no-daemon installDist

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /src/build/install/cengbot/ ./

# config.json, cengbot.sqlite and logs/ all live here (relative to the working directory)
VOLUME /app/data

ENTRYPOINT ["./cengbot"]
