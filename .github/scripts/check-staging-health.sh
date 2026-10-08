#
# Copyright (c) 2026 Talent Catalog.
#
# This program is free software: you can redistribute it and/or modify it under
#  the terms of the GNU General Public License as published by the Free
#  Software Foundation, either version 3 of the License, or any later version.
#
# This program is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
# for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see https://www.gnu.org/licenses/.
#

#!/usr/bin/env bash
set -euo pipefail

instance="${INSTANCE:-staging}"

if [[ -z "${HEALTH_URL:-}" ]]; then
  echo "::error::Missing health URL for $instance"
  exit 1
fi

# Give the application a short window to become healthy.
for attempt in {1..6}; do
  if curl -fsS --connect-timeout 5 --max-time 15 "$HEALTH_URL" \
    | jq -e '.status == "UP"' > /dev/null; then
    echo "::notice::$instance staging is healthy (UP)."
    exit 0
  fi

  echo "$instance health check failed ($attempt/6)."
  if (( attempt < 6 )); then sleep 10; fi
done

echo "::error::$instance staging is unhealthy. Validation stopped."
exit 1
