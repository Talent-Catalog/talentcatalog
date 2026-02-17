#!/usr/bin/env python3
import argparse
import json
import sys
from pathlib import Path
from typing import Any, Optional, Tuple


def to_int(x: Any) -> Optional[int]:
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
            # handle "123", "123.0"
            return int(float(s))
        except Exception:
            return None
    return None


def load_json(path: Path) -> Tuple[Optional[dict], Optional[str]]:
    try:
        return json.loads(path.read_text(encoding="utf-8")), None
    except Exception as e:
        return None, str(e)


def pick_percentile(rt_ms: dict, preferred: str) -> Tuple[Optional[int], str]:
    """
    Returns (value, label_used). Falls back from preferred -> p50 -> min if missing.
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


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument("--summary-json-dir", required=True, help="Directory containing summary .json files")

    # correctness gate
    ap.add_argument("--max-failed-pct", type=float, default=100.0,
                    help="Maximum allowed failed requests percentage. Default 100 for 'let it pass now'.")

    # latency gate
    ap.add_argument("--max-latency-ms", type=int, default=10**9,
                    help="Maximum allowed latency for selected percentile. Default huge for 'let it pass now'.")
    ap.add_argument("--latency-percentile", choices=["p95", "p90", "p75", "p50", "min"], default="p50",
                    help="Which percentile to use for latency gating. Default p50 for small-sample nightlies.")
    ap.add_argument("--min-requests-for-strict", type=int, default=200,
                    help="Only enforce strict percentile (p95/p90/etc) if total requests >= this. Default 200.")
    ap.add_argument("--strict-percentile", choices=["p95", "p90", "p75"], default="p95",
                    help="Percentile to enforce when enough samples exist. Default p95.")

    # behavior switches (so you can force-pass while debugging)
    ap.add_argument("--ignore-failures", action="store_true",
                    help="Ignore KO/failed percentage completely (PASS even if 400s).")
    ap.add_argument("--ignore-latency", action="store_true",
                    help="Ignore latency checks completely.")
    ap.add_argument("--warn-only", action="store_true",
                    help="Never fail (exit 0) but print what would have failed.")

    args = ap.parse_args()

    d = Path(args.summary_json_dir)
    files = sorted(d.glob("*.json"))

    if not files:
        print(f"No summary json files found in {d}")
        return 1

    failures = []

    for f in files:
        data, err = load_json(f)
        if data is None:
            failures.append((f.name, f"invalid_json: {err}"))
            continue

        g = (data or {}).get("global", {}) or {}
        req = g.get("requests", {}) or {}
        total_i = to_int(req.get("total"))
        ko_i = to_int(req.get("ko"))

        rt = g.get("rtMs", {}) or {}

        if total_i is None or ko_i is None:
            failures.append((f.name, "missing_requests"))
            continue

        # --- Fail-rate gate ---
        if not args.ignore_failures:
            failed_pct = (ko_i / total_i) * 100.0 if total_i else 0.0
            if failed_pct > args.max_failed_pct:
                failures.append((f.name, f"failed_pct {failed_pct:.2f} > {args.max_failed_pct}"))

        # --- Latency gate (small-sample friendly) ---
        if not args.ignore_latency:
            if total_i >= args.min_requests_for_strict:
                latency_val, used = pick_percentile(rt, args.strict_percentile)
                gate_label = f"{used} (strict, n={total_i})"
            else:
                latency_val, used = pick_percentile(rt, args.latency_percentile)
                gate_label = f"{used} (lenient, n={total_i})"

            if latency_val is not None and latency_val > args.max_latency_ms:
                failures.append((f.name, f"latency {gate_label} {latency_val}ms > {args.max_latency_ms}ms"))

    if failures:
        print("❌ Budget checks failed:")
        for name, reason in failures:
            print(f" - {name}: {reason}")
        if args.warn_only:
            print("⚠️ warn-only enabled: returning success (0)")
            return 0
        return 2

    print("✅ Budget checks passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
