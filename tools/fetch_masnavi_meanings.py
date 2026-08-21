# -*- coding: utf-8 -*-
"""معنی بیت‌به‌بیت مثنوی مولانا را از گنجور می‌گیرد و در rumi.dat ادغام می‌کند."""
import json, gzip, re, urllib.request, time
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE = "https://api.ganjoor.net/api/ganjoor"

def get(path, retries=5):
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

path = "app/src/main/assets/corpus/rumi.dat"
data = json.loads(gzip.open(path, "rt", encoding="utf-8").read())
masnavi_ids = [p["id"] for p in data if p["collection"] == "masnavi"]
print("masnavi poems:", len(masnavi_ids))

results, fail = {}, []
with ThreadPoolExecutor(max_workers=8) as ex:
    futs = {ex.submit(fetch_meanings, pid): pid for pid in masnavi_ids}
    for i, fut in enumerate(as_completed(futs), 1):
        pid, m = fut.result()
        if m is None: fail.append(pid)
        else: results[pid] = m
        if i % 200 == 0: print(f"  {i}/{len(masnavi_ids)}", flush=True)

applied = 0
for p in data:
    if p["collection"] != "masnavi":
        continue
    m = results.get(p["id"], {})
    for idx, verse in enumerate(p["verses"]):
        meaning = m.get(idx)
        if meaning:
            verse["meaning"] = meaning
            applied += 1

with gzip.open(path, "wt", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, separators=(",", ":"))

print(f"applied={applied} meanings, poems-without={len(fail)}")
import os
print("rumi.dat size:", os.path.getsize(path)//1024, "KB")
