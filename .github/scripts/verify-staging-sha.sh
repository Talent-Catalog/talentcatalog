
#!/usr/bin/env bash
set -euo pipefail
# Get the Git commit SHA currently running on TC staging.
# Fail if the endpoint is unavailable or the SHA is missing.
running_sha=$(curl -fsS --max-time 20 \
  -H "Cache-Control: no-cache" \
  https://tctalent-test.org/actuator/info | jq -er '.build.commit') || {
  echo "::error::Cannot read staging Git SHA."
  exit 1
}
# Compare the running SHA with the commit deployed by GitHub Actions.
# Stop validation if they do not match.
if [[ "$running_sha" != "$EXPECTED_SHA" ]]; then
  echo "::error::SHA mismatch! Expected: $EXPECTED_SHA, Running: $running_sha"
  exit 1
fi
# The correct version is running, so validation can continue.
echo "Staging Git SHA verified: $running_sha"
