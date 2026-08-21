# -*- coding: utf-8 -*-
"""Deep content audit for the Fal Hafez corpus (.dat = gzip JSON).

Usage: python3 tools/deep_content_audit.py
Exits non-zero if any CRITICAL issue is found.
"""
import gzip, json, re, sys, collections, unicodedata

CORPUS = ["hafez", "khayyam", "saadi", "rumi", "stories"]
BASE = "app/src/main/assets/corpus"

def load(name):
    p = f"{BASE}/{name}.dat"
    return json.loads(gzip.open(p, "rt", encoding="utf-8").read())

# ---------- Persian normalization for dedup ----------
AR = {
    "ي": "ی", "ك": "ک", "ة": "ه", "ۀ": "ه", "إ": "ا", "أ": "ا", "ٱ": "ا",
    "ى": "ی", "ھ": "ه", "ﺀ": "ء", "٠": "۰", "١": "۱", "٢": "۲", "٣": "۳",
    "٤": "۴", "٥": "۵", "٦": "۶", "٧": "۷", "٨": "۸", "٩": "۹",
    "۰": "۰", "۱": "۱", "۲": "۲", "۳": "۳", "۴": "۴", "۵": "۵",
    "۶": "۶", "۷": "۷", "۸": "۸", "۹": "۹",
}
DIACRITICS = set("\u064b\u064c\u064d\u064e\u064f\u0650\u0651\u0652\u0670\u0654\u0655\u0656\u0657\u0658\u0671\u0640\u0653")

def normalize(s):
    s = unicodedata.normalize("NFKC", s or "")
    s = "".join(AR.get(c, c) for c in s)
    s = "".join(c for c in s if c not in DIACRITICS)
    s = re.sub(r"\s+", " ", s).strip()
    return s

def alpha_key(s):
    """keep only Persian/Arabic letters for matching"""
    s = normalize(s)
    return re.sub(r"[^\u0600-\u06FF ]", " ", s)

issues = collections.defaultdict(list)
stats = collections.Counter()

all_poems = []
for name in CORPUS:
    d = load(name)
    stats[f"poems.{name}"] = len(d)
    all_poems += [("", p) for p in d]

# ---------- 1. global unique ids ----------
by_id = collections.defaultdict(list)
for name in CORPUS:
    for p in load(name):
        by_id[p["id"]].append(name)
for iid, names in by_id.items():
    if len(names) > 1:
        issues["CRITICAL.dup_global_id"].append(f"id {iid} in {names}")
stats["poems.total"] = sum(1 for _ in by_id)

# ---------- 2. per-poem structural ----------
themes = set()
for name in CORPUS:
    for p in load(name):
        pid = p["id"]
        # fields
        for f in ("poet", "collection", "themeTag", "tafsir"):
            if not p.get(f) or not str(p[f]).strip():
                issues["CRITICAL.empty_field"].append(f"{name} id {pid}: empty {f}")
        if p.get("tafsir"):
            stats["tafsir_len_total"] += len(p["tafsir"])
            stats["tafsir_count"] += 1
            if len(p["tafsir"]) < 40:
                issues["WARN.short_tafsir"].append(f"{name} id {pid}: {len(p['tafsir'])} chars: {p['tafsir'][:50]!r}")
        themes.add(p.get("themeTag"))
        # number uniqueness + continuity per (poet, collection)
        key = (p["poet"], p["collection"])
        stats[f"poems.{key[0]}.{key[1]}"] += 1

# number continuity per (poet,collection)
nums = collections.defaultdict(list)
for name in CORPUS:
    for p in load(name):
        nums[(p["poet"], p["collection"])].append((p["number"], p["id"]))
for k, v in nums.items():
    ns = sorted(n for n, _ in v)
    if ns != list(range(1, len(ns) + 1)):
        issues["WARN.number_gap"].append(f"{k}: numbers {ns[:20]}... (count {len(ns)})")

# ---------- 3. per-verse structural ----------
for name in CORPUS:
    for p in load(name):
        pid = p["id"]
        verses = p.get("verses") or []
        if not verses:
            issues["CRITICAL.empty_verses"].append(f"{name} id {pid}")
            continue
        positions = [v.get("position") for v in verses]
        if positions != list(range(len(verses))):
            issues["CRITICAL.bad_positions"].append(f"{name} id {pid}: {positions[:10]}")
        seen = set()
        for v in verses:
            f = v.get("first") or ""
            s = v.get("second")
            m = v.get("meaning")
            if not f.strip():
                issues["CRITICAL.empty_first"].append(f"{name} id {pid}")
            if s is not None and not str(s).strip():
                issues["WARN.empty_second"].append(f"{name} id {pid} pos {v.get('position')}")
            if m is None:
                stats["missing_meaning"] += 1
                issues["WARN.missing_meaning"].append(f"{name} id {pid} pos {v.get('position')}")
            elif not str(m).strip():
                stats["empty_meaning"] += 1
                issues["CRITICAL.empty_meaning"].append(f"{name} id {pid} pos {v.get('position')}")
            # first == second ?
            if s and normalize(f) == normalize(s) and normalize(f):
                issues["WARN.mesra_identical"].append(f"{name} id {pid} pos {v.get('position')}")
            # duplicate verse within poem
            key = normalize(f) + "|" + normalize(s or "")
            if key in seen:
                issues["WARN.dup_verse_in_poem"].append(f"{name} id {pid} pos {v.get('position')}: {f[:40]!r}")
            seen.add(key)
            stats["verse_total"] += 1

# ---------- 4. cross-poem duplicate verses ----------
verse_to_poems = collections.defaultdict(list)
for name in CORPUS:
    for p in load(name):
        for v in p.get("verses") or []:
            key = alpha_key(v.get("first") or "") + "|" + alpha_key(v.get("second") or "")
            if len(key.strip("| ")) > 10:
                verse_to_poems[key].append((name, p["id"], v.get("position")))
for k, locs in verse_to_poems.items():
    uniq = set((n, i) for n, i, _ in locs)
    if len(uniq) > 1:
        issues["WARN.cross_poem_dup_verse"].append(f"{k[:60]!r} in {sorted(uniq)[:6]}")

# ---------- 5. text-quality: control chars, html, mojibake, script ----------
CTRL = re.compile(r"[\x00-\x08\x0b\x0c\x0e-\x1f\x7f\u200b\u200c\u200d\u200e\u200f\ufeff\ufffd]")
HTML = re.compile(r"<[a-zA-Z/][^>]*>|&[a-zA-Z#0-9]+;")
LATIN = re.compile(r"[A-Za-z]")
ARABIC_DIGIT = re.compile(r"[٠-٩]")
ARABIC_ONLY = re.compile(r"[يكةأإٱىھ]")
def scan_text(label, name, pid, text, where):
    if not text:
        return
    for ch in set(text):
        if CTRL.match(ch) or ch == "\ufeff":
            issues["CRITICAL.control_char"].append(f"{label} {name} id {pid} {where}: U+{ord(ch):04X}")
            break
    if HTML.search(text):
        issues["CRITICAL.html_leak"].append(f"{label} {name} id {pid} {where}: {HTML.search(text).group(0)!r}")
    if "هوش مصنوعی" in text or "هوش‌مصنوعی" in text:
        issues["CRITICAL.ai_label"].append(f"{label} {name} id {pid} {where}")
    if re.search(r"\bnull\b|\bNone\b|TODO|FIXME", text):
        issues["CRITICAL.placeholder"].append(f"{label} {name} id {pid} {where}")

for name in CORPUS:
    for p in load(name):
        pid = p["id"]
        scan_text("tafsir", name, pid, p.get("tafsir"), "")
        scan_text("theme", name, pid, p.get("themeTag"), "")
        for v in p.get("verses") or []:
            scan_text("verse", name, pid, v.get("first"), f"pos{v.get('position')}")
            scan_text("verse", name, pid, v.get("second"), f"pos{v.get('position')}")
            scan_text("meaning", name, pid, v.get("meaning"), f"pos{v.get('position')}")

# meaning truncated? (ends with half-word typical of cut text: ends with " " or ends with "خا" etc)
# detect meaning that ends abruptly with a comma or space
trunc = 0
for name in CORPUS:
    for p in load(name):
        for v in p.get("verses") or []:
            m = v.get("meaning")
            if m and str(m).rstrip().endswith(("،", ",", " ", "(", "«", "از", "که", "و", "به")):
                trunc += 1
stats["meanings_suspicious_end"] = trunc

# ---------- 6. Arabic script ratio in MEANINGS (should be clean Persian) ----------
ar_cnt = 0
per_cnt = 0
for name in CORPUS:
    for p in load(name):
        for v in p.get("verses") or []:
            m = v.get("meaning") or ""
            for c in m:
                if c in "يكةأإٱىھ":
                    ar_cnt += 1
                if c in "یک":
                    per_cnt += 1
stats["arabic_letters_in_meanings"] = ar_cnt
stats["persian_k_in_meanings"] = per_cnt

# ---------- 7. stories structure ----------
for p in load("stories"):
    pid = p["id"]
    for v in p.get("verses") or []:
        if v.get("second") is not None and str(v.get("second")).strip():
            issues["WARN.story_has_second"].append(f"id {pid}")
        if not (v.get("meaning") or "").strip():
            issues["CRITICAL.story_no_lesson"].append(f"id {pid}")

# ---------- 8. hafez specifics ----------
hafez = load("hafez")
if len(hafez) != 495:
    issues["CRITICAL.hafez_count"].append(f"expected 495, got {len(hafez)}")
for p in hafez:
    if p["collection"] != "ghazal":
        issues["WARN.hafez_collection"].append(f"id {p['id']}: {p['collection']}")
stats["hafez_tafsir_avg"] = round(sum(len(p['tafsir']) for p in hafez)/len(hafez), 1)

# ---------- 9. duplicate tafsir texts ----------
tafsir_map = collections.defaultdict(list)
for name in CORPUS:
    for p in load(name):
        t = normalize(p.get("tafsir") or "")
        if t:
            tafsir_map[t].append((name, p["id"]))
for t, locs in tafsir_map.items():
    if len(locs) > 1:
        issues["WARN.dup_tafsir"].append(f"{t[:50]!r} on {locs[:5]}")

# ---------- 10. known typos in user-facing modern text ----------
TYPOS = [("خافظ", "حافظ"), ("حافط", "حافظ"), ("سعدي", "سعدی"), ("مولوي", "مولوی"),
         ("خيام", "خیام"), ("ميشود", "می‌شود")]
for typo, fix in TYPOS:
    cnt = 0
    examples = []
    for name in CORPUS:
        for p in load(name):
            for field in ("tafsir",):
                txt = p.get(field) or ""
                if typo in txt:
                    cnt += 1
                    examples.append(f"{name} id {p['id']}")
            for v in p.get("verses") or []:
                m = v.get("meaning") or ""
                if typo in m:
                    cnt += 1
                    if len(examples) < 5:
                        examples.append(f"{name} id {p['id']} pos {v.get('position')}")
    if cnt:
        issues["WARN.typo"].append(f"{typo}->{fix}: {cnt} occurrences e.g. {examples[:5]}")

# ---------- report ----------
print("=" * 70)
print("DEEP CONTENT AUDIT — corpus statistics")
print("=" * 70)
for k in sorted(stats):
    print(f"  {k:40s} {stats[k]}")

order = ["CRITICAL", "WARN"]
print()
print("=" * 70)
print("ISSUES")
print("=" * 70)
critical = 0
warn = 0
for level in order:
    for k in sorted(issues):
        if k.startswith(level):
            n = len(issues[k])
            print(f"\n[{k}]  -> {n} occurrences")
            for item in issues[k][:8]:
                print("    " + str(item)[:200])
            if n > 8:
                print(f"    ... and {n-8} more")
            if level == "CRITICAL":
                critical += n
            else:
                warn += n
print()
print(f"TOTAL CRITICAL={critical}  WARN={warn}")
sys.exit(1 if critical else 0)
