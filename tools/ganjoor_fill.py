# -*- coding: utf-8 -*-
"""
پرکردنِ شکافِ شرحِ ابیات از گنجور.

پس‌زمینه
--------
شناسهٔ هر شعر در پیکرهٔ ما **همان شناسهٔ گنجور** است (بررسی شد: `id=2130` در
هر دو «حافظ » غزلیات » غزل شمارهٔ ۱» است). پس می‌شود مستقیم به
`api.ganjoor.net/api/ganjoor/poem/<id>` زد و داده‌های تکمیلی گرفت.

۲۴۱ بیت در پیکره شرح ندارند (۲۲۸ تای آن‌ها در اشعارِ منتسبِ حافظ). این
ابزار می‌کوشد آن‌ها را از `coupletSummary` گنجور پر کند.

چرا فقط شرحِ بیت و نه خلاصهٔ شعر؟
----------------------------------
گنجور دو میدانِ توضیحی دارد:

* `coupletSummary` — شرحِ تک‌بیت، عمدتاً کارِ انسانی و دقیق.
* `poemSummary` — خلاصهٔ کلِ شعر.

نمونه‌گیری از ۱۲۰ شعر نشان داد بخشِ بزرگی از `poemSummary`ها خودشان
ماشینی‌اند و با عبارتِ «هوش مصنوعی:» شروع می‌شوند:

    حافظ  انسانی ۵ / ماشینی ۱۹      سعدی   انسانی ۴ / ماشینی ۲۶
    خیام  انسانی ۱۱ / ماشینی ۱۹     مولانا انسانی ۱ / ماشینی ۲۹

جایگزین کردنِ محتوای فعلی با متنِ ماشینیِ یک سرویسِ دیگر، کیفیت را بالا
نمی‌برد و فقط منبعِ خطا را عوض می‌کند. بنابراین:

* هر متنی که با «هوش مصنوعی» شروع شود **رد می‌شود**.
* فقط بیت‌هایی که *هیچ* شرحی ندارند پر می‌شوند؛ شرحِ موجود هرگز بازنویسی
  نمی‌شود.

تطبیقِ بیت‌ها
------------
گنجور هر مصراع را جداگانه نگه می‌دارد (`versePosition` صفر=مصراعِ اول،
یک=مصراعِ دوم) و `coupletSummary` روی مصراعِ اولِ هر بیت می‌نشیند. تطبیق با
مقایسهٔ متنِ نرمال‌شدهٔ مصراع انجام می‌شود، نه با ترتیب — چون ترتیبِ ما و
گنجور همیشه یکی نیست (بیت‌های الحاقی، اختلافِ نسخه).

اجرا:  python3 tools/ganjoor_fill.py [--dry] [--limit N]
"""

import concurrent.futures as cf
import gzip
import json
import os
import re
import sys
import time
import urllib.error
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CORPUS = os.path.join(ROOT, "app", "src", "main", "assets", "corpus")
FILES = ["hafez", "saadi", "rumi", "khayyam"]

API = "https://api.ganjoor.net/api/ganjoor/poem/{}"
TIMEOUT = 25
WORKERS = 6          # محافظه‌کارانه، تا به سرویس فشار نیاید
RETRIES = 2

AI_MARK = "هوش مصنوعی"
MIN_LEN = 25

ZWNJ = "\u200c"
_DIAC = re.compile(r"[\u064B-\u0652\u0670\u0640]")
_NONWORD = re.compile(r"[^\u0600-\u06FF\s]")


def norm(s):
    """نرمال‌سازی برای تطبیقِ مصراع: بی‌اعراب، بی‌نقطه‌گذاری، تک‌فاصله."""
    s = (s or "").replace(ZWNJ, "")
    s = _DIAC.sub("", s)
    s = s.replace("ي", "ی").replace("ك", "ک").replace("ۀ", "ه").replace("أ", "ا")
    s = s.replace("إ", "ا").replace("آ", "ا").replace("ؤ", "و")
    s = _NONWORD.sub(" ", s)
    return " ".join(s.split())


def fetch(pid):
    for attempt in range(RETRIES + 1):
        try:
            req = urllib.request.Request(
                API.format(pid), headers={"User-Agent": "falhafez-content/1.0"}
            )
            with urllib.request.urlopen(req, timeout=TIMEOUT) as r:
                return json.loads(r.read().decode("utf-8"))
        except (urllib.error.URLError, TimeoutError, json.JSONDecodeError):
            if attempt == RETRIES:
                return None
            time.sleep(1.5 * (attempt + 1))
    return None


def usable(text):
    """آیا این متن ارزشِ نشستن در پیکره را دارد؟"""
    t = (text or "").strip()
    if len(t) < MIN_LEN:
        return False
    if t.startswith(AI_MARK) or t.lstrip("«\"' ").startswith(AI_MARK):
        return False
    return True


def couplet_map(data):
    """
    نگاشتِ «متنِ نرمال‌شدهٔ مصراعِ اول» → شرحِ بیت.

    `coupletSummary` روی مصراعِ اول می‌نشیند؛ مصراعِ دوم را هم کلید می‌کنیم
    تا اگر تطبیقِ اول نگرفت، از راهِ دوم پیدا شود.
    """
    out = {}
    verses = data.get("verses") or []
    for i, v in enumerate(verses):
        cs = (v.get("coupletSummary") or "").strip()
        if not usable(cs):
            continue
        key1 = norm(v.get("text"))
        if key1:
            out.setdefault(key1, cs)
        if i + 1 < len(verses) and verses[i + 1].get("versePosition") == 1:
            key2 = norm(verses[i + 1].get("text"))
            if key2:
                out.setdefault(key2, cs)
    return out


def gaps_in(poem):
    return [v for v in poem.get("verses") or [] if not (v.get("meaning") or "").strip()]


def main():
    dry = "--dry" in sys.argv
    limit = None
    if "--limit" in sys.argv:
        limit = int(sys.argv[sys.argv.index("--limit") + 1])

    targets = []
    loaded = {}
    for name in FILES:
        with gzip.open(os.path.join(CORPUS, name + ".dat"), "rt", encoding="utf-8") as f:
            poems = json.load(f)
        loaded[name] = poems
        for p in poems:
            if gaps_in(p):
                targets.append((name, p))

    if limit:
        targets = targets[:limit]
    print(f"شعرهای دارای شکاف: {len(targets)}")

    results = {}
    with cf.ThreadPoolExecutor(WORKERS) as ex:
        futs = {ex.submit(fetch, p["id"]): (name, p) for name, p in targets}
        done = 0
        for fut in cf.as_completed(futs):
            name, p = futs[fut]
            results[p["id"]] = fut.result()
            done += 1
            if done % 20 == 0:
                print(f"  دریافت {done}/{len(targets)}")

    filled = skipped_ai = unmatched = no_data = 0
    for name, p in targets:
        data = results.get(p["id"])
        if not data:
            no_data += 1
            continue
        cmap = couplet_map(data)
        raw_any = any(
            (v.get("coupletSummary") or "").strip() for v in (data.get("verses") or [])
        )
        if not cmap and raw_any:
            skipped_ai += 1
        for v in gaps_in(p):
            key = norm(v.get("first")) or norm(v.get("second"))
            hit = cmap.get(key)
            if not hit and v.get("second"):
                hit = cmap.get(norm(v.get("second")))
            if hit:
                v["meaning"] = " ".join(hit.split())
                filled += 1
            else:
                unmatched += 1

    print(f"\nپرشده: {filled}")
    print(f"بدونِ تطبیقِ مصراع: {unmatched}")
    print(f"شعرهایی که شرحشان ماشینی بود و رد شد: {skipped_ai}")
    print(f"بدونِ پاسخ از سرویس: {no_data}")

    if not dry and filled:
        for name, poems in loaded.items():
            with gzip.open(os.path.join(CORPUS, name + ".dat"), "wt", encoding="utf-8") as f:
                json.dump(poems, f, ensure_ascii=False)
        print("پیکره ذخیره شد.")
    elif dry:
        print("(آزمایشی — چیزی ذخیره نشد)")


if __name__ == "__main__":
    main()
