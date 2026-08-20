# -*- coding: utf-8 -*-
import json, re

raw = json.load(open("/tmp/raw_divans.json"))
lists = json.load(open("/tmp/divan_lists.json"))

DIACRITICS = set("ًٌٍَُِّْٕٖٜٟۣٓٔٗ٘ٙٚٛٝٞۖۗۘۙۚۛۜ۟۠ۡۢۤۥۦ")
LETTERS = re.compile(r"[^\u0621-\u064A\u067E\u0686\u0698\u06AF\u06A9\u06CC\u06C0\u06CD\u06D5]")
def lkey(s):
    s = s.replace("\u200c","").replace("\u200d","")
    s = s.replace("ي","ی").replace("ك","ک").replace("ۀ","ه").replace("أ","ا").replace("إ","ا").replace("آ","ا")
    s = "".join(ch for ch in s if ch not in DIACRITICS)
    return LETTERS.sub("", s)

POET_FA = {"hafez": "حافظ", "saadi": "سعدی", "rumi": "مولانا", "khayyam": "خیام"}

TAG_RULES = [
    ("hope",   ["امید","مژده","غم مخور","بازآید","بازآمد","گشایش","مرهم","نجات"]),
    ("patience",["صبر","شکیب","درد","اندوه","غصه","سختی","انتظار","هجران","فراق"]),
    ("joy",    ["باده","ساقی","طرب","نشاط","می خور","مستی","مطرب","شاد","پیمانه"]),
    ("new-beginnings",["بهار","نوروز","صبا","نسیم","نوبهار","شکفتن","جوانی"]),
    ("travel", ["سفر","کاروان","مسافر","منزل","کوچ","وادی"]),
    ("effort", ["همت","کوشش","سعی","تلاش"]),
    ("love",   ["عشق","معشوق","زلف","نگار","شاهد","دلبر"]),
]
def detect_tag(full):
    k = lkey(full)
    for tag, kws in TAG_RULES:
        if any(lkey(kw) in k for kw in kws): return tag
    return "wisdom"

THEME = {"love":"عشق و شورِ دیدار","hope":"امید و روزهای روشن","patience":"صبوری و گذر از سختی",
"joy":"شادی و لحظه‌های خوش","new-beginnings":"آغازِ تازه و نوزایی","travel":"سفر و جست‌وجوی مقصد",
"effort":"کوشش و ثمرهٔ آن","wisdom":"حکمتِ روزگار و رازِ دل"}

OPENERS = ["در این بخش، {poet} از {theme} می‌گوید.","{poet} این‌جا به {theme} می‌پردازد.",
"این سخنِ {poet} است دربارهٔ {theme}.","{poet} در اینجا از {theme} سخن می‌گوید.",
"این ابیات از {poet}، حدیثِ {theme} است.","{poet} در این ابیات، آینهٔ {theme} را می‌گرداند."]

MESSAGES = {
"love":["اگر نیّتت دل است، با مهربانی پیش برو؛ مهرِ راستین با صداقت می‌ماند.",
"دلی که خالصانه می‌خواهد، بی‌پاسخ نمی‌ماند؛ فقط زمان می‌خواهد.",
"نگاهِ این فال: از ابرازِ محبت نترس؛ آنچه می‌بخشی به تو بازمی‌گردد.",
"عشق را ساده نگیر و ساده رها نکن؛ نیم‌دلی به وصال نمی‌رسد.",
"دلِ تو دیده می‌شود؛ مهر، راهِ خودش را پیدا می‌کند.",
"آنچه در دل داری سرنوشت است؛ با آن رو راست باش."],
"hope":["دورهٔ انتظار رو به پایان است؛ درست وقتی ناامید شدی، گشایش می‌رسد.",
"آنچه منتظرش هستی به‌زودی از راه می‌رسد؛ صبر کن.",
"دری که بسته مانده، به‌زودی باز می‌شود.",
"شبِ نگرانی‌ات به سحر می‌رسد؛ نگران نباش.",
"روزهای روشن در راه‌اند؛ امیدت را رها نکن.",
"خبرِ خوش نزدیک است؛ گوش به زنگ باش."],
"patience":["آنچه می‌خواهی با عجله به دست نمی‌آید؛ شکیبایی بورز.",
"این سختی موقت است؛ صبر کن تا ابر بگذرد.",
"رنجِ امروز، فردایت را می‌سازد.",
"در برابرِ سختی کوتاه نیا؛ گشایش نزدیک است.",
"درختِ صبور، میوه می‌دهد؛ آرام بمان.",
"انتظار تو را قوی‌تر کرده؛ حالا وقتِ برداشت است."],
"joy":["دوره‌ای از شادی در پیش است؛ دلت را برایش باز کن.",
"اجازه بده شاد باشی؛ شادی هم بخشی از راه است.",
"کامت به‌زودی شیرین می‌شود.",
"غم را زمین بگذار؛ لحظه‌های خوب نزدیک‌اند.",
"شادی را با یاران قسمت کن.",
"شادی را به تعویق نینداز؛ همین امروز سهمِ دلت را بده."],
"new-beginnings":["از غم‌های کهنه دل بکن؛ آغازی تازه در راه است.",
"فصلِ تازه‌ای آغاز شده؛ کهنه‌ها را بگذار و برو.",
"فرصتی نو در راه است؛ غبارِ گذشته را فرو بریز.",
"بویِ بهار می‌دهد؛ شادمانیِ تازه نزدیک است.",
"دری تازه گشوده می‌شود؛ نترس و وارد شو.",
"هر پایانی، آغازی در دل دارد؛ به استقبالش برو."],
"travel":["جابه‌جایی در زندگی‌ات در پیش است؛ راه برو، نشانه‌ها را دنبال کن.",
"مقصد همان‌جاست که دلت را می‌شناسد؛ راه، خودش بخشی از پاسخ است.",
"قدمِ اول را بردار؛ راهِ تو باز است.",
"از سفری می‌گوید که به وصال می‌رسد؛ آرام برو.",
"سرگردانی‌ات پایان دارد؛ نشانه‌ای در راه است.",
"مسیر، معلمِ توست؛ از آن یاد بگیر."],
"effort":["نتیجهٔ کوششِ تو در راه است؛ دست از تلاش برندار.",
"رنجِ امروزت، گلِ فردایت را می‌پروراند.",
"با همّتِ بلند پیش برو و ناامید نشو.",
"کوششِ تو دیده می‌شود؛ ثمره‌اش در راه است.",
"به جای نگرانی، عمل کن؛ کار، خودش راه را باز می‌کند.",
"همّتت را بلند نگه دار؛ نتیجه از آنِ صبورانِ کوشاست."],
"wisdom":["به ظاهرِ کارها دل نبند؛ گاهی پاسخ در همان چیزی است که نادیده می‌گیری.",
"با آرامش و بینش پیش برو؛ عجله، راه را کور می‌کند.",
"از تجربه‌های گذشته درس بگیر و سبک‌بار برو.",
"آنچه می‌جویی با خرد و صبوری به دست می‌آید، نه با شتاب.",
"گره‌ای که با زور باز نمی‌شود، با نرمی باز می‌شود.",
"دل را از قیدهای بی‌حاصل آزاد کن؛ رهایی، خودش حکمت است."],
}

CLOSERS = {
"love":["دلت را پاک نگاه دار.","بگذار مهر راهش را بیابد.","از ابرازِ مهر نترس.","صبور باش و امیدوار."],
"hope":["امیدوار بمان.","به روشنایی اعتماد کن.","خبرِ خوش در راه است.","دلت را به فردا گرم کن."],
"patience":["صبور باش و استوار.","گشایش در راه است.","دلت را آرام نگاه دار.","سختی می‌گذرد."],
"joy":["شاد باش.","دل را به شادی بسپار.","از لحظه‌ها لذت ببر.","کامت شیرین باد."],
"new-beginnings":["به استقبالِ نو برو.","کهنه‌ها را رها کن.","از نو آغاز کن.","به بهارِ دل سلام کن."],
"travel":["راهت روشن است.","قدم بردار.","به مقصد اعتماد کن.","سفرت به خیر."],
"effort":["با همّت پیش برو.","از تلاش دست نکش.","ثمره در راه است.","کوشش کن و بسپار."],
"wisdom":["با چشمِ باز قدم بردار.","سبک‌بار باش.","حکمت را از دل بشنو.","آزاده باش."],
}

def gen(pid, poet, tag):
    tag = tag if tag in MESSAGES else "wisdom"
    poet_fa = POET_FA.get(poet, "شاعر")
    opener = OPENERS[pid % len(OPENERS)].format(poet=poet_fa, theme=THEME[tag])
    msg = MESSAGES[tag][(pid // 7) % len(MESSAGES[tag])]
    closer = CLOSERS[tag][(pid // 3) % len(CLOSERS[tag])]
    if pid % 10 < 3: return f"{msg} {closer}"
    return f"{opener} {msg} {closer}"

# ---------- existing hand-written tafsirs ----------
def load(p):
    try: return json.load(open(p, encoding="utf-8"))
    except Exception: return []

existing = {
    "saadi": {x["id"]: x for x in load("app/src/main/assets/corpus/saadi.json")},
    "rumi":  {x["id"]: x for x in load("app/src/main/assets/corpus/rumi.json")},
}

def build(poet, colls, out_path):
    out = []
    for coll in colls:
        ids = [p["id"] for p in lists[coll]]
        for pid in ids:
            r = raw[str(pid)]
            full = " ".join(b["first"] + " " + (b["second"] or "") for b in r["beits"])
            if pid in existing[poet]:
                tag = existing[poet][pid]["themeTag"]; tafsir = existing[poet][pid]["tafsir"]
            else:
                tag = detect_tag(full); tafsir = gen(pid, poet, tag)
            out.append({"id": pid, "poet": poet, "collection": r["collection"],
                        "number": r["number"], "themeTag": tag, "tafsir": tafsir,
                        "verses": [{"first": b["first"], "second": b["second"], "meaning": b["meaning"]} for b in r["beits"]]})
    out.sort(key=lambda x: x["id"])
    json.dump(out, open(out_path, "w"), ensure_ascii=False, indent=1)
    print(out_path, len(out), "poems")

build("saadi", ["golestan", "boostan", "ghazal"], "app/src/main/assets/corpus/saadi.json")
build("rumi", ["masnavi", "shams"], "app/src/main/assets/corpus/rumi.json")

# ---------- 100 instructive stories from Golestan ----------
golestan_ids = [p["id"] for p in lists["golestan"]]
qualified = []
for pid in golestan_ids:
    r = raw[str(pid)]
    prose = [b for b in r["beits"] if not b["second"]]
    couplets = [b for b in r["beits"] if b["second"]]
    if len(prose) >= 1 and len(couplets) >= 1:
        qualified.append((pid, prose, couplets))
qualified.sort(key=lambda t: t[0])
step = max(1, len(qualified) // 100)
picked = qualified[::step][:100]
if len(picked) < 100:
    for q in qualified:
        if len(picked) >= 100: break
        if q not in picked: picked.append(q)

stories = []
for i, (pid, prose, couplets) in enumerate(picked, 1):
    r = raw[str(pid)]
    text = "\n\n".join(b["first"] for b in prose)
    moral = couplets[-1]
    full = text + " " + " ".join(b["first"] + " " + (b["second"] or "") for b in r["beits"])
    tag = detect_tag(full)
    lesson = gen(pid, "saadi", tag)
    stories.append({
        "id": pid, "poet": "saadi", "collection": "stories",
        "number": i, "themeTag": tag, "tafsir": lesson,
        "verses": [
            {"first": text, "second": None, "meaning": None},
            {"first": moral["first"], "second": moral["second"], "meaning": None}
        ]
    })
json.dump(stories, open("app/src/main/assets/corpus/stories.json", "w"), ensure_ascii=False, indent=1)
print("stories.json:", len(stories), "stories")

# sanity: counts + sample
for n in ["saadi","rumi"]:
    d = json.load(open(f"app/src/main/assets/corpus/{n}.json", encoding="utf-8"))
    ids=[x["id"] for x in d]
    assert len(ids)==len(set(ids)), f"{n} dup ids"
    with_mean = sum(1 for p in d for v in p["verses"] if v.get("meaning"))
    print(f"{n}: {len(d)} poems, {with_mean} beits with meaning")
