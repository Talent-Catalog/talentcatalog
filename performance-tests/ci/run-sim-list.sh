#!/usr/bin/env bash
set -euo pipefail

# run-sim-list.sh
#
# Runs a list of Gatling simulations (one per line) with optional per-simulation JVM props.
# Supports sharding via SHARD_INDEX/SHARD_TOTAL.
#
# Artifacts:
# - HTML reports:   /work/build/reports/gatling   (mounted to perf-reports in GitHub Actions)
# - Summaries:      /work/perf-summary           (mounted to perf-summary)
# - Raw logs:       /work/perf-raw               (mounted to perf-raw)
#
# IMPORTANT:
# - /work is mounted from the GitHub workspace. It is writable in your current setup,
#   but keeping build/temp output in /tmp is safer and avoids permission issues.
# - Your Gradle task currently hard-codes "-rf build/reports/gatling". We fix that by
#   passing a system property "-Dgatling.reportRoot=..." and updating Gradle to read it.

LIST_FILE="${1:-}"
shift || true

if [[ -z "$LIST_FILE" ]]; then
  echo "Usage: run-sim-list.sh <list-file> [extra gradle args...]"
  exit 1
fi
if [[ ! -f "$LIST_FILE" ]]; then
  echo "List file not found: $LIST_FILE"
  exit 1
fi

SHARD_INDEX="${SHARD_INDEX:-0}"
SHARD_TOTAL="${SHARD_TOTAL:-1}"

if ! [[ "$SHARD_TOTAL" =~ ^[0-9]+$ ]] || (( SHARD_TOTAL < 1 )); then
  echo "Invalid SHARD_TOTAL: $SHARD_TOTAL (must be >= 1)"
  exit 1
fi
if ! [[ "$SHARD_INDEX" =~ ^[0-9]+$ ]] || (( SHARD_INDEX < 0 || SHARD_INDEX >= SHARD_TOTAL )); then
  echo "Invalid SHARD_INDEX: $SHARD_INDEX (must be 0 <= index < total)"
  exit 1
fi

echo "Shard index: $SHARD_INDEX"
echo "Shard total: $SHARD_TOTAL"
echo "List file:   $LIST_FILE"
echo "Extra args:  $*"

# Read all sims, ignoring blank lines and comments.
mapfile -t ALL_SIMS < <(grep -vE '^\s*#|^\s*$' "$LIST_FILE" || true)
if [[ ${#ALL_SIMS[@]} -eq 0 ]]; then
  echo "No simulations found in $LIST_FILE"
  exit 1
fi

# Select shard slice.
SIMS=()
for i in "${!ALL_SIMS[@]}"; do
  if (( i % SHARD_TOTAL == SHARD_INDEX )); then
    SIMS+=("${ALL_SIMS[$i]}")
  fi
done

echo "Simulations in this shard: ${#SIMS[@]}"
printf ' - %s\n' "${SIMS[@]}"

# ------------------------------------------------------------------------------
# Artifact roots (mounted by CI)
# ------------------------------------------------------------------------------
HOST_REPORT_ROOT="/work/build/reports/gatling"   # mounted to perf-reports
HOST_RAW_ROOT="/work/perf-raw"                  # mounted to perf-raw
HOST_SUMMARY_ROOT="/work/perf-summary"          # mounted to perf-summary
mkdir -p "$HOST_REPORT_ROOT" "$HOST_RAW_ROOT" "$HOST_SUMMARY_ROOT"

# ------------------------------------------------------------------------------
# Writable temp roots inside the container
# ------------------------------------------------------------------------------
TMP_BUILD_ROOT="/tmp/tc-build"

# We want Gatling output to go into a temp directory, one sub-folder per simulation run.
# The Gradle task will be modified to read -Dgatling.reportRoot and pass it to "-rf".
TMP_REPORT_ROOT="/tmp/tc-gatling-reports"

mkdir -p "$TMP_BUILD_ROOT" "$TMP_REPORT_ROOT"

# Split extra CLI args into:
# - JVM_PROPS: args starting with -D... (must come before Gradle task)
# - GRADLE_ARGS: everything else (e.g., --info, -P..., etc.)
JVM_PROPS=()
GRADLE_ARGS=()
for a in "$@"; do
  if [[ "$a" == -D* ]]; then
    JVM_PROPS+=("$a")
  else
    GRADLE_ARGS+=("$a")
  fi
done

# Copy the newest Gatling run directory from TMP_REPORT_ROOT to HOST_REPORT_ROOT.
# Gatling creates dirs like: <simulation-name>-<timestamp>/
copy_latest_report_dir() {
  local newest
  newest="$(ls -1dt "$TMP_REPORT_ROOT"/* 2>/dev/null | head -n 1 || true)"
  if [[ -n "$newest" && -d "$newest" ]]; then
    cp -R "$newest" "$HOST_REPORT_ROOT/" || true
  fi
}

# Find newest simulation.log in TMP_REPORT_ROOT (if any).
find_latest_sim_log() {
  find "$TMP_REPORT_ROOT" -name simulation.log -type f -print0 2>/dev/null \
    | xargs -0 -r ls -1t 2>/dev/null \
    | head -n 1 || true
}

# Pick the report root that actually contains a Gatling stats.json.
# This guards against future changes where Gatling writes to a different place.
pick_report_root_with_stats() {
  local d
  for d in "$TMP_REPORT_ROOT" "$HOST_REPORT_ROOT"; do
    if [[ -d "$d" ]] && find "$d" -maxdepth 3 -type f -path "*/js/stats.json" | head -n 1 | grep -q .; then
      echo "$d"
      return 0
    fi
  done
  echo "$TMP_REPORT_ROOT"
}

# ------------------------------------------------------------------------------
# Main loop
# ------------------------------------------------------------------------------
for LINE in "${SIMS[@]}"; do
  echo
  echo "=============================="
  echo "Running entry: $LINE"
  echo "=============================="

  # Parse: "<SIM_CLASS> | <SIM_PROPS...>"
  SIM_CLASS="$(echo "$LINE" | awk -F'\\|' '{print $1}' | xargs)"
  SIM_PROPS_RAW="$(echo "$LINE" | awk -F'\\|' '{print $2}' | xargs || true)"

  if [[ -z "$SIM_CLASS" ]]; then
    echo "WARN: empty simulation class in line: $LINE"
    continue
  fi

  # Parse SIM_PROPS into an array.
  # Assumption: props are space-delimited -Dkey=value tokens (no spaces in values).
  SIM_PROPS=()
  if [[ -n "${SIM_PROPS_RAW:-}" ]]; then
    # shellcheck disable=SC2206
    SIM_PROPS=( ${SIM_PROPS_RAW} )
  fi

  echo "Simulation class: $SIM_CLASS"
  echo "Simulation props: ${SIM_PROPS_RAW:-<none>}"

  SAFE_SIM="${SIM_CLASS//./_}"
  TS="$(date -u +%Y%m%dT%H%M%SZ)"
  RUN_ID="${SAFE_SIM}_${TS}_shard${SHARD_INDEX}"

  unset EXIT_CODE

  # Clean temp report output between sims to avoid mixing runs.
  rm -rf "$TMP_REPORT_ROOT"/* || true

  # Run Gatling via Gradle.
  #
  # Critical: We pass -Dgatling.reportRoot="$TMP_REPORT_ROOT" so the Gradle task
  # can route Gatling reports to /tmp (NOT /work).
  #
  # Note: Your Gradle task must be updated (see below) to use this property.
  ./gradlew --no-daemon \
    "${JVM_PROPS[@]}" \
    -g "${GRADLE_USER_HOME:-/tmp/.gradle}" \
    -Dorg.gradle.project.performanceTestsBuildDir="$TMP_BUILD_ROOT" \
    -Dgatling.reportRoot="$TMP_REPORT_ROOT" \
    :performance-tests:gatlingTest \
    -PsimClass="$SIM_CLASS" \
    "${SIM_PROPS[@]}" \
    "${GRADLE_ARGS[@]}" || EXIT_CODE=$?

  # Helpful debug in CI: show where stats.json ended up.
  echo "DEBUG: stats.json (tmp):"
  find "$TMP_REPORT_ROOT" -maxdepth 3 -type f -path "*/js/stats.json" -print || true
  echo "DEBUG: stats.json (host):"
  find "$HOST_REPORT_ROOT" -maxdepth 3 -type f -path "*/js/stats.json" -print || true

  # Copy newest report dir (contains index.html etc) to mounted perf-reports.
  copy_latest_report_dir

  # Copy simulation.log if present.
  LATEST_LOG="$(find_latest_sim_log)"
  if [[ -n "$LATEST_LOG" && -f "$LATEST_LOG" ]]; then
    cp "$LATEST_LOG" "$HOST_RAW_ROOT/${RUN_ID}_simulation.log"
  else
    echo "WARN: simulation.log not found for $SIM_CLASS"
  fi

  # Summary generation:
  # We summarize from whichever root actually contains stats.json.
  REPORT_ROOT_USED="$(pick_report_root_with_stats)"

  if [[ -f "/work/performance-tests/ci/summarize_gatling.py" ]]; then
    python3 /work/performance-tests/ci/summarize_gatling.py \
      --report-root "$REPORT_ROOT_USED" \
      --out-md "$HOST_SUMMARY_ROOT/${RUN_ID}.md" \
      --out-json "$HOST_SUMMARY_ROOT/${RUN_ID}.json" \
      --sim-class "$SIM_CLASS" \
      --run-id "$RUN_ID" || true
  else
    echo "WARN: summarize_gatling.py not found, skipping summary generation"
  fi

  # If the simulation itself failed, fail fast with the same exit code.
  if [[ -n "${EXIT_CODE:-}" && "${EXIT_CODE:-0}" -ne 0 ]]; then
    echo "Simulation failed: $SIM_CLASS (exit ${EXIT_CODE})"
    exit "$EXIT_CODE"
  fi
done
