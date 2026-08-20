# Project status — فال حافظ (Fal-e Hafez)

Built in phases per the master prompt. Each phase is committed separately on `main`.

| Phase | Status | Commit subject |
|-------|--------|----------------|
| 1 — Foundation | ✅ | Compose app, RTL Persian design system, Room+Hilt+DataStore, offline seeding, draw loop |
| 2 — Content pipeline | ✅ | Ganjoor-verified poems with interpretations, offline assets |
| 3 — Hero flow | ✅ | niyyat → animated Divan-opening → line-by-line reveal → interpretation → action bar |
| 4 — Theming | ✅ | 4 original generated artworks wired into `FalThemeSpec` + theme picker |
| 5 — Secondary features | ✅ | History, Favorites, Library/FTS search, PoemDetail, share-as-image, full Settings, daily reminder |
| 6 — Monetization | ✅ | `AdManager` abstraction + AdMob + offline frequency cap + Tapsell mediation guide |
| 7 — Polish | ✅ | inset/transition/perf fixes, accessibility + QA checklist |
| 8 — Full corpus | ✅ | **Complete Divan-e Hafez (495 ghazals) + complete Ruba'iyat of Khayyam (178)**, expanded Saadi/Rumi |
| 9 — Social share + polish | ✅ | Direct share to Telegram/WhatsApp/Rubika/Bale/Instagram/Eitaa/Soroush + gallery save; animated shimmer buttons, SVG-style ornaments; humanized, poet-aware interpretations |
| 10 — Retention & monetization | ✅ | beit-by-beit meaning (4,548 beits), deterministic daily fal (فالِ روز), home-screen widget, personal stats/streak (کارنامه), theme filter + study mode, rewarded cooldown-skip & premium-theme unlock (نوروز + یلدا), haptics, copy-text, JSON export |
| 11 — CI/CD + store | ✅ | GitHub Actions builds **debug + release APK** (release 6.5 MB, R8), emulator screenshot workflow, Cafe Bazaar assets (icon 512, descriptions, checklist), Persian privacy policy on GitHub Pages |
| 12 — Compile pass | ✅ | First real compile on GitHub runners — fixed 8 Kotlin/Room issues (FTS4 rowid, imports, smart casts, signing types); CI is **green** |
| 13 — Full divans + stories + sound | ✅ | **Complete Saadi (1,158) + Rumi (4,246)** — 6,177 poems total; **100 instructive stories** from Golestan; 10 visual themes; synthesized ritual sounds (buttons/draw/reveal/ambient) + haptics toggles; release-only CI |

## Corpus (complete, offline) — 6,177 poems
> Saadi: غزلیات (637) + گلستان (291) + بوستان (230) · Rumi: مثنوی (972) + دیوان شمس (3,274) · Hafez 495 · Khayyam 178 · + 100 داستان آموزنده

## Corpus (previous) — for reference
| Poet | Collection | Count | Beits |
|------|------------|-------|-------|
| حافظ | غزلیات — **کامل** | 495 | 4,193 |
| خیام | رباعیات — **کامل** | 178 | 356 |
| سعدی | گلستان + بوستان + غزلیات (گزیده) | 21 | 224 |
| مولانا | مثنوی + دیوان شمس (گزیده) | 8 | 112 |
| **جمع** | | **702** | **4,885** |

## Content provenance
- Poem texts: **Ganjoor** (public-domain classical Persian corpus), fetched verbatim — verse text,
  numbering and eraab are Ganjoor's scholarly editions, not hand-typed.
- Interpretations (tafsir): original — **hand-written for the famous poems**, and theme-aware
  generated prose (warm, varied, non-horoscope tone) for the rest, so every poem has one.
- Artwork: original, AI-generated for this app (no copyrighted/museum/historical-manuscript imagery).
- Fonts: **Vazirmatn** + **Noto Nastaliq Urdu** (both OFL).

## Social share (Phase 9)
- Share sheet with **direct buttons** to Telegram, WhatsApp, **Rubika**, **Bale**, Instagram, Eitaa and Soroush
  (targeted intents by package name, graceful fallback to the generic chooser) + save-to-gallery + "more…".
- Share image is rendered once (off the main thread) as a 1080×1350 PNG.

## Visual polish (Phase 9)
- Animated gold CTA (shimmer sweep + press-scale spring + breathing glow), ghost buttons with press feedback.
- Original vector ornaments (khatam 8-point star, tazhib corner motif, floral rosette, laurel divider):
  rotating star on splash/onboarding, breathing rosette behind the draw CTA, corner flourishes on cards.

## Retention & monetization strategy (Phase 10)

**Habit / return loops (all offline):**
- **فالِ روز** — a deterministic fal shared by every user on the same day → daily return + "فال امروزت چی اومد؟" word-of-mouth.
- **بیتِ امروز widget** — the day's verse sits on the home screen, one tap back into the app.
- **کارنامهٔ شخصی** — draws, streak (زنجیره), favourite poet/theme, busiest hour → investment in one's own data.
- **Beit-by-beit meaning** for the full Divan — the paywalled feature of competitors, free & offline here.

**Monetization (never interrupting the ritual):**
- Rewarded video: extra draws beyond the daily limit, **skip the repeat cooldown**, and **unlock the premium یلدا theme**.
- Banner (niyyat/History/Library), frequency-capped interstitial (every 4th draw, on return home), one native unit in the Library list.
- New free theme نوروز keeps the visual freshness that drives shares.

## CI / CD (Phase 11)

- `.github/workflows/android.yml` — every push to `main` builds `assembleDebug` + `assembleRelease`
  (JDK 17 + Android SDK on GitHub runners, Gradle caching, corpus validation) and uploads the
  artifact **falhafez-apks** (release APK ≈ 6.5 MB).
- `.github/workflows/screenshots.yml` — manual dispatch; boots an API-33 emulator, installs the app
  and captures real store screenshots.
- Release signing: env vars `KEYSTORE_PATH/KEYSTORE_PASSWORD/KEY_ALIAS/KEY_PASSWORD` (GitHub Secrets);
  falls back to the debug key when absent.

## Cafe Bazaar
- `store/cafebazaar/` — icon 512, short/long description, release checklist.
- Privacy policy (Persian): https://amirrezahadipoor.github.io/falhafez/privacy.html (GitHub Pages).

## Notes / flags
- **Build**: Android Studio Jellyfish+ / JDK 17 (this sandbox has no Android SDK, so APK
  compilation happens on your machine).
- Tapsell needs its private Maven key (see `docs/TAPSELL_INTEGRATION.md`); AdMob is the
  buildable reference network and swapping is a one-line change in `di/AdModule.kt`.

## Structure
```
app/src/main/java/com/amirrezahadipoor/falhafez/
├── core/          # design system, typography, RTL/Jalali utils, theme data
├── data/          # Room entities/DAOs, repositories, settings, ads (AdManager)
├── domain/        # models, repository contracts, use-cases
├── di/            # Hilt modules (app + ads)
└── presentation/  # navigation + features (home, history, favorites, library, settings,
                   # onboarding, splash, share, notifications, ads)
```
