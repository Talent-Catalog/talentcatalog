# syntax=docker/dockerfile:1.6
FROM eclipse-temurin:17-jdk-jammy
# This will add:
# bash: to run shell scripts
# python3: for performance_threshold_check.py and summarize_gatling.py
# ca-certificates: so HTTPS downloads work
# git: sometimes Gradle deps/plugins come from git, or scripts may need it
RUN apt-get update && apt-get install -y --no-install-recommends \
  bash python3 ca-certificates git \
  && rm -rf /var/lib/apt/lists/*

# Create a non-root user to avoid root-owned build outputs at runtime
# (Matches how we run docker in CI with --user "$(id -u):$(id -g)")
RUN useradd -m -u 1000 -s /bin/bash runner

# Inside the container, /work is the “current folder”.
WORKDIR /work

# Copy minimal first for caching
# This copies only what’s needed to run Gradle for performance-tests:
# gradlew + gradle/ (Gradle wrapper)
# root Gradle settings/build files
# the performance-tests module (Dockerfile + CI scripts + Gatling tests)
COPY gradlew /work/gradlew
COPY gradle /work/gradle
COPY settings.gradle* /work/
COPY build.gradle* /work/
COPY performance-tests /work/performance-tests

# This ensures:
# Gradle wrapper is runnable
# CI scripts are runnable
# everything under /work is owned by runner user to avoid permission headaches
RUN chmod +x /work/gradlew \
  && chmod +x /work/performance-tests/ci/run-sim-list.sh \
  && chmod +x /work/performance-tests/ci/summarize_gatling.py \
  && chmod +x /work/performance-tests/ci/performance_threshold_check.py \
  && chown -R runner:runner /work

# Warm deps cache (as non-root) so the cached Gradle home isn't root-owned
USER runner
ENV GRADLE_USER_HOME=/home/runner/.gradle

RUN --mount=type=cache,target=/home/runner/.gradle \
  /work/gradlew :performance-tests:dependencies --no-daemon || true

ENTRYPOINT ["/bin/bash"]
