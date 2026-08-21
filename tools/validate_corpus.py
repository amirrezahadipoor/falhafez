# -*- coding: utf-8 -*-
"""Corpus integrity + content-quality validation (run in CI before every build).

Checks:
  - valid gzip JSON, unique global ids
  - non-empty verses / tafsir / themeTag
  - no Latin/foreign words leaked into user-facing text (tafsir/verses/meanings)
  - no `***` placeholder verses
  - no AI labels ("AI:", "هوش مصنوعی:", "تصحیح AI:")
  - no duplicated tafsirs (every poem must have a unique interpretation)
  - no control characters except newline (story prose) and ZWNJ (legit Persian)
"""
import gzip, json, re, sys, collections

NAMES = ["hafez", "khayyam", "saadi", "rumi", "stories"]
BASE = "app/src/main/assets/corpus"

LATIN = re.compile(r"[A-Za-zÀ-ÿ]{2,}")
AI_RE = re.compile(r"(^|[^A-Za-z])(AI|ai)[:：]|هوش\s*مصنوعی|تصحیح\s*(AI|ai|ترجمه)")
CTRL = re.compile(r"[\x00-\x08\x0b\x0c\x0e-\x1f\x7f\ufeff\ufffd]")  # allows \n \t and U+200C

errors = []
ids = []
tafsirs = collections.defaultdict(list)
total_poems = 0
total_verses = 0

for name in NAMES:
    p = f"{BASE}/{name}.dat"
    data = json.loads(gzip.open(p, "rt", encoding="utf-8").read())
    total_poems += len(data)
    for x in data:
        pid = x["id"]
        ids.append(pid)
        # required fields
        assert x.get("verses"), f"{name} id {pid}: empty verses"
        assert x.get("tafsir", "").strip(), f"{name} id {pid}: empty tafsir"
        assert x.get("themeTag"), f"{name} id {pid}: empty themeTag"
        tafsirs[x.get("tafsir", "").strip()].append((name, pid))
        total_verses += len(x.get("verses", []))
        # user-facing text hygiene
        for field in ("tafsir",):
            t = x.get(field) or ""
            if LATIN.search(t):
                errors.append(f"{name} id {pid}: latin in {field}: {LATIN.search(t).group(0)!r}")
            if AI_RE.search(t):
                errors.append(f"{name} id {pid}: AI label in {field}")
        for vi, v in enumerate(x.get("verses", [])):
            f = v.get("first") or ""
            if f.strip() == "***":
                errors.append(f"{name} id {pid} v{vi}: *** placeholder verse")
            for field in ("first", "second", "meaning"):
                t = v.get(field) or ""
                if CTRL.search(t):
                    errors.append(f"{name} id {pid} v{vi}: control char in {field}")
                if LATIN.search(t):
                    errors.append(f"{name} id {pid} v{vi}: latin in {field}: {LATIN.search(t).group(0)!r}")
                if AI_RE.search(t):
                    errors.append(f"{name} id {pid} v{vi}: AI label in {field}")

# global uniqueness
if len(ids) != len(set(ids)):
    errors.append("duplicate global poem ids")

dup_tafsir = [(t, l) for t, l in tafsirs.items() if len(l) > 1]
if dup_tafsir:
    errors.append(f"{len(dup_tafsir)} duplicated tafsirs, e.g. {dup_tafsir[0][1]}")

if errors:
    print("CORPUS VALIDATION FAILED:")
    for e in errors[:40]:
        print("  -", e)
    if len(errors) > 40:
        print(f"  ... and {len(errors) - 40} more")
    sys.exit(1)

print(f"corpus OK: {total_poems} poems, {total_verses} verses, {len(set(ids))} unique ids, 0 dup tafsirs")
