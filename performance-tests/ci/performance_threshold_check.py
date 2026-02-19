#!/usr/bin/env python3
"""
Performance Threshold Check

Reads one or more per-run summary JSON files (produced by summarize_gatling.py) and enforces:
  1) Correctness threshold: failed request percentage (KO%)
  2) Latency threshold: selected percentile response time

This script is designed to work well for:
- Nightly runs (often small sample sizes): enforce strict percentile (p95), default to lenient percentile (p50)
- Soak / larger runs: enforce strict percentile (p95) once sample size is large enough

Exit codes:
  0 = PASS
  1 = script/config error (e.g., no summary files)
  2 = threshold failures
"""

import argparse
import json
import sys
from pathlib import Path
from typing import Any, Optional, Tuple, List


def to_int(x: Any) -> Optional[int]:
    """
    Convert a value to an int when possible.

    Accepts:
      - int/float/bool
      - strings like "123" or "123.0"

    Returns None for empty/invalid values.
    """
    if x is None:
        return None
    if isinstance(x, bool):
        return int(x)
    if isinstance(x, (int, float)):
        return int(x)
    if isinstance(x, str):
        s = x.strip()
        if not s:
            return None
        try:
            # Accept both "123" and "123.0"
            return int(float(s))
        except Exception:
            return None
    return None


def load_json(path: Path) -> Tuple[Optional[dict], Optional[str]]:
    """
    Load JSON from disk.
    Returns: (data, error_message)
    """
    try:
        return json.loads(path.read_text(encoding="utf-8")), None
    except Exception as e:
        return None, str(e)


def pick_percentile(rt_ms: dict, preferred: str) -> Tuple[Optional[int], str]:
    """
    Pick a percentile value from the rtMs dict.

    Returns:
      (value, label_used)

    Fallback order:
      preferred -> p50 -> min -> none
    """
    preferred_val = to_int(rt_ms.get(preferred))
    if preferred_val is not None:
        return preferred_val, preferred

    p50_val = to_int(rt_ms.get("p50"))
    if p50_val is not None:
        return p50_val, "p50"

    min_val = to_int(rt_ms.get("min"))
    if min_val is not None:
        return min_val, "min"

    return None, "none"


def compute_failed_pct(total: int, ko: int) -> float:
    """Compute KO% safely."""
    if total <= 0:
        return 0.0
    return (ko / total) * 100.0


def main() -> int:
    ap = argparse.ArgumentParser(description="Performance Threshold Check")

    ap.add_argument(
        "--summary-json-dir",
        required=True,
        help="Directory containing summary .json files (from summarize_gatling.py)",
    )

    # -----------------------------
    # Correctness (KO%) threshold
    # -----------------------------
    ap.add_argument(
        "--max-failed-pct",
        type=float,
        default=100.0,
        help="Maximum allowed failed requests percentage (KO%%). "
             "Default 100.0 for 'let it pass now'.",
    )

    # -----------------------------
    # Latency threshold
    # -----------------------------
    ap.add_argument(
        "--max-latency-ms",
        type=int,
        default=10**9,
        help="Maximum allowed latency (ms) for the selected percentile. "
             "Default is huge for 'let it pass now'.",
    )
    ap.add_argument(
        "--latency-percentile",
        choices=["p95","p75", "p50", "min"],
        default="p50",
        help="Percentile used for latency gating when sample size is small. "
             "Default p50 is more stable for small nightlies.",
    )
    ap.add_argument(
        "--min-requests-for-strict",
        type=int,
        default=200,
        help="Only enforce strict percentile (p95/p75) if total requests >= this. Default 200.",
    )
    ap.add_argument(
        "--strict-percentile",
        choices=["p95", "p75"],
        default="p95",
        help="Percentile to enforce when enough samples exist. Default p95.",
    )

    # -----------------------------
    # Behavior switches
    # -----------------------------
    ap.add_argument(
        "--ignore-failures",
        action="store_true",
        help="Ignore KO%% completely (PASS even if there are HTTP 400/500 errors).",
    )
    ap.add_argument(
        "--ignore-latency",
        action="store_true",
        help="Ignore latency checks completely.",
    )
    ap.add_argument(
        "--warn-only",
        action="store_true",
        help="Never fail (exit 0) but print what would have failed.",
    )

    args = ap.parse_args()

    summary_dir = Path(args.summary_json_dir)
    files = sorted(summary_dir.glob("*.json"))

    if not files:
        print(f"❌ Performance Threshold Check: no summary json files found in {summary_dir}")
        return 1

    failures: List[Tuple[str, str]] = []

    for f in files:
        data, err = load_json(f)
        if data is None:
            failures.append((f.name, f"invalid_json: {err}"))
            continue

        # Expected structure:
        # data["global"]["requests"]["total"/"ko"]
        # data["global"]["rtMs"]["p50"/"p95"/...]
        g = (data or {}).get("global", {}) or {}
        req = g.get("requests", {}) or {}
        rt = g.get("rtMs", {}) or {}

        total_i = to_int(req.get("total"))
        ko_i = to_int(req.get("ko"))

        if total_i is None or ko_i is None:
            failures.append((f.name, "missing_requests"))
            continue

        # -----------------------------
        # 1) Correctness gate (KO%)
        # -----------------------------
        if not args.ignore_failures:
            failed_pct = compute_failed_pct(total_i, ko_i)
            if failed_pct > args.max_failed_pct:
                failures.append((f.name, f"failed_pct {failed_pct:.2f} > {args.max_failed_pct}"))

        # -----------------------------
        # 2) Latency gate
        #    Strict percentile when sample size is large enough,
        #    otherwise use a lenient percentile (defaults to p50).
        # -----------------------------
        if not args.ignore_latency:
            if total_i >= args.min_requests_for_strict:
                latency_val, used = pick_percentile(rt, args.strict_percentile)
                gate_label = f"{used} (strict, n={total_i})"
            else:
                latency_val, used = pick_percentile(rt, args.latency_percentile)
                gate_label = f"{used} (lenient, n={total_i})"

            if latency_val is not None and latency_val > args.max_latency_ms:
                failures.append(
                    (f.name, f"latency {gate_label} {latency_val}ms > {args.max_latency_ms}ms")
                )

    # -----------------------------
    # Final report / exit code
    # -----------------------------
    if failures:
        print("❌ Performance Threshold Check failed:")
        for name, reason in failures:
            print(f" - {name}: {reason}")

        if args.warn_only:
            print("⚠️ warn-only enabled: returning success (0)")
            return 0

        return 2

    print("✅ Performance Threshold Check passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
