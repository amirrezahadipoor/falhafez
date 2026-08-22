# -*- coding: utf-8 -*-
"""
اعتبارسنجیِ محتوا — دروازهٔ کیفیت پیش از انتشار

این اسکریپت در CI اجرا می‌شود و اگر هر یک از سنجه‌های زیر شکسته شود،
با کدِ خروجیِ ۱ برمی‌گردد تا بیلد رد شود.

سنجه‌ها
-------
۱. هیچ تفسیرِ قالبی نمانده باشد («پیامِ آن این است که»).
۲. تنوع: هیچ متنِ تفسیری بیش از [MAX_DUP] بار تکرار نشده باشد.
   (validate_corpus.py قدیمی فقط تکرارِ **کاملِ** رکورد را می‌گرفت و این
    مشکل را نمی‌دید: ۸٬۴۶۵ شعر تنها ۸۷۲ تفسیرِ یکتا داشتند.)
۳. هیچ شرحِ بیت یا تفسیری وسطِ جمله بریده نشده باشد.
۴. نویزِ ویرایشی نمانده باشد: «منبع:»، قلابِ باز، پرانتزِ نامتوازن.
۵. انتسابِ شاعر درست باشد (بخشِ «جهان» نباید به سعدی نسبت داده شود).
۶. شناسه‌ها یکتا باشند و هیچ تفسیری خالی نباشد.
۷. توزیعِ تم‌ها فاجعه‌بار نامتوازن نباشد.

اجرا:  python3 tools/validate_content.py
"""

import gzip
import json
import os
import re
import sys
from collections import Counter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CORPUS = os.path.join(ROOT, "app", "src", "main", "assets", "corpus")
FILES = ["hafez", "khayyam", "saadi", "rumi", "stories"]

MAX_DUP = 5           # سقفِ تکرارِ یک متنِ تفسیری
MIN_TAFSIR_LEN = 60
TERMINALS = ".!؟?…"

failures = []
warnings = []


def fail(msg):
    failures.append(msg)


def warn(msg):
    warnings.append(msg)


def load(name):
    with gzip.open(os.path.join(CORPUS, name + ".dat"), "rt", encoding="utf-8") as f:
        return json.load(f)


def main():
    all_poems = []
    for name in FILES:
        poems = load(name)
        for p in poems:
            p["_file"] = name
        all_poems.extend(poems)

    print(f"مجموع: {len(all_poems)} شعر")

    # ۱) تفسیرِ قالبی
    templated = [p for p in all_poems if "پیامِ آن این است که" in (p.get("tafsir") or "")]
    if templated:
        fail(f"{len(templated)} تفسیرِ قالبی باقی مانده است")
    else:
        print("✓ هیچ تفسیرِ قالبی نمانده")

    # ۲) تنوعِ تفسیر
    bodies = Counter((p.get("tafsir") or "").strip() for p in all_poems)
    top, n = bodies.most_common(1)[0]
    print(f"✓ تفسیرِ یکتا: {len(bodies)} از {len(all_poems)}  (پرتکرارترین ×{n})")
    if n > MAX_DUP:
        fail(f"یک متنِ تفسیری {n} بار تکرار شده (سقف {MAX_DUP}): {top[:80]}…")

    # ۳) تفسیرِ خالی/کوتاه + پایان‌بندی
    empty = [p for p in all_poems if not (p.get("tafsir") or "").strip()]
    if empty:
        fail(f"{len(empty)} تفسیرِ خالی")
    short = [p for p in all_poems if 0 < len((p.get("tafsir") or "").strip()) < MIN_TAFSIR_LEN]
    if short:
        warn(f"{len(short)} تفسیرِ کوتاه‌تر از {MIN_TAFSIR_LEN} نویسه")
    unfinished = [p for p in all_poems if (p.get("tafsir") or "").strip()[-1:] not in TERMINALS]
    if unfinished:
        fail(f"{len(unfinished)} تفسیر بدونِ پایان‌بندیِ درست")
    else:
        print("✓ همهٔ تفسیرها پایان‌بندیِ درست دارند")

    # ۴) نویزِ ویرایشی
    noise = Counter()
    for p in all_poems:
        t = p.get("tafsir") or ""
        if re.search(r"منبع\s*[:：]", t):
            noise["استنادِ داخلِ متن"] += 1
        if "[" in t or "]" in t:
            noise["قلاب"] += 1
        if t.count("(") != t.count(")"):
            noise["پرانتزِ نامتوازن"] += 1
        if t.count("«") != t.count("»"):
            noise["گیومهٔ نامتوازن"] += 1
    if noise:
        fail(f"نویز در تفسیر: {dict(noise)}")
    else:
        print("✓ تفسیرها بدونِ نویزِ ویرایشی")

    # ۵) شرحِ ابیات
    vn = Counter()
    total_v = with_m = 0
    for p in all_poems:
        for v in p.get("verses", []):
            total_v += 1
            m = (v.get("meaning") or "").strip()
            if not m:
                continue
            with_m += 1
            if m[-1] not in TERMINALS:
                vn["بی‌پایان‌بندی"] += 1
            if re.search(r"منبع\s*[:：]", m):
                vn["استناد"] += 1
            if "[" in m or "]" in m:
                vn["قلاب"] += 1
            if m.count("(") != m.count(")"):
                vn["پرانتزِ نامتوازن"] += 1
    print(f"✓ ابیات: {total_v} ({with_m} با شرح = {with_m / max(total_v, 1):.0%})")
    if vn:
        fail(f"نویز در شرحِ ابیات: {dict(vn)}")
    else:
        print("✓ شرحِ ابیات پاکیزه")

    # ۶) انتسابِ شاعر
    wrong = [p for p in all_poems if p.get("collection") == "stories" and p.get("poet") != "world"]
    if wrong:
        fail(f"{len(wrong)} متنِ بخشِ «جهان» هنوز به شاعرِ فارسی نسبت داده شده")
    else:
        print("✓ انتسابِ بخشِ «جهان» درست است")

    # ۷) شناسهٔ تکراری
    ids = Counter(p["id"] for p in all_poems)
    dups = [i for i, c in ids.items() if c > 1]
    if dups:
        fail(f"{len(dups)} شناسهٔ تکراری: {dups[:5]}")
    else:
        print("✓ همهٔ شناسه‌ها یکتا")

    # ۸) توزیعِ تم
    themes = Counter(p.get("themeTag") for p in all_poems)
    top_theme, top_n = themes.most_common(1)[0]
    share = top_n / len(all_poems)
    print(f"✓ تم‌ها: {len(themes)} گونه، پرتکرارترین «{top_theme}» {share:.0%}")
    if share > 0.45:
        warn(f"تمِ «{top_theme}» {share:.0%} از کل را گرفته — توزیع نامتوازن است")
    for t, c in themes.items():
        if c < 10:
            warn(f"تمِ «{t}» فقط {c} شعر دارد")

    # ── نتیجه ──
    print()
    for w in warnings:
        print("⚠  " + w)
    if failures:
        print()
        for f in failures:
            print("✗  " + f)
        print(f"\nشکست: {len(failures)} ایراد")
        return 1
    print("\n✅ همهٔ سنجه‌های محتوا سبز است.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
