# -*- coding: utf-8 -*-
"""Verify ALL Hafez ghazals in the corpus against the live Ganjoor API."""
import gzip, json, re, urllib.request, time, unicodedata, collections, sys
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE = "https://api.ganjoor.net/api/ganjoor"

def get(path, retries=3):
    req = urllib.request.Request(BASE + path, headers={"User-Agent": "falhafez-verify/1.0"})
    for a in range(retries):
        try:
            with urllib.request.urlopen(req, timeout=30) as r:
                return json.loads(r.read().decode("utf-8"))
        except Exception:
            if a == retries - 1:
                return None
            time.sleep(1.0 + a)
    return None

DIAC = set("\u064b\u064c\u064d\u064e\u064f\u0650\u0651\u0652\u0670\u0654\u0655\u0656\u0657\u0658\u0671\u0640")
def norm(s):
    s = unicodedata.normalize("NFKC", s or "")
    s = "".join(c for c in s if c not in DIAC)
    s = s.replace("\u200c", " ").replace("\u200d", " ")
    s = re.sub(r"\s+", " ", s).strip()
    return s

data = json.loads(gzip.open("app/src/main/assets/corpus/hafez.dat", "rt", encoding="utf-8").read())
print("corpus poems:", len(data))

def check(p):
    pid = p["id"]
    d = get(f"/poem/{pid}")
    if d is None:
        return (pid, "FETCH_FAIL", None)
    gv = d.get("verses", [])
    couplets = {}
    for v in gv:
        ci = v.get("coupletIndex")
        pos = v.get("versePosition")
        txt = norm(v.get("text"))
        if ci is None:
            continue
        couplets.setdefault(ci, {})[pos] = txt
    gkeys = sorted(couplets.keys())
    cv = p["verses"]
    problems = []
    if len(cv) != len(gkeys):
        problems.append(f"COUNT corpus={len(cv)} ganjoor={len(gkeys)}")
    for i, v in enumerate(cv):
        if i >= len(gkeys):
            problems.append(f"EXTRA corpus couplet {i}: {norm(v['first'])[:30]!r}")
            continue
        ci = gkeys[i]
        gf = couplets[ci].get(0, "")
        gs = couplets[ci].get(1, "")
        cf = norm(v["first"] or "")
        cs = norm(v.get("second") or "")
        if cf != gf:
            problems.append(f"c{i}.first diff: corpus={cf[:36]!r} ganjoor={gf[:36]!r}")
        if cs != gs and (cs or gs):
            problems.append(f"c{i}.second diff: corpus={cs[:36]!r} ganjoor={gs[:36]!r}")
    return (pid, "OK" if not problems else "DIFF", problems)

results = []
with ThreadPoolExecutor(max_workers=10) as ex:
    futs = {ex.submit(check, p): p["id"] for p in data}
    for i, fut in enumerate(as_completed(futs), 1):
        results.append(fut.result())
        if i % 100 == 0:
            print(f"  {i}/{len(data)}", flush=True)

ok = [r for r in results if r[1] == "OK"]
diff = [r for r in results if r[1] == "DIFF"]
fail = [r for r in results if r[1] == "FETCH_FAIL"]
print(f"\nchecked {len(results)}: OK={len(ok)} DIFF={len(diff)} FETCH_FAIL={len(fail)}")
for r in diff:
    pid, _, probs = r
    p = next(x for x in data if x["id"] == pid)
    print(f"\n--- ghazal {p['number']} (id {pid}):")
    for pr in probs:
        print("    ", pr)
for r in fail:
    print("FETCH_FAIL:", r[0])
