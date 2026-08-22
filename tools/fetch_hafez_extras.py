# -*- coding: utf-8 -*-
"""
تکمیلِ دیوان حافظ: افزودنِ بخش‌های ناقص از گنجور
- قطعات (34) → collection=qete
- رباعیات (42) → collection=robaee
- قصاید (3) → collection=qaside
- اشعار منتسب (118) → collection=attributed

هر شعر: متن از گنجور، معنیِ بیت از coupletSummary، تفسیرِ یکتا تولیدشده.
"""
import gzip, json, re, urllib.request, time, unicodedata
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE = "https://api.ganjoor.net/api/ganjoor"
CATS = [
    (25, "qete", "قطعات"),
    (26, "hafez-robaee", "رباعیات"),
    (27, "qaside", "قصاید"),
    (674, "attributed", "اشعار منتسب"),
]

def get(path, retries=5):
    req = urllib.request.Request(BASE + path, headers={"User-Agent": "falhafez-corpus/1.0"})
    for a in range(retries):
        try:
            with urllib.request.urlopen(req, timeout=40) as r:
                return json.loads(r.read().decode("utf-8"))
        except Exception:
            if a == retries - 1:
                raise
            time.sleep(1.0 + a)
    return None

def clean(s):
    if not s:
        return None
    s = re.sub(r"^هوش مصنوعی[:：]\s*", "", s).strip()
    s = re.sub(r"\s+", " ", s)
    s = s.replace("\u200c", " ")
    s = re.sub(r"\s+", " ", s).strip()
    return s if s else None

THEME_MAP = [
    ("love", ["عشق", "یار", "معشوق", "زلف", "وصال", "دلبر", "غمزه"]),
    ("hope", ["امید", "مژده", "سحر", "بهار", "گل", "نوروز", "بشارت"]),
    ("patience", ["صبر", "صبور", "شکیب"]),
    ("joy", ["شادی", "ساقی", "باده", "عشرت", "می‌خور", "طرب", "مطرب"]),
    ("wisdom", ["پند", "حکمت", "عقل", "دانش", "درس", "تجربه"]),
    ("faith", ["خدا", "توکل", "توسل", "دعا"]),
    ("effort", ["کوشش", "تلاش", "همت", "سعی"]),
    ("travel", ["سفر", "راه", "منزل", "کوچ"]),
]

def classify(text):
    for tag, kws in THEME_MAP:
        for kw in kws:
            if kw in text:
                return tag
    return "wisdom"

FORTUNES = {
    "love": "این فال از دلِ عاشقانه‌ات خبر می‌دهد: دلت را پنهان مکن؛ آنچه با خلوص پیشکش کنی، دیر یا زود به تو بازمی‌گردد.",
    "hope": "این فال بشارت می‌دهد: شبِ نگرانی‌ات به سحر می‌رسد؛ ناامید مباش، که روشنی در راه است.",
    "patience": "این فال از صبوری سخن می‌گوید: درختِ صبور میوه می‌دهد؛ سختی‌هایت موقت‌اند و می‌گذرند.",
    "joy": "این فال دعوتی است به شادی: غم را زمین بگذار؛ زندگی برای لذت‌های کوچکِ امروز است.",
    "wisdom": "فالِ تو دعوتی است به دانایی: پیش از هر گام، اندیشه کن؛ که تدبیر، چراغِ راه است.",
    "faith": "این فال از ایمان و توکل می‌گوید: کار را به خدا بسپار، اما پارو زدن را رها مکن.",
    "effort": "این فال از کوشش می‌گوید: هیچ گنجی بی‌هنر به دست نمی‌آید؛ کارِ امروز، فردای تو را می‌سازد.",
    "travel": "این فال از سفر می‌گوید: راهی پیش روی توست؛ توشه بردار و با توکل گام بردار.",
}

def make_tafsir(coll_fa, verses, pid):
    if verses:
        v = verses[0]
        txt = v["first"] + (("؛ " + v["second"]) if v.get("second") else "")
        anchor = txt.strip()
    else:
        anchor = "شعر"
    theme = verses[0].get("_theme", "wisdom")
    fortune = FORTUNES.get(theme, FORTUNES["wisdom"])
    if coll_fa == "قصاید":
        opening = "حافظ در این قصیده می‌فرماید:"
    elif coll_fa == "قطعات":
        opening = "حافظ در این قطعه می‌فرماید:"
    elif coll_fa == "رباعیات":
        opening = "حافظ در این رباعی می‌فرماید:"
    else:
        opening = "حافظ در این ابیات می‌فرماید:"
    return f"{opening} «{anchor}» {fortune}"

def fetch_poem(pid):
    d = get(f"/poem/{pid}")
    if d is None:
        return pid, None
    couplets = {}
    for v in d.get("verses", []):
        ci = v.get("coupletIndex")
        pos = v.get("versePosition")
        txt = clean(v.get("text"))
        summ = clean(v.get("coupletSummary"))
        if ci is None:
            continue
        couplets.setdefault(ci, {})[pos] = txt
        # معنی فقط روی مصرع اول می‌آید؛ None را روی آن بازنویسی نکن.
        if summ is not None:
            couplets[ci]["_summary"] = summ
    verses = []
    for ci in sorted(couplets.keys()):
        first = couplets[ci].get(0, "")
        second = couplets[ci].get(1)
        if not first:
            continue
        verses.append({
            "first": first,
            "second": second if second else None,
            "meaning": couplets[ci].get("_summary"),
        })
    if not verses:
        return pid, None
    return pid, verses

def main():
    data = json.loads(gzip.open("app/src/main/assets/corpus/hafez.dat", "rt", encoding="utf-8").read())
    existing_ids = {p["id"] for p in data}
    added = 0
    for cat_id, coll_key, coll_fa in CATS:
        cat = get(f"/cat/{cat_id}?poems=true")
        poems = (cat["cat"].get("poems") or [])
        print(f"== {coll_fa}: {len(poems)} poems")
        results = {}
        with ThreadPoolExecutor(max_workers=8) as ex:
            futs = {ex.submit(fetch_poem, p["id"]): p for p in poems}
            for fut in as_completed(futs):
                pid, verses = fut.result()
                if verses:
                    results[pid] = verses
        # order by title number
        ordered = sorted(poems, key=lambda p: p["id"])
        for i, p in enumerate(ordered, 1):
            pid = p["id"]
            if pid in existing_ids:
                continue
            verses = results.get(pid)
            if not verses:
                continue
            theme = classify(verses[0]["first"])
            for v in verses:
                v["_theme"] = theme
            data.append({
                "id": pid,
                "poet": "hafez",
                "collection": coll_key,
                "number": i,
                "themeTag": theme,
                "tafsir": make_tafsir(coll_fa, verses, pid),
                "verses": [{k: v[k] for k in ("first", "second", "meaning")} for v in verses],
            })
            added += 1
    data.sort(key=lambda p: (p["collection"], p["number"]))
    # keep ghazals first? original order was ghazal only; keep stable: ghazal then extras
    def order_key(p):
        return {"ghazal": 0, "qete": 1, "hafez-robaee": 2, "qaside": 3, "attributed": 4}[p["collection"]]
    data.sort(key=lambda p: (order_key(p), p["number"]))
    gzip.open("app/src/main/assets/corpus/hafez.dat", "wt", encoding="utf-8").write(
        json.dumps(data, ensure_ascii=False, separators=(",", ":"))
    )
    print(f"\nadded {added} poems; total hafez now {len(data)}")
    ids = [p["id"] for p in data]
    print("unique ids:", len(ids) == len(set(ids)))

if __name__ == "__main__":
    main()
