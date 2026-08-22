# -*- coding: utf-8 -*-
"""
بازنویسیِ نهایی تفسیرها (tafsir):
- هیچ‌جا خودِ شعر (متنِ بیت) داخلِ تعبیر نقل نمی‌شود.
- هر تفسیر: شناساییِ موضوع + بازتابِ درون‌مایه (برگرفته از معنیِ بیت، نه متنِ بیت) + پیامِ شخصیِ دوم‌شخص.
- تفسیرهای دست‌نویس و بخشِ «جهان» دست نمی‌خورند.

فقط تفسیرهای ساختِ قبلی که با «می‌فرماید: «…»» شعر را نقل می‌کردند بازتولید می‌شوند.
"""
import gzip, json, re, collections, sys

BASE = "app/src/main/assets/corpus"
FILES = ["hafez", "khayyam", "saadi", "rumi", "stories"]
SKIP_COLLECTIONS = {"stories"}

THEME_PHRASE = {
    "love": "عشق و دل‌سپردن",
    "hope": "امید و گشایش",
    "patience": "صبوری و گذر از سختی",
    "joy": "شادی و عشرت",
    "wisdom": "دانایی و بینش",
    "faith": "توکل و آرامشِ دل",
    "effort": "کوشش و همّت",
    "travel": "سفر و گذر از منزل‌ها",
    "decision": "انتخاب و تصمیم",
    "compassion": "مهر و بخشندگی",
    "new-beginnings": "آغازی تازه",
    "general": "زندگی و روزگار",
}

FORTUNES = {
    "love": "این فال از دلِ عاشقانه‌ات خبر می‌دهد؛ دلت را پنهان مکن، که آنچه با خلوص پیشکش کنی دیر یا زود به تو بازمی‌گردد. اگر میانِ بودن و نبودن مردّدی، یادت باشد: عشقی که از صبوری می‌گذرد، ماندگار می‌شود.",
    "hope": "این فال بشارت می‌دهد: شبِ نگرانی‌ات به سحر می‌رسد و آنچه منتظرش هستی، از راهی می‌رسد که گمانش را نمی‌بری. ناامیدی را رها کن؛ روشنایی همین نزدیکی است.",
    "patience": "این فال از صبوری می‌گوید: درختِ صبور میوه می‌دهد و سختی‌های تو موقت‌اند. شتاب مکن؛ آنچه می‌خواهی با عجله به دست نمی‌آید، اما با شکیبایی حتماً می‌رسد.",
    "joy": "این فال دعوتی است به شادی: غم را زمین بگذار و برای دلِ امروزت کاری کن. دلِ شاد گنجِ بی‌پایان است؛ آن را از هیاهوی روزگار پس بگیر.",
    "wisdom": "فالِ تو دعوتی است به دانایی: پیش از هر گام اندیشه کن، که تدبیر چراغِ راه است. آنچه امروز درست می‌نماید، با نگاهی سنجیده فردا نیز روشن خواهد ماند.",
    "faith": "این فال از ایمان و توکل می‌گوید: کار را با تدبیر پیش ببر و دلت را آرام نگه دار. هر کاری که با نیّتِ پاک آغاز شود، فرجامش روشن است.",
    "effort": "این فال از کوشش می‌گوید: هیچ گنجی بی‌هنر به دست نمی‌آید و کارِ امروز فردای تو را می‌سازد. دست از تلاش برندار؛ آنچه می‌کاری، همان را درو خواهی کرد.",
    "travel": "این فال از سفر می‌گوید: راهی پیش روی توست و هر منزل درسی برای منزلِ بعد دارد. توشه بردار و با توکل گام بردار؛ همراهِ تو از خودت به تو نزدیک‌تر است.",
    "decision": "این فال برای تصمیمِ تو آمده است: دودل مباش، که آنچه دل و عقل با هم تأیید کنند همان راهِ درست است. هنگامِ انتخاب فرا رسیده؛ به نشانه‌های درونت اعتماد کن و گامِ نخست را بردار.",
    "compassion": "این فال از مهر می‌گوید: با دیگران مهربان باش، که نیکی گم نمی‌شود و به تو بازمی‌گردد. دلِ بزرگ خانهٔ آرامش است؛ ببخش تا سبک شوی.",
    "new-beginnings": "این فال از آغازی تازه خبر می‌دهد: فصلِ نو در راه است و درهای بسته یکی‌یکی باز می‌شوند. از گذشته سبک‌بار شو و با دلِ باز از نو آغاز کن.",
    "general": "این فال برای تو آمده است: با آرامش قدم بردار و به نشانه‌های راه اعتماد کن. نیّتت را پاک نگه‌دار؛ فرجامِ کارِ پاک، نیکوست.",
}

OPENING = {
    ("hafez", "ghazal"): "حافظ در این غزل",
    ("hafez", "qete"): "حافظ در این قطعه",
    ("hafez", "qaside"): "حافظ در این قصیده",
    ("hafez", "attributed"): "حافظ در این ابیات",
    ("hafez", "hafez-robaee"): "حافظ در این رباعی",
    ("saadi", "saadi-ghazal"): "سعدی در این غزل",
    ("saadi", "saadi-robaee"): "سعدی در این رباعی",
    ("saadi", "saadi-ghete"): "سعدی در این قطعه",
    ("saadi", "golestan"): "سعدی در این ابیات",
    ("saadi", "bustan"): "سعدی در این ابیات",
    ("saadi", "saadi-molhaghat"): "سعدی در این ابیات",
    ("rumi", "shams"): "مولانا در این غزل",
    ("rumi", "masnavi"): "مولانا در این ابیات",
    ("rumi", "robaee"): "مولانا در این رباعی",
    ("khayyam", "rubaiyat"): "خیام در این رباعی",
}

COLL_FA = {
    "ghazal": "غزلیات", "qete": "قطعات", "qaside": "قصاید", "attributed": "اشعار منتسب",
    "hafez-robaee": "رباعیات", "saadi-ghazal": "غزلیات", "saadi-robaee": "رباعیات",
    "saadi-ghete": "قطعات", "golestan": "گلستان", "bustan": "بوستان",
    "saadi-molhaghat": "ملحقات", "shams": "دیوان شمس", "masnavi": "مثنوی معنوی",
    "robaee": "رباعیات", "rubaiyat": "رباعیات",
}

GENERATED_RE = re.compile(r":\s*«")          # نشانهٔ ساختِ قبلی که شعر را نقل می‌کرد
BAD_MEANING = re.compile(r"[«»:←]|مصدر|جناس|استعار|کنایه|اشاره دارد|واژه|لغت|مخفّف|مخفف|یعنی فلان|به معنیِ|/ ")
VOCATIVE = re.compile(r"^(ای|یا)\s+[^!؟]*[!؟]\s*|^ساقی[ا]?[!،]?\s*")

# تفسیرهای دست‌نویسی که مصرعِ کاملِ شعر را در خود نقل کرده‌اند → بازتولید با قالبِ بدونِ نقل.
FORCE_REWRITE = {2131, 2137, 2140, 2150, 2155, 2166, 2170, 2181, 2599, 2622}

FA_DIGITS = str.maketrans("0123456789", "۰۱۲۳۴۵۶۷۸۹")
ORD = ["نخستین", "دومین", "سومین", "چهارمین", "پنجمین", "ششمین", "هفتمین", "هشتمین", "نهمین", "دهمین",
       "یازدهمین", "دوازدهمین", "سیزدهمین", "چهاردهمین", "پانزدهمین", "شانزدهمین", "هفدهمین", "هجدهمین", "نوزدهمین", "بیستمین"]


def ordinal(n):
    if 1 <= n <= 20:
        return ORD[n - 1]
    return str(n).translate(FA_DIGITS) + "مین"


def clean_clause(s):
    if not s:
        return None
    t = s.strip()
    t = re.sub(r"^معنی\s*[««][^»»]*[»»]\s*[:：]?\s*", "", t)
    t = re.sub(r"^نکته\s*[:：]?\s*", "", t)
    t = re.sub(r"^یعنی\s*", "", t)
    t = re.split(r"\((نکته|منبع|تصحیح|معنی)", t)[0]
    t = re.sub(r"\s+", " ", t).strip()
    # حذفِ «ای فلان!» / «ساقی!» از آغاز — تا درون‌مایه به خوانشِ خودِ شعر نیفتد.
    t = VOCATIVE.sub("", t)
    # حالتِ بدونِ علامت: «ای صوفی بیا که …» → فقط واژهٔ ندا حذف می‌شود.
    t = re.sub(r"^(ای|یا)\s+[^،\s]+[،]?\s*", "", t)
    # پاک‌کردنِ اِعرابِ باقی‌مانده از ابتدا («ِ بهشتی» و…)
    t = re.sub(r"^[\s\u064B-\u0652]+", "", t)
    m = re.split(r"[.؛]", t)[0].strip()
    if len(m) < 8:
        return None
    if BAD_MEANING.search(m):
        return None
    if len(m) > 130:
        cut = m[:130]
        sp = cut.rfind(" ")
        m = cut[:sp] if sp > 60 else cut
    return m.rstrip("،, ")


def motif_of(poem):
    verses = poem.get("verses") or []
    couplets = [v for v in verses if v.get("second")]
    pool = couplets or verses
    for v in pool:
        c = clean_clause(v.get("meaning"))
        if c:
            return c
    return None


def generate(p):
    poet = p["poet"]
    coll = p.get("collection", "")
    opening = OPENING.get((poet, coll), "شاعر در این شعر")
    theme = p.get("themeTag") or "general"
    phrase = THEME_PHRASE.get(theme, THEME_PHRASE["general"])
    fortune = FORTUNES.get(theme, FORTUNES["general"])
    motif = motif_of(p)
    if motif:
        # بدونِ نقلِ شعر: پیام در قالبِ «این است که …» بازتاب داده می‌شود.
        m = motif.rstrip(".؟!، ")
        return f"{opening} از {phrase} می‌گوید؛ پیامِ آن این است که {m}. {fortune}"
    # بدون معنیِ قابل استفاده: مُهرِ یکتا با جایگاهِ شعر در مجموعه.
    coll_fa = COLL_FA.get(coll, "این دفتر")
    return f"{opening} از {phrase} می‌گوید. این برگزیده، {ordinal(p.get('number') or 1)}ِ {coll_fa} است. {fortune}"


def main():
    regenerated = 0
    for f in FILES:
        path = f"{BASE}/{f}.dat"
        data = json.loads(gzip.open(path, "rt", encoding="utf-8").read())
        for p in data:
            if p.get("collection") in SKIP_COLLECTIONS:
                continue
            if p["id"] in FORCE_REWRITE or GENERATED_RE.search(p["tafsir"]):
                p["tafsir"] = generate(p)
                regenerated += 1
        gzip.open(path, "wt", encoding="utf-8").write(
            json.dumps(data, ensure_ascii=False, separators=(",", ":"))
        )
    print(f"regenerated: {regenerated}")

    latin = re.compile(r"[A-Za-zÀ-ÿ]{2,}")
    owners = collections.defaultdict(list)
    bad = []
    for f in FILES:
        data = json.loads(gzip.open(f"{BASE}/{f}.dat", "rt", encoding="utf-8").read())
        for p in data:
            t = p["tafsir"].strip()
            owners[t].append((f, p["id"]))
            if latin.search(t) or "\n" in t or len(t) < 20:
                bad.append((f, p["id"], t[:70]))
    dups = {t: l for t, l in owners.items() if len(l) > 1}
    print(f"remaining duplicated tafsirs: {len(dups)}")
    for t, l in list(dups.items())[:10]:
        print("  DUP:", l, t[:90])
    print(f"latin/newline/short issues: {len(bad)}")
    for b in bad[:10]:
        print("  BAD:", b)
    if dups or bad:
        sys.exit(1)
    print("OK")

if __name__ == "__main__":
    main()
