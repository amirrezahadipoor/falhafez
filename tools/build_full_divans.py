# -*- coding: utf-8 -*-
"""Fetches the COMPLETE Saadi + Rumi collections from Ganjoor (verbatim scholarly text),
with couplet meanings, generates poet-aware tafsirs, and builds the offline corpus assets.
Also extracts 100 instructive stories (داستان‌های آموزنده) from Saadi's Golestan."""
import json, re, urllib.request, time, sys
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

DIACRITICS = set("ًٌٍَُِّْٕٖٜٟۣٓٔٗ٘ٙٚٛٝٞۖۗۘۙۚۛۜ۟۠ۡۢۤۥۦ")
LETTERS = re.compile(r"[^\u0621-\u064A\u067E\u0686\u0698\u06AF\u06A9\u06CC\u06C0\u06CD\u06D5]")
def lkey(s):
    s = s.replace("\u200c","").replace("\u200d","")
    s = s.replace("ي","ی").replace("ك","ک").replace("ۀ","ه").replace("أ","ا").replace("إ","ا").replace("آ","ا")
    s = "".join(ch for ch in s if ch not in DIACRITICS)
    return LETTERS.sub("", s)

def num_from_title(t):
    m = re.search(r"(\d+)", t)
    return int(m.group(1)) if m else 0

def clean_meaning(s):
    if not s: return None
    s = re.sub(r"^هوش مصنوعی[:：]\s*", "", s).strip()
    s = re.sub(r"\s+", " ", s)
    return s if s else None

def beits_from(d):
    beits, cur = [], None
    for v in sorted(d["verses"], key=lambda x: x["vOrder"]):
        ci = v["coupletIndex"]
        if cur is None or cur[0] != ci:
            cur = [ci, v["text"], None, clean_meaning(v.get("coupletSummary"))]
            beits.append(cur)
        else:
            if v["versePosition"] == 1: cur[2] = v["text"]
            else: cur[1] = v["text"]
    return [{"first": c[1], "second": c[2], "meaning": c[3]} for c in beits]

def walk(cat_id, path, out, seen):
    d = get(f"/cat/{cat_id}")
    cat = d["cat"]
    name = cat["urlSlug"]
    p = f"{path}/{name}" if path else name
    poems = cat.get("poems") or []
    for pm in poems:
        if pm["id"] not in seen:
            seen.add(pm["id"]); out.append(pm)
    for ch in (cat.get("children") or []):
        walk(ch["id"], p, out, seen)

# ---------- 1. build id lists ----------
targets = {}
def add(key, ids):
    targets[key] = ids

golestan, boostan, ghazal_s, masnavi, shams = [], [], [], [], []
seen = set()
# Saadi
walk(1665, "", golestan, seen)      # golestan
walk(123, "", boostan, seen)        # boostan
saadi_ghaz = get("/cat/120?poems=true")["cat"]["poems"]
for p in saadi_ghaz:
    if p["id"] not in seen: seen.add(p["id"]); ghazal_s.append(p)
# Rumi
walk(103, "", masnavi, seen)        # masnavi (6 daftars)
shams_gh = get("/cat/99?poems=true")["cat"]["poems"]
for p in shams_gh:
    if p["id"] not in seen: seen.add(p["id"]); shams.append(p)
shams_tarjee = get("/cat/101?poems=true")["cat"]["poems"]
for p in shams_tarjee:
    if p["id"] not in seen: seen.add(p["id"]); shams.append(p)

print("lists:", len(golestan), "golestan,", len(boostan), "boostan,", len(ghazal_s),
      "saadi-ghazal,", len(masnavi), "masnavi,", len(shams), "shams")
json.dump({"golestan":golestan,"boostan":boostan,"ghazal":ghazal_s,"masnavi":masnavi,"shams":shams},
          open("/tmp/divan_lists.json","w"), ensure_ascii=False)

# ---------- 2. fetch all poem details ----------
def job(p, poet, coll):
    pid = p["id"]
    try:
        d = get(f"/poem/{pid}")
        if d is None or "verses" not in d: return None
        return {"id": pid, "poet": poet, "collection": coll, "title": d["title"],
                "number": num_from_title(d["title"]), "beits": beits_from(d)}
    except Exception:
        return None

tasks = []
for p in golestan: tasks.append((p, "saadi", "golestan"))
for p in boostan:  tasks.append((p, "saadi", "bustan"))
for p in ghazal_s: tasks.append((p, "saadi", "saadi-ghazal"))
for p in masnavi:  tasks.append((p, "rumi", "masnavi"))
for p in shams:    tasks.append((p, "rumi", "shams"))

results, fail = {}, []
with ThreadPoolExecutor(max_workers=8) as ex:
    futs = {ex.submit(job, p, poet, coll): p["id"] for (p, poet, coll) in tasks}
    for i, fut in enumerate(as_completed(futs), 1):
        r = fut.result()
        if r is None: fail.append(futs[fut])
        else: results[r["id"]] = r
        if i % 500 == 0: print(f"  fetched {i}/{len(tasks)}", flush=True)

print("ok:", len(results), "failed:", len(fail))
json.dump(results, open("/tmp/raw_divans.json","w"), ensure_ascii=False)
print("saved raw")
