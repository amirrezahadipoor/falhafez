#!/usr/bin/env python3
"""
گیرانداختنِ کامنت‌های بلوکیِ ناتمام در سورس‌های کاتلین — پیش از آنکه CI را بشکنند.

## چرا این ابزار لازم شد
برخلافِ جاوا و C، **کامنت‌های بلوکیِ کاتلین تودرتو می‌شوند** (nesting). یعنی این
KDoc کاملاً سالم به نظر می‌رسد ولی نیست:

    /**
     * فایل‌های `assets/corpus/*.dat` را بازسازی کنید.
     */

دنبالهٔ `/*` داخلِ «corpus/*.dat» یک کامنتِ تودرتو باز می‌کند، پس `*/` پایانی فقط
همان تودرتو را می‌بندد و کامنتِ بیرونی **باز می‌ماند** — بقیهٔ فایل بی‌صدا بلعیده
می‌شود. خطایی که کامپایلر می‌دهد («Missing '}'» در خطی ده‌ها سطر پایین‌تر) هیچ
اشاره‌ای به علتِ واقعی ندارد و پیدا کردنش وقت‌گیر است.

این دقیقاً همان چیزی بود که هشت بیلدِ پشتِ‌سرِ هم را شکست.

## روش کار
مثلِ لِکسرِ کاتلین روی فایل راه می‌رود: رشته‌های معمولی و خام (سه‌گانه) و کامنت‌های
تک‌خطی رد می‌شوند، و عمقِ `/*` … `*/` شمرده می‌شود. اگر در پایانِ فایل عمق صفر
نباشد، شمارهٔ خطی که کامنتِ بازِ باقی‌مانده از آن شروع شده گزارش می‌شود.

خروجی: کد ۱ اگر فایلی خراب باشد (تا CI متوقف شود).
"""

import sys
from pathlib import Path

SCAN_DIRS = ["app/src"]
SCAN_SUFFIXES = (".kt", ".kts")


def unbalanced_comment_lines(src: str):
    """شمارهٔ خطِ کامنت‌های بلوکیِ بسته‌نشده. لیستِ خالی یعنی سالم."""
    i, n = 0, len(src)
    depth = 0
    opened_at = []

    while i < n:
        pair = src[i:i + 2]

        # داخلِ کامنت فقط دنبالِ باز/بسته شدن می‌گردیم؛ رشته‌ها آنجا معنا ندارند.
        if depth == 0:
            ch = src[i]

            # رشتهٔ خام
            if src[i:i + 3] == '"""':
                end = src.find('"""', i + 3)
                i = end + 3 if end != -1 else n
                continue

            # رشتهٔ معمولی
            if ch == '"':
                i += 1
                while i < n and src[i] != '"':
                    if src[i] == "\\":
                        i += 1
                    i += 1
                i += 1
                continue

            # کاراکترِ تکی — 'x' یا '\n'
            if ch == "'":
                i += 1
                while i < n and src[i] != "'":
                    if src[i] == "\\":
                        i += 1
                    i += 1
                i += 1
                continue

            # کامنتِ تک‌خطی
            if pair == "//":
                end = src.find("\n", i)
                i = end + 1 if end != -1 else n
                continue

        if pair == "/*":
            depth += 1
            opened_at.append(src.count("\n", 0, i) + 1)
            i += 2
            continue

        if pair == "*/":
            if depth > 0:
                depth -= 1
                opened_at.pop()
            i += 2
            continue

        i += 1

    return opened_at


def main() -> int:
    root = Path(__file__).resolve().parent.parent
    files = [
        p
        for d in SCAN_DIRS
        for p in (root / d).rglob("*")
        if p.suffix in SCAN_SUFFIXES
    ]
    files += [p for p in root.rglob("*.kts") if ".gradle" not in str(p)]

    broken = []
    for path in sorted(set(files)):
        try:
            src = path.read_text(encoding="utf-8")
        except (OSError, UnicodeDecodeError):
            continue
        lines = unbalanced_comment_lines(src)
        if lines:
            broken.append((path.relative_to(root), lines))

    if broken:
        print("❌ کامنتِ بلوکیِ بسته‌نشده پیدا شد.")
        print("   یادآوری: کامنت‌های بلوکیِ کاتلین تودرتو می‌شوند؛ یک «/*»")
        print("   داخلِ متنِ KDoc (مثلاً در مسیری مثل corpus/*.dat) کافی است.\n")
        for path, lines in broken:
            for ln in lines:
                print(f"   {path}:{ln}  ← این کامنت بسته نشده")
        return 1

    print(f"✓ کامنت‌های {len(files)} فایلِ کاتلین متوازن است.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
