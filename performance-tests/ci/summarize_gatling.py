#!/usr/bin/env python3
import argparse
import json
import os
from pathlib import Path

def newest_run_dir(report_root: Path) -> Path | None:
    # Gatling creates dirs like: build/reports/gatling/<simulation-name>-<timestamp>/
    if not report_root.exists():
        return None
    run_dirs = [p for p in report_root.iterdir() if p.is_dir()]
    if not run_dirs:
        return None
    run_dirs.sort(key=lambda p: p.stat().st_mtime, reverse=True)
    return run_dirs[0]

def load_stats(run_dir: Path) -> dict | None:
    stats = run_dir / "js" / "stats.json"
    if not stats.exists():
        return None
    return json.loads(stats.read_text(encoding="utf-8"))

def pick_global(stats: dict) -> dict:
    # Standard Gatling layout:
    # stats["stats"] => global
    return stats.get("stats", {})

def to_number(x):
    try:
        return float(x)
    except Exception:
        return None

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--report-root", required=True)
    ap.add_argument("--out-md", required=True)
    ap.add_argument("--out-json", required=True)
    ap.add_argument("--sim-class", required=True)
    ap.add_argument("--run-id", required=True)
    args = ap.parse_args()

    report_root = Path(args.report_root)
    run_dir = newest_run_dir(report_root)
    if run_dir is None:
        Path(args.out_md).write_text(f"# {args.run_id}\n\nNo Gatling run directory found.\n", encoding="utf-8")
        Path(args.out_json).write_text(json.dumps({"runId": args.run_id, "error": "no_run_dir"}), encoding="utf-8")
        return

    stats_json = load_stats(run_dir)
    if stats_json is None:
        Path(args.out_md).write_text(f"# {args.run_id}\n\nNo stats.json found in {run_dir}.\n", encoding="utf-8")
        Path(args.out_json).write_text(json.dumps({"runId": args.run_id, "error": "no_stats_json"}), encoding="utf-8")
        return

    g = pick_global(stats_json)

    # Extract common fields
    name = g.get("name")
    numberOfRequests = g.get("numberOfRequests", {})
    ok = numberOfRequests.get("ok")
    ko = numberOfRequests.get("ko")
    total = numberOfRequests.get("total")

    rt = g.get("responseTime", {})
    p50 = rt.get("percentiles1")
    p75 = rt.get("percentiles2")
    p95 = rt.get("percentiles3")
    p99 = rt.get("percentiles4")
    mean = rt.get("mean")
    maxv = rt.get("max")
    minv = rt.get("min")

    out = {
        "runId": args.run_id,
        "simClass": args.sim_class,
        "runDir": str(run_dir),
        "global": {
            "requests": {"total": total, "ok": ok, "ko": ko},
            "rtMs": {"min": minv, "p50": p50, "p75": p75, "p95": p95, "p99": p99, "max": maxv, "mean": mean},
        }
    }

    md = f"""# Perf Summary: {args.run_id}

**Simulation**
- Class: `{args.sim_class}`
- Report folder: `{run_dir.name}`

## Global
- Requests: total={total}, ok={ok}, ko={ko}
- Response time (ms): min={minv}, mean={mean}, p50={p50}, p75={p75}, p95={p95}, p99={p99}, max={maxv}
"""

    Path(args.out_md).write_text(md, encoding="utf-8")
    Path(args.out_json).write_text(json.dumps(out, indent=2), encoding="utf-8")

if __name__ == "__main__":
    main()
