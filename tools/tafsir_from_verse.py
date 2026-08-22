# -*- coding: utf-8 -*-
"""
تفسیر برای شعرهایی که **هیچ شرحِ بیتی ندارند**.

مسئله
-----
پس از اجرای `tafsir_deepen.py`، ۶۱ شعر در پیکرهٔ حافظ روی متنِ قالبیِ قدیمی
باقی ماندند. همه‌شان در مجموعهٔ `attributed` (اشعارِ منتسب) هستند و علتش روشن
است: `tafsir_deepen` بدنهٔ تفسیر را از شرحِ ابیات می‌سازد و این ۶۱ شعر اصلاً
شرحِ بیت ندارند، پس تابع `None` برمی‌گرداند و متنِ قبلی دست‌نخورده می‌مانَد.

متنِ قبلی سه ایراد داشت:

    «حافظ در این ابیات از دانایی و بینش می‌گوید. این برگزیده، ۵۶مینِ اشعارِ
     منتسب است. فالِ تو دعوتی است به دانایی: پیش از هر گام اندیشه کن…»

۱. جملهٔ اولش برای هر ۶۱ شعر یکسان بود، فارغ از اینکه شعر دربارهٔ چه باشد.
۲. «۵۶مینِ اشعارِ منتسب» اطلاعاتِ کاتالوگی است، نه تفسیر؛ به دردِ کسی که فال
   گرفته نمی‌خورد.
۳. پندِ پایانی هم از همان قالب می‌آمد و ۴۶ بار تکرار شده بود.

این شعرها گرچه از قرعهٔ فال بیرون‌اند (`attributed` در پرس‌وجوهای قرعه حذف
می‌شود)، اما در بخشِ **دیوان** خوانده می‌شوند و تفسیرشان دیده می‌شود.

راهکار
------
وقتی شرحِ آماده نداریم، تفسیر را از **خودِ بیت** می‌سازیم:

۱. واژه‌های کلیدیِ شعر با فرهنگِ نقش‌مایه‌های موجود تحلیل می‌شود تا موضوعِ
   واقعیِ شعر (نه تمِ برچسب‌خورده) به دست آید.
۲. بیتی که بیشترین بارِ پندی دارد انتخاب و **عیناً نقل** می‌شود — نقلِ بیت،
   امانت‌دارترین کاری است که بدونِ شرح می‌توان کرد.
۳. یک جملهٔ راهنما بر پایهٔ تمِ تشخیص‌داده‌شده افزوده می‌شود.

هیچ ادعای معناییِ تازه‌ای دربارهٔ بیت ساخته نمی‌شود؛ متن فقط بیت را پیشِ چشم
می‌گذارد و پیوندش با حالِ خواننده را می‌گوید.

اجرا:  python3 tools/tafsir_from_verse.py [--dry]
"""

import gzip
import json
import os
import random
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from tafsir_engine import (  # noqa: E402
    CORPUS,
    POET_FA,
    FORM_FA,
    detect_motifs,
    detect_theme,
    strip_diacritics,
    _MOTIF_LABEL,
    _SIGNAL,
)
from tafsir_deepen import GUIDE, TERMINALS, terminate  # noqa: E402

FILES = ["hafez", "khayyam", "saadi", "rumi"]

# نشانه‌های متنِ قالبیِ قدیمی که باید جایگزین شود
OLD_MARKERS = (
    "فالِ تو دعوتی است به دانایی",
    "حافظ در این ابیات از دانایی و بینش",
    "این برگزیده،",
)

OPEN_MOTIF = [
    "{p} در این {f} {m} سخن می‌گوید",
    "{p} این {f} را {m} بسته است",
    "در این {f}، {p} {m} پرده برمی‌دارد",
]

OPEN_PLAIN = [
    "{p} در این {f} بی‌پرده حرفش را می‌زند",
    "این {f} از {p} لحنی صریح دارد",
    "{p} در این {f} سرراست سخن می‌گوید",
]

# پیوندِ نقلِ بیت به حالِ خواننده
QUOTE_LEAD = [
    "بیتی که بیش از همه با پرسشِ تو کار دارد این است",
    "گرانیگاهِ این سروده اینجاست",
    "این بیت را دو بار بخوان",
    "سنگینیِ این {f} روی این بیت است",
    "اگر یک بیت را باید نگه داری، همین است",
]

CLOSE_LEAD = [
    "برای نیّتِ تو یعنی",
    "در کارِ تو این‌طور خوانده می‌شود",
    "برگردانش به حالِ امروزِ تو این است",
    "پیامش برای تو روشن است",
    "این را در وضعِ خودت بگذار",
]


def is_old(tafsir):
    return any(m in (tafsir or "") for m in OLD_MARKERS)


def pick_verse(verses):
    """
    بیتِ پیام‌دار را برمی‌گرداند.

    چون شرحی در کار نیست، امتیازدهی فقط روی خودِ بیت انجام می‌شود:
    نشانه‌های پندی (امر، شرط، «هر که»، «عاقبت»…) وزنِ اصلی را دارند و
    مطلع کمی عقب رانده می‌شود، چون معمولاً تغزّلی است نه پندآمیز.
    """
    best, best_score = None, -1.0
    n = len(verses)
    for i, v in enumerate(verses):
        first = (v.get("first") or "").strip()
        second = (v.get("second") or "").strip()
        if not first or not second:
            continue
        text = strip_diacritics(first + " " + second)
        score = 2.4 * len(_SIGNAL.findall(text))
        score += min(len(text), 120) / 90.0
        if 0 < i < n - 1:
            score += 1.0
        if i == n - 1 and n > 2:
            score += 0.8
        if i == 0:
            score -= 1.0
        if score > best_score:
            best, best_score = v, score
    if best is None:
        best = next(
            (v for v in verses if (v.get("first") or "").strip() and (v.get("second") or "").strip()),
            None,
        )
    return best


def build(poem, rnd):
    verses = poem.get("verses") or []
    v = pick_verse(verses)
    if v is None:
        return None

    poet = POET_FA.get(poem.get("poet"), "شاعر")
    form = FORM_FA.get(poem.get("collection"), "سروده")

    text = " ".join(
        strip_diacritics((x.get("first") or "") + " " + (x.get("second") or ""))
        for x in verses
    )
    theme = detect_theme(text, poem.get("themeTag") or "general")
    motifs = detect_motifs(text, limit=1)
    motif = _MOTIF_LABEL.get(motifs[0]) if motifs else None

    if motif:
        opener = rnd.choice(OPEN_MOTIF).format(p=poet, f=form, m=motif)
    else:
        opener = rnd.choice(OPEN_PLAIN).format(p=poet, f=form)

    first = re.sub(r"\s+", " ", (v.get("first") or "").strip())
    second = re.sub(r"\s+", " ", (v.get("second") or "").strip())
    quote = f"«{first} / {second}»"

    lead = rnd.choice(QUOTE_LEAD).format(f=form)
    guide = rnd.choice(GUIDE.get(theme, GUIDE["general"]))
    close = rnd.choice(CLOSE_LEAD)

    out = f"{terminate(opener)} {lead}: {quote} {close}: {guide}"
    out = re.sub(r"\s+", " ", out).strip()
    if out and out[-1] not in TERMINALS:
        out += "."
    return out


def main():
    dry = "--dry" in sys.argv
    fixed = 0
    for name in FILES:
        path = os.path.join(CORPUS, name + ".dat")
        with gzip.open(path, "rt", encoding="utf-8") as f:
            poems = json.load(f)
        touched = 0
        for p in poems:
            if not is_old(p.get("tafsir")):
                continue
            rnd = random.Random(p["id"] * 104729)
            new = build(p, rnd)
            if new:
                p["tafsir"] = new
                touched += 1
        fixed += touched
        if touched and not dry:
            with gzip.open(path, "wt", encoding="utf-8") as f:
                json.dump(poems, f, ensure_ascii=False)
        print(f"  {name}: {touched} بازنویسی")
    print(f"{'(آزمایشی) ' if dry else ''}مجموع: {fixed}")


if __name__ == "__main__":
    main()
