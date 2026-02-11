#!/usr/bin/env python3
import argparse
import json
from pathlib import Path

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--summary-json-dir", required=True, help="Directory containing summary .json files")
    ap.add_argument("--max-failed-pct", type=float, default=1.0)
    ap.add_argument("--max-p95-ms", type=int, default=15000)  # safe default until you tune per endpoint
    args = ap.parse_args()

    d = Path(args.summary_json_dir)
    files = sorted(d.glob("*.json"))

    if not files:
        print(f"No summary json files found in {d}")
        return 1

    failed = []
    for f in files:
        data = json.loads(f.read_text(encoding="utf-8"))
        g = data.get("global", {})
        req = g.get("requests", {})
        total = req.get("total")
        ko = req.get("ko")

        rt = g.get("rtMs", {})
        p95 = rt.get("p95")

        # Convert values if they are strings like "123"
        def to_int(x):
            try:
                return int(x)
            except Exception:
                return None

        total_i = to_int(total)
        ko_i = to_int(ko)
        p95_i = to_int(p95)

        if total_i is None or ko_i is None:
            failed.append((f.name, "missing_requests"))
            continue

        failed_pct = (ko_i / total_i) * 100.0 if total_i else 0.0
        if failed_pct > args.max_failed_pct:
            failed.append((f.name, f"failed_pct {failed_pct:.2f} > {args.max_failed_pct}"))

        if p95_i is not None and p95_i > args.max_p95_ms:
            failed.append((f.name, f"p95 {p95_i}ms > {args.max_p95_ms}ms"))

    if failed:
        print("❌ Budget checks failed:")
        for name, reason in failed:
            print(f" - {name}: {reason}")
        return 2

    print("✅ Budget checks passed")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
