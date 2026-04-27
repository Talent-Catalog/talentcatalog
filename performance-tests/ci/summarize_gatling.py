#!/usr/bin/env python3
"""
summarize_gatling.py

Purpose
-------
Convert a single Gatling HTML report (the newest run directory) into:
  1) A small Markdown summary (human readable)
  2) A small JSON summary (machine readable, used by performance_threshold_check.py)

Why this exists
---------------
Gatling's native report lives under: <report-root>/<run-dir>/js/stats.json
That file is not very CI-friendly to parse across many shards/simulations.

Also: Gatling sometimes writes "-" (a string) for numeric fields when there are
no requests or stats are missing. This script normalizes those values into 0
so downstream gating does not fail with "missing_requests" due to non-numeric data.

Inputs (CLI)
------------
--report-root  : folder that contains Gatling run directories
--out-md       : where to write the Markdown summary
--out-json     : where to write the JSON summary
--sim-class    : simulation class name (for traceability)
--run-id       : unique run id (for traceability)

Output JSON shape (important)
----------------------------
{
  "runId": "...",
  "simClass": "...",
  "runDir": "...",
  "global": {
    "requests": { "total": <int>, "ok": <int>, "ko": <int> },
    "rtMs": { "min": <int>, "mean": <int>, "p50": <int>, "p75": <int>, "p95": <int>, "p99": <int>, "max": <int> },
    "hasRequests": <bool>
  }
}
"""

import argparse
import json
from pathlib import Path
from typing import Any, Optional


def newest_run_dir(report_root: Path) -> Optional[Path]:
    """
    Return the newest Gatling run directory in report_root.

    Gatling run directories typically look like:
      <report-root>/<simulation-name>-<timestamp>/

    We pick the directory with the most recent modification time.
    This works well in CI where we clean the report root between simulations.
    """
    if not report_root.exists():
        return None

    run_dirs = [p for p in report_root.iterdir() if p.is_dir()]
    if not run_dirs:
        return None

    run_dirs.sort(key=lambda p: p.stat().st_mtime, reverse=True)
    return run_dirs[0]


def load_stats(run_dir: Path) -> Optional[dict]:
    """
    Load the Gatling stats.json from a run directory.

    Expected location:
      <run-dir>/js/stats.json

    Returns None if the file does not exist.
    """
    stats = run_dir / "js" / "stats.json"
    if not stats.exists():
        return None
    return json.loads(stats.read_text(encoding="utf-8"))


def pick_global(stats: dict) -> dict:
    """
    Extract the global aggregate stats from a Gatling stats.json structure.

    Standard Gatling layout:
      stats_json["stats"] => global aggregate across all requests
    """
    return stats.get("stats", {}) or {}


def to_int_or_zero(x: Any) -> int:
    """
    Convert Gatling values to an integer safely, defaulting to 0.

    Why:
    - Gatling can output "-" for missing data (common when total requests == 0).
    - Some values might be strings like "123" or "123.0".
    - Downstream gating expects numeric values.

    Rules:
    - None / "" / "-" => 0
    - bool => 0/1
    - float => int(float)
    - invalid strings => 0
    """
    if x is None:
        return 0
    if isinstance(x, bool):
        return int(x)
    if isinstance(x, int):
        return x
    if isinstance(x, float):
        return int(x)
    if isinstance(x, str):
        s = x.strip()
        if s == "" or s == "-":
            return 0
        try:
            return int(float(s))
        except Exception:
            return 0
    return 0


def main() -> int:
    """
    Main entrypoint:
    - Find newest run directory under --report-root
    - Read stats.json
    - Normalize / extract metrics
    - Write Markdown + JSON summaries
    """
    ap = argparse.ArgumentParser(description="Summarize a Gatling report into MD + JSON.")
    ap.add_argument(
        "--report-root",
        required=True,
        help="Directory containing Gatling run directories (each contains js/stats.json).",
    )
    ap.add_argument("--out-md", required=True, help="Output Markdown file path.")
    ap.add_argument("--out-json", required=True, help="Output JSON file path.")
    ap.add_argument("--sim-class", required=True, help="Simulation class name (for traceability).")
    ap.add_argument("--run-id", required=True, help="Unique run identifier (for traceability).")
    args = ap.parse_args()

    report_root = Path(args.report_root)
    out_md = Path(args.out_md)
    out_json = Path(args.out_json)

    # 1) Find newest run directory
    run_dir = newest_run_dir(report_root)
    if run_dir is None:
        # We still write output files so CI can upload artifacts and show a clear reason.
        out_md.write_text(
            f"# {args.run_id}\n\nNo Gatling run directory found under `{report_root}`.\n",
            encoding="utf-8",
        )
        out_json.write_text(
            json.dumps({"runId": args.run_id, "simClass": args.sim_class, "error": "no_run_dir"}, indent=2),
            encoding="utf-8",
        )
        return 0

    # 2) Load stats.json
    stats_json = load_stats(run_dir)
    if stats_json is None:
        out_md.write_text(
            f"# {args.run_id}\n\nNo `js/stats.json` found in `{run_dir}`.\n",
            encoding="utf-8",
        )
        out_json.write_text(
            json.dumps({"runId": args.run_id, "simClass": args.sim_class, "runDir": str(run_dir), "error": "no_stats_json"}, indent=2),
            encoding="utf-8",
        )
        return 0

    # 3) Extract global aggregate
    g = pick_global(stats_json)

    # -----------------------------
    # Requests (total/ok/ko)
    # -----------------------------
    # Gatling layout:
    #   g["numberOfRequests"] = { "total": ..., "ok": ..., "ko": ... }
    # These values can be "-", so normalize to ints.
    number_of_requests = g.get("numberOfRequests", {}) or {}
    total = to_int_or_zero(number_of_requests.get("total"))
    ok = to_int_or_zero(number_of_requests.get("ok"))
    ko = to_int_or_zero(number_of_requests.get("ko"))

    # -----------------------------
    # Response time (min/mean/max/percentiles)
    # -----------------------------
    # Gatling layout:
    #   g["responseTime"] = { "min": ..., "mean": ..., "max": ..., "percentiles1..4": ... }
    # Percentiles are usually:
    #   percentiles1=p50, percentiles2=p75, percentiles3=p95, percentiles4=p99
    rt = g.get("responseTime", {}) or {}
    minv = to_int_or_zero(rt.get("min"))
    mean = to_int_or_zero(rt.get("mean"))
    maxv = to_int_or_zero(rt.get("max"))

    p50 = to_int_or_zero(rt.get("percentiles1"))
    p75 = to_int_or_zero(rt.get("percentiles2"))
    p95 = to_int_or_zero(rt.get("percentiles3"))
    p99 = to_int_or_zero(rt.get("percentiles4"))

    # 4) Build compact JSON output expected by threshold checker
    out = {
        "runId": args.run_id,
        "simClass": args.sim_class,
        "runDir": str(run_dir),
        "global": {
            "requests": {"total": total, "ok": ok, "ko": ko},
            "rtMs": {
                "min": minv,
                "mean": mean,
                "p50": p50,
                "p75": p75,
                "p95": p95,
                "p99": p99,
                "max": maxv,
            },
            # Convenience flag for CI debugging:
            # If false, the simulation likely did not send any requests (env down, auth, feeder, etc.)
            "hasRequests": total > 0,
        },
    }

    # 5) Human-readable Markdown summary
    md = f"""# Perf Summary: {args.run_id}

**Simulation**
- Class: `{args.sim_class}`
- Report folder: `{run_dir.name}`

## Global
- Requests: total={total}, ok={ok}, ko={ko}
- Response time (ms): min={minv}, mean={mean}, p50={p50}, p75={p75}, p95={p95}, p99={p99}, max={maxv}
"""

    # 6) Write outputs (always)
    out_md.write_text(md, encoding="utf-8")
    out_json.write_text(json.dumps(out, indent=2), encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
