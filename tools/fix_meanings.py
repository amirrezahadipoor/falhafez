# -*- coding: utf-8 -*-
"""
پاک‌سازیِ «معنیِ بیت‌ها» — «فال حافظ»

مسائلی که حل می‌کند
--------------------
۱. **شرحِ بریده (۴٬۵۳۹ مورد):** شرح‌ها وسطِ جمله تمام می‌شدند («…و چقدر تناسب شگفتی
   دارد با معنای مصراع»). ریشه: بُرشِ کورکورانه روی تعدادِ نویسه هنگام ساختِ corpus.
   اینجا هر شرح در **مرزِ جمله** بسته می‌شود.

۲. **استنادِ داخلِ متن (۲٬۱۳۴ مورد):** «منبع: شرح غزل‌های سعدی» انتهای شرح می‌آمد.
   جای استناد، صفحهٔ «دربارهٔ منابع» است، نه وسطِ فال.

۳. **قلاب و پرانتزِ نیمه‌باز:** «[ نمودن = نشان دادن» بدونِ بسته‌شدن.

۴. **یادداشت‌های واژه‌نامه‌ای:** «[ می روشن = شراب صاف ]» — برای شرحِ دانشگاهی خوب
   است، برای فال حواس‌پرت‌کن. به انتهای شرح و داخلِ یک جملهٔ روان منتقل می‌شوند
   یا حذف می‌گردند.

اجرا:  python3 tools/fix_meanings.py [--dry-run]
"""

import gzip
import json
import os
import re
import sys
from collections import Counter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CORPUS = os.path.join(ROOT, "app", "src", "main", "assets", "corpus")
FILES = ["hafez", "khayyam", "saadi", "rumi"]

ZWNJ = "\u200c"
TERMINALS = ".!؟?…"


def normalize(s):
    if not s:
        return ""
    s = s.replace("ي", "ی").replace("ك", "ک")
    s = re.sub(r"[ \t]+", " ", s)
    return s.strip()


# ── حذفِ استناد و یادداشت ───────────────────────────────────────────────────

_CITATION = re.compile(
    r"\s*[-–—•]?\s*منبع\s*[:：]\s*(?:شرح|دیوان|کتاب|تفسیر|برگرفته)?[^.!؟\n]*",
)
_NOTE_TAIL = re.compile(r"\s*\(?\s*نکته\s*[۰-۹0-9]*\s*[:：].*$", re.S)
_GLOSS = re.compile(r"\[[^\]]*\]?")           # قلاب — چه بسته چه نیمه‌باز
_EMPTY_PAREN = re.compile(r"\(\s*\)|«\s*»|\[\s*\]")


def balance(s):
    """پرانتز/گیومهٔ نامتوازن را می‌بندد یا پارهٔ ناقص را حذف می‌کند."""
    for op, cl in (("(", ")"), ("«", "»")):
        # بازِ بسته‌نشده → از همان‌جا قیچی
        while s.count(op) > s.count(cl):
            i = s.rfind(op)
            s = s[:i].rstrip(" ،؛:-")
        # بسته بدونِ باز → حذفِ نویسه
        while s.count(cl) > s.count(op):
            s = s.replace(cl, "", 1)
    return s


def cut_at_sentence(s, max_len=260):
    """
    شرح را در مرزِ جمله می‌بندد.
    اگر متن کوتاه‌تر از سقف باشد دست نمی‌خورد؛ فقط پایان‌بندی‌اش اصلاح می‌شود.
    """
    s = s.strip()
    if not s:
        return ""
    if len(s) <= max_len:
        return s

    cuts = [m.end() for m in re.finditer(r"[.!؟?…]", s) if m.end() <= max_len]
    if cuts:
        return s[: cuts[-1]].strip()

    for sep in ("؛", "،", " و ", " که ", " تا "):
        i = s.rfind(sep, 0, max_len)
        if i > max_len * 0.5:
            return s[:i].strip().rstrip("؛،و") + "."

    i = s.rfind(" ", 0, max_len)
    return (s[:i] if i > 0 else s[:max_len]).strip() + "…"


def finish(s):
    """پایان‌بندیِ درست — هیچ شرحی نباید بی‌نقطه یا با ویرگول تمام شود."""
    s = s.strip().rstrip(" ،؛:-—–/\\")
    if not s:
        return ""
    if s[-1] in TERMINALS:
        return s
    return s + "."


def clean(meaning):
    if not meaning:
        return meaning
    s = normalize(meaning)
    s = _CITATION.sub(" ", s)
    s = _NOTE_TAIL.sub(" ", s)
    s = _GLOSS.sub(" ", s)
    s = s.replace("]", " ")
    s = _EMPTY_PAREN.sub(" ", s)
    s = balance(s)
    s = re.sub(r"\s+", " ", s)
    s = re.sub(r"\s+([.،؛:!؟])", r"\1", s)
    s = re.sub(r"([.!؟…])\s*\1+", r"\1", s)
    s = cut_at_sentence(s)
    # بُرشِ جمله می‌تواند پرانتزِ باز را از جفتش جدا کند → دوباره متوازن می‌کنیم.
    s = balance(s)
    s = _EMPTY_PAREN.sub(" ", s)
    s = re.sub(r"\s+", " ", s)
    return finish(s)


def main():
    dry = "--dry-run" in sys.argv
    stats = Counter()

    for name in FILES:
        path = os.path.join(CORPUS, name + ".dat")
        with gzip.open(path, "rt", encoding="utf-8") as f:
            poems = json.load(f)

        changed = 0
        for p in poems:
            for v in p.get("verses", []):
                old = v.get("meaning")
                if not old:
                    continue
                new = clean(old)
                if new != old:
                    v["meaning"] = new
                    changed += 1
        stats[name] = changed
        print(f"{name:9s} شرحِ اصلاح‌شده = {changed}")

        if not dry:
            with gzip.open(path, "wt", encoding="utf-8", compresslevel=9) as f:
                json.dump(poems, f, ensure_ascii=False, separators=(",", ":"))

    print("\nجمع:", sum(stats.values()))

    # ── بازبینی ──
    bad = Counter()
    for name in FILES:
        with gzip.open(os.path.join(CORPUS, name + ".dat"), "rt", encoding="utf-8") as f:
            poems = json.load(f)
        for p in poems:
            for v in p.get("verses", []):
                m = (v.get("meaning") or "").strip()
                if not m:
                    continue
                if m[-1] not in TERMINALS:
                    bad["بی‌پایان‌بندی"] += 1
                if "منبع:" in m:
                    bad["استناد"] += 1
                if "[" in m or "]" in m:
                    bad["قلاب"] += 1
                if m.count("(") != m.count(")"):
                    bad["پرانتزِ نامتوازن"] += 1
    print("باقی‌مانده:", dict(bad) if bad else "هیچ ✓")


if __name__ == "__main__":
    main()
