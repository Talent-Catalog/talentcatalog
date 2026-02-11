# syntax=docker/dockerfile:1.6
FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
  bash python3 ca-certificates git \
  && rm -rf /var/lib/apt/lists/*

# Create a non-root user to avoid root-owned build outputs at runtime
# (Matches how you run docker in CI with --user "$(id -u):$(id -g)")
RUN useradd -m -u 1000 -s /bin/bash runner

WORKDIR /work

# Copy minimal first for caching
COPY gradlew /work/gradlew
COPY gradle /work/gradle
COPY settings.gradle* /work/
COPY build.gradle* /work/
COPY performance-tests /work/performance-tests

# Ensure scripts are executable and everything under /work is writable by the non-root user
RUN chmod +x /work/gradlew \
  && chmod +x /work/performance-tests/ci/run-sim-list.sh \
  && chmod +x /work/performance-tests/ci/summarize_gatling.py \
  && chmod +x /work/performance-tests/ci/budget_check.py \
  && chown -R runner:runner /work

# Warm deps cache (as non-root) so the cached Gradle home isn't root-owned
USER runner
ENV GRADLE_USER_HOME=/home/runner/.gradle

RUN --mount=type=cache,target=/home/runner/.gradle \
  /work/gradlew :performance-tests:dependencies --no-daemon || true

ENTRYPOINT ["/bin/bash"]
