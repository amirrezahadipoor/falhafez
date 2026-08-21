# -*- coding: utf-8 -*-
"""
Comprehensive content fixer for the Fal Hafez corpus.

Fixes applied (in order):
1. Remove `***` placeholder verses (Ganjoor section separators) — e.g. rumi 4409.
2. Clean AI-label contamination in beit meanings ("AI:", "(تصحیح AI: …)", "تصحیح ترجمه Al :", mojibake "Ãshoory:").
3. Fix known garbled meanings (LLM disclaimers / meta-comments) with clean paraphrases.
4. Fix typos (خافظ -> حافظ, حافط -> حافظ).
5. Remove English / foreign-language words that leaked into meanings (token dictionary + targeted embedded fixes).
6. Re-serialize all corpus files (gzip).

Idempotent: safe to re-run.
"""
import gzip, json, re, sys, collections

BASE = "app/src/main/assets/corpus"
FILES = ["hafez", "khayyam", "saadi", "rumi", "stories"]

# ---------------------------------------------------------------------------
# AI-label cleanup
# ---------------------------------------------------------------------------
AI_LABEL = r"(?:AI|ai|Al|هوش\s*مصنوعی)"

def clean_ai(m: str) -> str:
    if not m:
        return m
    s = m.strip()
    # 1) "AI: A. (تصحیح AI: B)"  /  "[تصحیح AI: B]"  -> prefer corrected B
    pat = re.compile(
        r"^\s*" + AI_LABEL + r"\s*[:：]\s*(.*?)\s*[\(\[（]\s*تصحیح\s*(?:ترجمه\s*)?"
        + AI_LABEL + r"\s*[:：]?\s*(.*?)\s*[\)\]）]\s*$",
        re.S,
    )
    mm = pat.match(s)
    if mm:
        return mm.group(2).strip()
    # 2) leading "تصحیح AI: X"  /  "تصحیح ترجمه Al : X"
    s = re.sub(r"^\s*تصحیح\s*(?:ترجمه\s*)?" + AI_LABEL + r"\s*[:：]\s*", "", s)
    # 3) leading "AI:" / "هوش مصنوعی:"
    s = re.sub(r"^\s*" + AI_LABEL + r"\s*[:：]\s*", "", s)
    return s.strip()

# ---------------------------------------------------------------------------
# Known garbled meanings -> clean paraphrases (keyed by (name, poemId, verseIdx))
# ---------------------------------------------------------------------------
GARBLED = {
    ("saadi", 41402, 11):
        "به سببِ آشنایی و شناختی که از پیش میانِ ما بود، آستینِ او را گرفتم و گفتم.",
    ("rumi", 8116, 7):
        "قصدِ کعبه کن چون هنگامِ حج باشد؛ چون رفتی، مکه را هم خواهی دید. شاعر در اینجا یکی از راهکارهای رسیدن به آگاهی را نیز بیان می‌کند.",
    ("rumi", 5762, 4):
        "از دور زانو می‌زنی و نزدیک نمی‌آیی و زانوی مرا بالینِ خود نمی‌کنی.",
}

# ---------------------------------------------------------------------------
# Exact (substring) replacements — applied BEFORE token dictionary.
# Handles embedded / multi-word cases. Longest first.
# ---------------------------------------------------------------------------
EXACT = [
    ("انسان‌های ن hypocritical", "انسان‌های ریاکار"),
    ("چیزی جز می God", "چیزی جز می"),
    ("به‌گونه‌ای بهawakening و بیداری", "به‌گونه‌ای به بیداری"),
    ("بهThought و احساسات", "به اندیشه و احساسات"),
    ("در حالFragmented است", "در حال پاره‌شدن است"),
    ("معدنFlow می‌شوند", "معدن روان می‌شوند"),
    ("معدنFlow", "معدن روان"),
    ("به سمت معدنFlow", "به سمت معدن روان"),
    ("برaste بخشید", "بر آستانه نهاد"),
    ("بoslavید", "بنوشید"),
    ("بگنجam", "بگنجم"),
    ("می‌افتدam", "می‌افتم"),
    ("می‌افتam", "می‌افتم"),
    ("بپرheز", "بپرهیز"),
    ("خ thornش", "خارش"),
    ("دل-bearing", "دلبر"),
    ("رنجvollen", "رنج‌آلود"),
    ("prometی نکرده", "قولی نداده"),
    ("ام tonight", "امشب"),
    ("hast به سمت او برو", "به سمت او برو"),
    ("joys‌ها و لذت‌ها", "لذت‌ها"),
    ("joysها و لذتها", "لذت‌ها"),
    ("swept away", "نابود"),
    ("loved ones", "عزیزان"),
    ("(Venus)", ""),
    ("خوش-hearted", "خوش‌قلب"),
    ("نرم-hearted", "نرم‌دل"),
    ("زن-like", "زنانه"),
    ("دیو like", "دیو‌گونه"),
    ("شیشهFragile", "شیشه‌ای شکننده"),
    ("یکservantِ", "یک خدمتگزارِ"),
    ("چوchaهای", "چون پیچ‌و‌تاب‌های"),
    ("ناhopeی", "ناامیدی"),
    ("دلbroken", "دلِ شکسته"),
    ("دلbrokened", "دلِ شکسته"),
    ("درخششLightning", "درخششِ برق"),
    ("درخ shinesد", "می‌درخشد"),
    ("اگر چHeart من", "اگرچه دلِ من"),
    ("چHeart", "دل"),
    ("Hoorان صفت", "حوران‌صفت"),
    ("چیزی else", "چیز دیگری"),
    ("خوب و bad خود", "خوب و بدِ خود"),
    ("کوچک-minded", "کوچک‌اندیش"),
    ("اینContrast", "این تقابل"),
    ("مقام cao رسیدند", "مقام بالا رسیدند"),
    ("می‌کند و contrast آن را", "می‌کند و آن را در تقابل با"),
    ("(طور Sinai)", "(طور سینا)"),
    ("بر لبWater", "بر لبِ آب"),
    ("خطرهایی lurking", "خطرهایی کمین‌کرده"),
    ("chăm sóc", "مراقبت"),
    ("انسان م edin", "انسان مدان"),
    ("رنجیدهeing", "رنجیده"),
    ("فراتر از هرMeasure", "فراتر از هر اندازه"),
    ("می God", "می"),
    ("ن hypocritical", "ریاکار"),
    ("بهawakening", "به بیداری"),
    ("بهThought", "به اندیشه"),
    ("از تو eman می‌شود", "از تو ساطع می‌شود"),
    ("reserv می‌کنم", "نگه می‌دارم"),
    ("به شدت lamenting و نابود شد", "به شدت نابود شد"),
    ("کلیدOpeningSolutions", "کلیدِ گشایش"),
    ("احساس extraño", "احساس غریبی"),
    ("también هستی", "نیز هستی"),
    ("Ãshoory:", ""),
    ("تصحیح ترجمه Al :", ""),
    ("آن‌ها باExperiences دردناک", "آن‌ها با تجربه‌های دردناک"),
    ("از چیزی جز می God", "از چیزی جز می"),
    ("شکستهed", "شکستهٔ"),
    ("رنجیده\u200ceing", "رنجیده"),
    ("صدای âm", "بانگ"),
]

# ---------------------------------------------------------------------------
# Token dictionary: standalone English/foreign words -> Persian.
# Applied with word boundaries for tokens surrounded by non-Latin chars.
# ---------------------------------------------------------------------------
TOKENS = {
    "generosity": "بخشندگی", "humble": "فروتن", "broken": "شکسته", "absence": "غیاب",
    "moment": "لحظه", "longing": "اشتیاق", "fragility": "شکنندگی", "stubborn": "سرسخت",
    "arrogance": "خودبزرگ‌بینی", "elegance": "ناز", "humility": "فروتنی", "intoxicated": "مست",
    "beauty": "زیبایی", "Beauty": "زیبایی", "lurking": "کمین‌کرده", "kindness": "مهربانی",
    "nourished": "پرورده", "darkness": "تاریکی", "wisdom": "خرد", "dawn": "سحر",
    "forgiveness": "بخشایش", "Fragile": "شکننده", "superficial": "سطحی", "mythical": "افسانه‌ای",
    "disbelief": "ناباوری", "sustenance": "روزی", "ignorance": "نادانی", "someone": "کسی",
    "attentive": "هوشیار", "intoxication": "مستی", "justice": "عدالت", "jealousy": "رشک",
    "calm": "آرام", "blossoming": "شکوفایی", "solitude": "خلوت", "freshness": "تازگی",
    "essence": "گوهر", "contrast": "تقابل", "sometimes": "گاهی", "divine": "الهی",
    "healing": "التیام", "Healing": "التیام", "poet": "شاعر", "ego": "خودخواهی",
    "wealth": "ثروت", "Wealth": "ثروت", "punished": "مجازات", "people": "مردم",
    "desire": "اشتیاق", "rushed": "شتافت", "haste": "شتاب", "stubbornness": "سرسختی",
    "carefree": "بی‌خیال", "meditation": "مراقبه", "enchanting": "افسونگر", "calamities": "بلاها",
    "unfortunately": "متأسفانه", "nimble": "چابک", "antidote": "پادزهر", "sensitive": "حساس",
    "others": "دیگران", "seductive": "فریبنده", "youth": "جوانی", "celebration": "جشن",
    "host": "میزبان", "Figuren": "عروسک‌ها", "back": "باز", "regret": "پشیمان",
    "virtuous": "پرهیزگارتر", "cowardice": "بزدلی", "affection": "مهر", "coexist": "همزیستی",
    "exchanged": "عوض", "feast": "ضیافت", "warmth": "گرمی", "wanderers": "غریبان",
    "swordplay": "شمشیربازی", "intellect": "خرد", "hospitality": "مهمان‌نوازی", "impatient": "کم‌طاقت",
    "surroundings": "اطرافیان", "tone": "لحن", "talent": "هنر", "shame": "شرم",
    "temptations": "وسوسه‌ها", "celestial": "آسمانی", "beloved": "معشوق", "bending": "خم",
    "servant": "خدمتگزار", "astonishment": "شگفتی", "caravan": "کاروان", "revitalized": "تازه",
    "symbolic": "نمادین", "handful": "مشتی", "Strings": "تارها", "bloom": "شکوفه",
    "container": "ظرف", "renewed": "تازه", "unseen": "غیب", "restless": "ناآرام",
    "sorrow": "اندوه", "vergonha": "شرم", "Heart": "دل", "drink": "شراب",
    "treasures": "گنج‌ها", "between": "بین", "Vigilance": "هوشیاری", "material": "ماده",
    "caller": "بانگ", "Materialistic": "مادی", "daggers": "خنجرها", "enchantment": "افسون",
    "trace": "رد", "loosen": "باز", "revitalizes": "تازه", "worshipers": "نمازگزاران",
    "fortune": "بخت", "person": "کس", "now": "اکنون", "poorer": "ضعیف‌تر",
    "reasoning": "استدلال", "forever": "همیشه", "animals": "جانوران", "sunlight": "نور خورشید",
    "masculinity": "مردانگی", "tonight": "امشب", "Natur": "طبیعت", "wandering": "سرگردان",
    "greedy": "حریص", "teachings": "آموزه‌ها", "branches": "شاخه‌ها", "sacrifices": "فداکاری‌ها",
    "heights": "اوج", "captivating": "دلفریب", "Lessons": "درس‌ها", "embodiment": "تجسم",
    "besides": "جز", "bewildered": "سرگشته", "vibrancy": "شادابی", "Secret": "پنهان",
    "imprisoned": "زندانی", "armor": "زره", "heavenly": "بهشتی", "patience": "شکیبایی",
    "generous": "بخشنده", "seriousness": "جدیت", "fresh": "تازه", "symbol": "نماد",
    "intertwined": "درهم‌تنیده", "cheeks": "گونه‌ها", "arrival": "رسیدن", "reconciliating": "آشتی",
    "full": "کامل", "realmente": "واقعاً", "unir": "اتحاد", "esencia": "گوهر",
    "allure": "جذابیت", "significance": "اهمیت", "permanence": "جاودانگی", "throne": "تخت",
    "place": "جا", "earthly": "مادی", "tension": "کشش", "gratitude": "سپاسگزاری",
    "outward": "ظاهر", "stagnant": "راکد", "Measure": "اندازه", "anguish": "اندوه",
    "forgiving": "بخشاینده", "trumpet": "دمیدن", "blame": "سرزنش", "hidden": "پنهان",
    "comparisons": "قیاس‌ها", "inexistence": "عدم", "idol": "بت", "cunning": "حیله",
    "punishment": "مجازات", "temperament": "خوی", "companionship": "همنشینی", "Experiences": "تجربه‌ها",
    "union": "اتحاد", "revelation": "وحی", "else": "دیگر", "REALITY": "واقعیت",
    "proud": "سربلند", "nourishment": "غذا", "blindness": "نابینایی", "bad": "بد",
    "walking": "راه‌رفتن", "seeds": "بذر", "riches": "ثروت", "confusion": "آشفتگی",
    "selfish": "خودخواه", "Contrast": "تقابل", "inner": "درونی", "loyalty": "وفاداری",
    "nourish": "پرورش", "attracted": "جذب", "warnings": "هشدارها", "Ausdruck": "نشان",
    "mesmerized": "متحیر", "adversaries": "دشمنان", "grace": "ظرافت", "distressed": "غمگین",
    "medicinal": "دارویی", "highs": "اوج", "Shock": "ضربه", "superiority": "برتری",
    "tristeza": "اندوه", "excitement": "هیجان", "Humility": "فروتنی", "inevitable": "ناگزیر",
    "illness": "بیماری", "belongings": "دارایی‌ها", "arrived": "آمد", "arrogant": "متکبر",
    "destruction": "نابودی", "warriors": "جنگاوران", "traditions": "سنت‌ها", "belonged": "تعلق",
    "vibrance": "درخشندگی", "passion": "شور", "desires": "آرزوها", "ruler": "فرمانروا",
    "possessions": "دارایی‌ها", "tricks": "ترفندها", "sages": "حکیمان", "Innocent": "معصوم",
    "cleverness": "زیرکی", "around": "گرداگرد", "illuminated": "روشن", "captiv": "شیفته",
    "Departure": "جدایی", "prayed": "نماز", "hardship": "رنج", "harmony": "هماهنگی",
    "Venus": "زهره", "Sinai": "سینا", "Water": "آب", "God": "خدا", "Ai": "",
    "Al": "", "AI": "", "ai": "",
    "reserv": "نگه", "Flow": "روان", "Hoor": "حور",
}

LATIN_RE = re.compile(r"[A-Za-zÀ-ÿ]{2,}")

def fix_meaning(m):
    if not m:
        return m
    s = clean_ai(m)
    for a, b in EXACT:
        s = s.replace(a, b)
    # token replacement (only standalone tokens — i.e. bounded by non-Latin)
    def repl(mo):
        t = mo.group(0)
        tr = TOKENS.get(t)
        if tr is None:
            tr = TOKENS.get(t.lower())
        return tr if tr is not None else t
    s = LATIN_RE.sub(repl, s)
    # tidy double spaces / stray spacing around ZWNJ
    s = re.sub(r"\s{2,}", " ", s).strip()
    s = re.sub(r"\s+([،؛:.)])", r"\1", s)
    return s

def main():
    total_fixed = collections.Counter()
    for name in FILES:
        path = f"{BASE}/{name}.dat"
        data = json.loads(gzip.open(path, "rt", encoding="utf-8").read())
        changed = 0
        for p in data:
            pid = p["id"]
            # 1) drop *** placeholder verses
            new_verses = [v for v in p.get("verses") or [] if (v.get("first") or "").strip() != "***"]
            if len(new_verses) != len(p.get("verses") or []):
                total_fixed["star_verses"] += len(p.get("verses") or []) - len(new_verses)
                changed += 1
            p["verses"] = new_verses
            # 2) fix verses
            for vi, v in enumerate(p["verses"]):
                m = v.get("meaning")
                key = (name, pid, vi)
                if key in GARBLED:
                    v["meaning"] = GARBLED[key]
                    total_fixed["garbled"] += 1
                    changed += 1
                    continue
                if m:
                    nm = fix_meaning(m)
                    if nm != m:
                        v["meaning"] = nm
                        total_fixed["meaning_cleaned"] += 1
                        changed += 1
                    # typos
                    if "خافظ" in (v["meaning"] or ""):
                        v["meaning"] = (v["meaning"] or "").replace("خافظ", "حافظ")
                        total_fixed["typo_khafez"] += 1
                        changed += 1
                    if "حافط" in (v["meaning"] or ""):
                        v["meaning"] = (v["meaning"] or "").replace("حافط", "حافظ")
                        total_fixed["typo_hafet"] += 1
                        changed += 1
        # re-serialize (always, to normalize)
        gzip.open(path, "wt", encoding="utf-8").write(json.dumps(data, ensure_ascii=False, separators=(",", ":")))
        print(f"{name}: {changed} poems touched")
    print("\nFIX TOTALS:", dict(total_fixed))

    # ---- verification: any Latin left in meanings/tafsirs? ----
    left = []
    for name in FILES:
        data = json.loads(gzip.open(f"{BASE}/{name}.dat", "rt", encoding="utf-8").read())
        for p in data:
            for vi, v in enumerate(p.get("verses") or []):
                for field in ("meaning",):
                    t = v.get(field) or ""
                    for w in LATIN_RE.findall(t):
                        left.append((name, p["id"], vi, w, t[:90]))
    if left:
        print("\nREMAINING LATIN (need manual):")
        for x in left[:80]:
            print("  ", x)
        print(f"  ... total {len(left)}")
    else:
        print("\nOK — no Latin left in meanings.")

if __name__ == "__main__":
    main()
