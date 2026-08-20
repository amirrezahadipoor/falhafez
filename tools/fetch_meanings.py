# -*- coding: utf-8 -*-
"""Fetches Ganjoor's couplet-by-couplet summaries (معنی بیت) for Hafez + Khayyam
and merges them into the existing offline corpus assets. Ganjoor's summaries are
public-domain modern-Persian prose; the "هوش مصنوعی:" label is stripped."""
import json, re, urllib.request, time
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE = "https://api.ganjoor.net/api/ganjoor"

def get(path, retries=4):
    req = urllib.request.Request(BASE + path, headers={"User-Agent": "falhafez-corpus/1.0"})
    for a in range(retries):
        try:
            with urllib.request.urlopen(req, timeout=40) as r:
                return json.loads(r.read().decode("utf-8"))
        except Exception:
            if a == retries - 1: raise
            time.sleep(1.0 + a)
    return None

def clean(s):
    if not s: return None
    s = re.sub(r"^هوش مصنوعی[:：]\s*", "", s).strip()
    s = re.sub(r"\s+", " ", s)
    return s if s else None

def fetch_meanings(pid):
    try:
        d = get(f"/poem/{pid}")
        if d is None or "verses" not in d:
            return pid, None
        m = {}
        for v in d["verses"]:
            if v.get("coupletSummary"):
                m[v["coupletIndex"]] = clean(v["coupletSummary"])
        return pid, m
    except Exception:
        return pid, None

def merge(path, name):
    data = json.load(open(path, encoding="utf-8"))
    ids = [p["id"] for p in data]
    results, fail = {}, []
    with ThreadPoolExecutor(max_workers=6) as ex:
        futs = {ex.submit(fetch_meanings, pid): pid for pid in ids}
        for i, fut in enumerate(as_completed(futs), 1):
            pid, m = fut.result()
            if m is None: fail.append(pid)
            else: results[pid] = m
            if i % 100 == 0: print(f"  {name}: {i}/{len(ids)}", flush=True)
    applied = 0
    for p in data:
        m = results.get(p["id"], {})
        for idx, verse in enumerate(p["verses"]):
            meaning = m.get(idx)
            if meaning:
                verse["meaning"] = meaning
                applied += 1
    json.dump(data, open(path, "w"), ensure_ascii=False, indent=1)
    print(f"{name}: {len(data)} poems, meanings applied={applied}, poems-without-summary={len(fail)}")

merge("app/src/main/assets/corpus/hafez.json", "hafez")
merge("app/src/main/assets/corpus/khayyam.json", "khayyam")
print("DONE")
