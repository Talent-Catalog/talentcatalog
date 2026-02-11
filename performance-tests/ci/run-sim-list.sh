#!/usr/bin/env bash
#
# Copyright (c) 2026 Talent Catalog.
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
# for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program. If not, see https://www.gnu.org/licenses/.
#

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

# Basic shard sanity
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

# Read sims (ignore blanks/comments)
mapfile -t ALL_SIMS < <(grep -vE '^\s*#|^\s*$' "$LIST_FILE" || true)

if [[ ${#ALL_SIMS[@]} -eq 0 ]]; then
  echo "No simulations found in $LIST_FILE"
  exit 1
fi

# Select sims for this shard
SIMS=()
for i in "${!ALL_SIMS[@]}"; do
  if (( i % SHARD_TOTAL == SHARD_INDEX )); then
    SIMS+=("${ALL_SIMS[$i]}")
  fi
done

echo "Simulations in this shard: ${#SIMS[@]}"
printf ' - %s\n' "${SIMS[@]}"

# Prepare output dirs
REPORT_ROOT="performance-tests/build/reports/gatling"
RAW_ROOT="performance-tests/build/perf-artifacts/raw"
SUMMARY_ROOT="performance-tests/build/perf-artifacts/summary"
mkdir -p "$RAW_ROOT" "$SUMMARY_ROOT" "$REPORT_ROOT"

# Split extra args into JVM -D props vs other Gradle args
# We must pass -D... BEFORE the task name to avoid:
#   "Unknown command-line option '-D'"
JVM_PROPS=()
GRADLE_ARGS=()
for a in "$@"; do
  if [[ "$a" == -D* ]]; then
    JVM_PROPS+=("$a")
  else
    GRADLE_ARGS+=("$a")
  fi
done

# Run each sim
for LINE in "${SIMS[@]}"; do
  echo
  echo "=============================="
  echo "Running entry: $LINE"
  echo "=============================="

  # Split: "Class | -D... -D..."
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

  # Reset exit code per sim
  unset EXIT_CODE

  # Avoid report mixing between sims
  rm -rf "$REPORT_ROOT"/* || true

  # Run Gatling
  # - JVM_PROPS (-D...) must come before the task
  # - SIM_PROPS from the list file are appended as-is (usually -D... too)
  # shellcheck disable=SC2086
  ./gradlew --no-daemon \
    "${JVM_PROPS[@]}" \
    :performance-tests:gatlingTest \
    -PsimClass="$SIM_CLASS" \
    $SIM_PROPS \
    "${GRADLE_ARGS[@]}" || EXIT_CODE=$?

  # Find newest simulation.log (if created) — safe even if none exist
  LATEST_LOG="$(find "$REPORT_ROOT" -name simulation.log -type f -print0 2>/dev/null | xargs -0 -r ls -1t 2>/dev/null | head -n 1 || true)"
  if [[ -n "$LATEST_LOG" && -f "$LATEST_LOG" ]]; then
    cp "$LATEST_LOG" "$RAW_ROOT/${RUN_ID}_simulation.log"
  else
    echo "WARN: simulation.log not found for $SIM_CLASS"
  fi

  # Summary is optional
  if [[ -f "performance-tests/ci/summarize_gatling.py" ]]; then
    python3 performance-tests/ci/summarize_gatling.py \
      --report-root "$REPORT_ROOT" \
      --out-md "$SUMMARY_ROOT/${RUN_ID}.md" \
      --out-json "$SUMMARY_ROOT/${RUN_ID}.json" \
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
