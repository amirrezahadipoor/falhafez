# -*- coding: utf-8 -*-
"""
Regenerates a unique, poem-specific tafsir for every poem whose current
tafsir text is shared with another poem (globally, across all corpus files).

Hand-written / already-unique tafsirs are never touched.

The generated tafsir is anchored on the poem's OWN first beit meaning
(unique per poem) so each poem gets a distinct, relevant interpretation.
"""
import gzip, json, re, collections, sys

BASE = "app/src/main/assets/corpus"
FILES = ["hafez", "khayyam", "saadi", "rumi", "stories"]

POET_FA = {"hafez": "حافظ", "saadi": "سعدی", "rumi": "مولانا", "khayyam": "خیام"}

OPENINGS = {
    "hafez": [
        "حافظ در این غزل می‌فرماید:",
        "این شعرِ حافظ چنین می‌سراید:",
        "حافظ در این بیت‌ها چنین گفته است:",
    ],
    "saadi": [
        "سعدی در این شعر می‌فرماید:",
        "این کلامِ سعدی چنین می‌سراید:",
        "سعدی در این ابیات چنین گفته است:",
    ],
    "rumi": [
        "مولانا در این شعر می‌فرماید:",
        "این کلامِ مولانا چنین می‌سراید:",
        "مولانا در این ابیات چنین گفته است:",
    ],
    "khayyam": [
        "خیام در این رباعی می‌فرماید:",
        "این رباعیِ خیام چنین می‌سراید:",
        "خیام در این ابیات چنین گفته است:",
    ],
}

FORTUNES = {
    "love": [
        "این فال از دلِ عاشقانه‌ات خبر می‌دهد: دلت را پنهان مکن؛ آنچه با خلوص پیشکش کنی، دیر یا زود به تو بازمی‌گردد.",
        "پیامِ این فال برای تو روشن است: عشق، راهِ دل است؛ به آن وفادار بمان و از دشواریِ راه نهراس.",
        "فالِ تو می‌گوید: محبتت دیده می‌شود؛ صبور باش، که وصال، پاداشِ دل‌هایی است که از نیمه‌راه برنگردند.",
    ],
    "wisdom": [
        "فالِ تو دعوتی است به دانایی: پیش از هر گام، اندیشه کن؛ که تدبیر، چراغِ راهِ بی‌برگشت است.",
        "پیامِ این فال: خِرد را با دل همراه کن؛ آنچه امروز درست می‌نماید، فردا نیز روشن خواهد بود.",
        "فالِ تو می‌گوید: حکمت را در سکوت و تأمل بجوی؛ پاسخِ پرسش‌هایت در آرامشِ درونت است.",
    ],
    "decision": [
        "این فال برای تصمیمِ تو آمده است: دودل مباش؛ آنچه دل و عقل با هم تأیید کنند، همان راهِ درست است.",
        "فالِ تو می‌گوید: هنگامِ انتخاب فرا رسیده؛ به نشانه‌های درونت اعتماد کن و گامِ نخست را بردار.",
        "پیامِ این فال: در دوراهی که ایستاده‌ای، سکوت کن و گوش بسپار؛ پاسخ از درونِ خودت بلند می‌شود.",
    ],
    "hope": [
        "این فال بشارت می‌دهد: شبِ نگرانی‌ات به سحر می‌رسد؛ ناامید مباش، که روشنی در راه است.",
        "فالِ تو می‌گوید: امیدت را از دست مده؛ آنچه منتظرش هستی، از راهی می‌رسد که گمانش را نمی‌بری.",
        "پیامِ این فال: دل را به فردا گرم کن؛ که هر سحری، دری تازه به روی امید می‌گشاید.",
    ],
    "new-beginnings": [
        "این فال از آغازی تازه خبر می‌دهد: فصلِ نو در راه است؛ آماده باش و از گذشته سبک‌بار شو.",
        "فالِ تو می‌گوید: فرصتی نو پیش روی توست؛ آن را با دلِ باز بپذیر و از نو آغاز کن.",
        "پیامِ این فال: همچون بهار، زندگی‌ات رنگی تازه می‌گیرد؛ درهای بسته، یکی‌یکی باز می‌شوند.",
    ],
    "joy": [
        "این فال دعوتی است به شادی: غم را زمین بگذار؛ زندگی برای لذت‌های کوچکِ امروز است.",
        "فالِ تو می‌گوید: لبخند را فراموش مکن؛ شادی‌ات را با دیگران قسمت کن تا دوچندان شود.",
        "پیامِ این فال: دلِ شاد، گنجِ بی‌پایان است؛ آن را از هیاهوی روزگار پس بگیر.",
    ],
    "patience": [
        "این فال از صبوری سخن می‌گوید: درختِ صبور میوه می‌دهد؛ سختی‌هایت موقت‌اند و می‌گذرند.",
        "فالِ تو می‌گوید: شتاب مکن؛ آنچه می‌خواهی با عجله به دست نمی‌آید، اما با شکیبایی می‌رسد.",
        "پیامِ این فال: صبر کن و دلت را آرام نگه‌دار؛ که هر انتظاری، سرانجامی نیکو دارد.",
    ],
    "travel": [
        "این فال از سفر می‌گوید: راهی پیش روی توست؛ توشه بردار و با توکل گام بردار.",
        "فالِ تو می‌گوید: مسیرت دراز اما روشن است؛ هر منزل، درسی برای منزلِ بعد دارد.",
        "پیامِ این فال: سفرِ تو آغاز شده؛ از مقصد نترس، که همراهِ تو از تو به تو نزدیک‌تر است.",
    ],
    "effort": [
        "این فال از کوشش می‌گوید: هیچ گنجی بی‌هنر به دست نمی‌آید؛ کارِ امروز، فردای تو را می‌سازد.",
        "فالِ تو می‌گوید: دست از تلاش برندار؛ آنچه می‌کاری، همان را درو خواهی کرد.",
        "پیامِ این فال: کوششت دیده می‌شود؛ پاداش، سهمِ کسی است که از نیمه‌راه برنگردد.",
    ],
    "faith": [
        "این فال از ایمان و توکل می‌گوید: کار را به خدا بسپار، اما پارو زدن را رها مکن.",
        "فالِ تو می‌گوید: دل را با یادِ او آرام کن؛ هر کاری که با نیتِ پاک آغاز شود، فرجامش روشن است.",
        "پیامِ این فال: به وعده‌های راستین امیدوار باش؛ که روزی، از جایی می‌رسد که گمانش را نمی‌بری.",
    ],
    "compassion": [
        "این فال از مهر می‌گوید: با دیگران مهربان باش؛ که نیکی، گم نمی‌شود و به تو بازمی‌گردد.",
        "فالِ تو می‌گوید: دلِ بزرگ، خانهٔ آرامش است؛ ببخش تا سبک شوی.",
        "پیامِ این فال: مهرِ تو دیده می‌شود؛ دستِ بخشنده‌ات، هرگز خالی نمی‌ماند.",
    ],
    "general": [
        "این فال برای تو آمده است: با آرامش قدم بردار و به نشانه‌های راه اعتماد کن.",
        "فالِ تو می‌گوید: نیتت را پاک نگه‌دار؛ که فرجامِ کارِ پاک، نیکوست.",
        "پیامِ این فال: دل را روشن نگه‌دار؛ که هرچه پیش آید، به حکمت است.",
    ],
}

def trim_sentence(s, limit=170):
    """Cut at the last sentence-ish boundary before `limit`."""
    s = s.strip()
    if len(s) <= limit:
        return s
    cut = s[:limit]
    # prefer cutting at end of a clause
    for sep in ("؛ ", ". ", "، ", "؛", "،"):
        idx = cut.rfind(sep)
        if idx > limit * 0.5:
            return cut[: idx + len(sep.strip())].strip()
    return cut.rstrip("، ؛") + "…"

def clean_anchor(m):
    """Take a beit meaning and turn it into a clean anchor phrase."""
    if not m:
        return None
    s = m.strip()
    # drop trailing notes like (نکته: …) / (منبع: …) / (تصحیح: …)
    s = re.split(r"\((?:نکته|منبع|تصحیح|معنی|توجه)\s*[:：]", s)[0]
    # drop a leading "نکته:" note
    s = re.sub(r"^نکته\s*[:：]?\s*", "", s)
    s = re.sub(r"\s+", " ", s).strip()
    s = trim_sentence(s)
    # ensure it ends with a sentence end
    if s and s[-1] not in ".؛!؟":
        s += "."
    return s or None

def make_anchor(p):
    verses = p.get("verses") or []
    # prefer couplet verses (with a second hemistich) over prose lines
    couplets = [v for v in verses if v.get("second")]
    if not couplets:
        couplets = verses
    # 1) first couplet's meaning (poem-specific)
    for v in couplets:
        a = clean_anchor(v.get("meaning"))
        if a and len(a) >= 25:
            return a
    for v in couplets:
        a = clean_anchor(v.get("meaning"))
        if a:
            return a
    # 2) fallback: first couplet's own text
    if couplets:
        v = couplets[0]
        txt = (v.get("first") or "") + (("؛ " + v["second"]) if v.get("second") else "")
        return trim_sentence(txt.strip())
    # 3) last resort
    if verses:
        v = verses[0]
        txt = (v.get("first") or "") + (("؛ " + v["second"]) if v.get("second") else "")
        return trim_sentence(re.sub(r"\s+", " ", txt).strip())
    return "این شعر برای تو پیامی دارد."

def generate(p):
    poet = p["poet"]
    coll = p.get("collection", "")
    pid = p["id"]
    opening = OPENINGS.get(poet, OPENINGS["hafez"])[pid % 3]
    anchor = make_anchor(p)
    theme = p.get("themeTag") or "general"
    fortunes = FORTUNES.get(theme, FORTUNES["general"])
    fortune = fortunes[(pid // 7) % 3]
    if coll == "stories":
        opening = "سعدی در این حکایت می‌فرماید:"
    return f"{opening} «{anchor}» {fortune}"

def main():
    # load everything
    data = {f: json.loads(gzip.open(f"{BASE}/{f}.dat", "rt", encoding="utf-8").read()) for f in FILES}
    # find tafsir texts shared by 2+ poems (globally)
    owners = collections.defaultdict(list)
    for f in FILES:
        for p in data[f]:
            owners[p["tafsir"].strip()].append((f, p["id"]))
    dup_texts = {t for t, locs in owners.items() if len(locs) > 1}

    regenerated = 0
    for f in FILES:
        for p in data[f]:
            if p["tafsir"].strip() in dup_texts or "\n" in (p["tafsir"] or ""):
                p["tafsir"] = generate(p)
                regenerated += 1
        gzip.open(f"{BASE}/{f}.dat", "wt", encoding="utf-8").write(
            json.dumps(data[f], ensure_ascii=False, separators=(",", ":"))
        )
    print(f"regenerated tafsirs: {regenerated}")

    # verify uniqueness + no latin
    all_tafsirs = collections.defaultdict(list)
    latin = re.compile(r"[A-Za-zÀ-ÿ]{2,}")
    bad = []
    for f in FILES:
        for p in data[f]:
            t = p["tafsir"].strip()
            all_tafsirs[t].append((f, p["id"]))
            if latin.search(t):
                bad.append((f, p["id"], t[:60]))
            if len(t) < 20:
                bad.append((f, p["id"], "TOO SHORT", t[:60]))
    dups = {t: l for t, l in all_tafsirs.items() if len(l) > 1}
    print(f"remaining duplicated tafsirs: {len(dups)}")
    for t, l in list(dups.items())[:10]:
        print("  DUP:", l, t[:80])
    print(f"latin/short issues: {len(bad)}")
    for b in bad[:10]:
        print("  BAD:", b)
    if dups or bad:
        sys.exit(1)

if __name__ == "__main__":
    main()
