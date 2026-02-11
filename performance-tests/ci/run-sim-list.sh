#!/usr/bin/env bash
set -euo pipefail

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

mapfile -t ALL_SIMS < <(grep -vE '^\s*#|^\s*$' "$LIST_FILE" || true)
if [[ ${#ALL_SIMS[@]} -eq 0 ]]; then
  echo "No simulations found in $LIST_FILE"
  exit 1
fi

SIMS=()
for i in "${!ALL_SIMS[@]}"; do
  if (( i % SHARD_TOTAL == SHARD_INDEX )); then
    SIMS+=("${ALL_SIMS[$i]}")
  fi
done

echo "Simulations in this shard: ${#SIMS[@]}"
printf ' - %s\n' "${SIMS[@]}"

# IMPORTANT:
# - Do NOT write Gradle build output into the repo (mounted /work) because it may be read-only in containers.
# - Use /tmp for all Gradle build output and copy artifacts to mounted dirs.

HOST_REPORT_ROOT="/work/build/reports/gatling"                 # mounted to perf-reports
HOST_RAW_ROOT="/work/perf-raw"                                # mounted to perf-raw
HOST_SUMMARY_ROOT="/work/perf-summary"                        # mounted to perf-summary
mkdir -p "$HOST_REPORT_ROOT" "$HOST_RAW_ROOT" "$HOST_SUMMARY_ROOT"

TMP_BUILD_ROOT="/tmp/tc-build"
TMP_REPORT_ROOT="$TMP_BUILD_ROOT/reports/gatling"
mkdir -p "$TMP_BUILD_ROOT" "$TMP_REPORT_ROOT"

JVM_PROPS=()
GRADLE_ARGS=()
for a in "$@"; do
  if [[ "$a" == -D* ]]; then
    JVM_PROPS+=("$a")
  else
    GRADLE_ARGS+=("$a")
  fi
done

copy_latest_report_dir() {
  # Copy the newest Gatling run dir to host report root
  local newest
  newest="$(ls -1dt "$TMP_REPORT_ROOT"/* 2>/dev/null | head -n 1 || true)"
  if [[ -n "$newest" && -d "$newest" ]]; then
    cp -R "$newest" "$HOST_REPORT_ROOT/" || true
  fi
}

for LINE in "${SIMS[@]}"; do
  echo
  echo "=============================="
  echo "Running entry: $LINE"
  echo "=============================="

  SIM_CLASS="$(echo "$LINE" | awk -F'\\|' '{print $1}' | xargs)"
  SIM_PROPS="$(echo "$LINE" | awk -F'\\|' '{print $2}' | xargs || true)"

  if [[ -z "$SIM_CLASS" ]]; then
    echo "WARN: empty simulation class in line: $LINE"
    continue
  fi

  echo "Simulation class: $SIM_CLASS"
  echo "Simulation props: ${SIM_PROPS:-<none>}"

  SAFE_SIM="${SIM_CLASS//./_}"
  TS="$(date -u +%Y%m%dT%H%M%SZ)"
  RUN_ID="${SAFE_SIM}_${TS}_shard${SHARD_INDEX}"

  unset EXIT_CODE

  # Clean tmp report output between sims to avoid mixing
  rm -rf "$TMP_REPORT_ROOT"/* || true

  # Run Gatling
  # -D props must come before task
  # Force Gradle build output to /tmp (writable), not /work (mounted repo)
  # shellcheck disable=SC2086
  ./gradlew --no-daemon \
    "${JVM_PROPS[@]}" \
    -g "${GRADLE_USER_HOME:-/tmp/.gradle}" \
    -Dorg.gradle.project.performanceTestsBuildDir="$TMP_BUILD_ROOT" \
    :performance-tests:gatlingTest \
    -PsimClass="$SIM_CLASS" \
    $SIM_PROPS \
    "${GRADLE_ARGS[@]}" || EXIT_CODE=$?

  # Copy newest report dir (contains index.html etc) to mounted perf-reports
  copy_latest_report_dir

  # Copy simulation.log if present (from tmp report root)
  LATEST_LOG="$(find "$TMP_REPORT_ROOT" -name simulation.log -type f -print0 2>/dev/null | xargs -0 -r ls -1t 2>/dev/null | head -n 1 || true)"
  if [[ -n "$LATEST_LOG" && -f "$LATEST_LOG" ]]; then
    cp "$LATEST_LOG" "$HOST_RAW_ROOT/${RUN_ID}_simulation.log"
  else
    echo "WARN: simulation.log not found for $SIM_CLASS"
  fi

  # Summary
  if [[ -f "/work/performance-tests/ci/summarize_gatling.py" ]]; then
    python3 /work/performance-tests/ci/summarize_gatling.py \
      --report-root "$TMP_REPORT_ROOT" \
      --out-md "$HOST_SUMMARY_ROOT/${RUN_ID}.md" \
      --out-json "$HOST_SUMMARY_ROOT/${RUN_ID}.json" \
      --sim-class "$SIM_CLASS" \
      --run-id "$RUN_ID" || true
  else
    echo "WARN: summarize_gatling.py not found, skipping summary generation"
  fi

  if [[ -n "${EXIT_CODE:-}" && "${EXIT_CODE:-0}" -ne 0 ]]; then
    echo "Simulation failed: $SIM_CLASS (exit ${EXIT_CODE})"
    exit "$EXIT_CODE"
  fi
done
