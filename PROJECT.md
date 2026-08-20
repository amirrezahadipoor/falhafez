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

## Corpus (complete, offline)
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
