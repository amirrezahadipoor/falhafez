# -*- coding: utf-8 -*-
"""Fetch Rumi's Rubaiyat (cat 102) + Saadi's remaining collections (robaees/ghetes/molhaghat)
and merge them into the offline corpus (preserving existing hand tafsirs, generating the rest)."""
import json, re, urllib.request, time
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

def clean_meaning(s):
    if not s: return None
    s = re.sub(r"^هوش مصنوعی[:：]\s*", "", s).strip()
    s = re.sub(r"\s+", " ", s)
    return s if s else None

def num_from_title(t):
    m = re.search(r"(\d+)", t)
    return int(m.group(1)) if m else 0

def beits_from(d, keep_meaning):
    beits, cur = [], None
    for v in sorted(d["verses"], key=lambda x: x["vOrder"]):
        ci = v["coupletIndex"]
        if cur is None or cur[0] != ci:
            cur = [ci, v["text"], None, clean_meaning(v.get("coupletSummary")) if keep_meaning else None]
            beits.append(cur)
        else:
            if v["versePosition"] == 1: cur[2] = v["text"]
            else: cur[1] = v["text"]
    return [{"first": c[1], "second": c[2], "meaning": c[3]} for c in beits]

def cat_poems(cat_id):
    d = get(f"/cat/{cat_id}?poems=true")
    return d["cat"].get("poems") or []

targets = [
    ("rumi",  "robaee",        102, False),
    ("saadi", "saadi-robaee",  122, True),
    ("saadi", "saadi-ghete",   144, True),
    ("saadi", "saadi-molhaghat", 145, True),
]

all_tasks = []
for poet, coll, cat_id, keep in targets:
    for p in cat_poems(cat_id):
        all_tasks.append((p, poet, coll, keep))
print("tasks:", len(all_tasks))

def job(t):
    p, poet, coll, keep = t
    try:
        d = get(f"/poem/{p['id']}")
        if d is None or "verses" not in d: return None
        return {"id": p["id"], "poet": poet, "collection": coll, "title": d["title"],
                "number": num_from_title(d["title"]), "beits": beits_from(d, keep)}
    except Exception:
        return None

results, fail = {}, []
with ThreadPoolExecutor(max_workers=8) as ex:
    futs = {ex.submit(job, t): t[0]["id"] for t in all_tasks}
    for i, fut in enumerate(as_completed(futs), 1):
        r = fut.result()
        if r is None: fail.append(futs[fut])
        else: results[r["id"]] = r
        if i % 500 == 0: print("  fetched", i, flush=True)
print("ok:", len(results), "failed:", len(fail))
json.dump(results, open("/tmp/remaining_raw.json", "w"), ensure_ascii=False)
