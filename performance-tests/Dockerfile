# syntax=docker/dockerfile:1.6
FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
  bash python3 ca-certificates git \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /work

# Copy minimal first for caching
COPY gradlew /work/gradlew
COPY gradle /work/gradle
COPY settings.gradle* /work/
COPY build.gradle* /work/
COPY performance-tests /work/performance-tests

RUN chmod +x /work/gradlew \
  && chmod +x /work/performance-tests/ci/run-sim-list.sh \
  && chmod +x /work/performance-tests/ci/summarize_gatling.py \
  && chmod +x /work/performance-tests/ci/budget_check.py

# Warm deps cache
RUN --mount=type=cache,target=/root/.gradle \
  /work/gradlew :performance-tests:dependencies --no-daemon || true

ENTRYPOINT ["/bin/bash"]
