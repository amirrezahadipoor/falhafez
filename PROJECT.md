# Project status — فال حافظ (Fal-e Hafez)

Built in phases per the master prompt. Each phase is committed separately on `main`.

| Phase | Status | Commit subject |
|-------|--------|----------------|
| 1 — Foundation | ✅ | Compose app, RTL Persian design system, Room+Hilt+DataStore, offline seeding, draw loop |
| 2 — Content pipeline | ✅ | 60 Ganjoor-verified poems (Hafez/Saadi/Rumi/Khayyam) with interpretations, offline assets |
| 3 — Hero flow | ✅ | niyyat → animated Divan-opening → line-by-line reveal → interpretation → action bar |
| 4 — Theming | ✅ | 4 original generated artworks wired into `FalThemeSpec` + theme picker |
| 5 — Secondary features | ✅ | History, Favorites, Library/FTS search, PoemDetail, share-as-image, full Settings, daily reminder |
| 6 — Monetization | ✅ | `AdManager` abstraction + AdMob + offline frequency cap + Tapsell mediation guide |
| 7 — Polish | ✅ | inset/transition/perf fixes, accessibility + QA checklist |

## Content provenance
- Poem texts: **Ganjoor** (public-domain classical Persian corpus), fetched verbatim — verse text,
  numbering and eraab are Ganjoor's scholarly editions, not hand-typed.
- Interpretations (tafsir): original, written for this app.
- Artwork: original, AI-generated for this app (no copyrighted/museum/historical-manuscript imagery).
- Fonts: **Vazirmatn** + **Noto Nastaliq Urdu** (both OFL).

## Notes / flags
- The seeded set is a curated **60-poem** collection (27 Hafez, 16 Khayyam, 10 Saadi, 7 Rumi);
  the full ~495-ghazal Divan can be dropped in via `assets/corpus/hafez.json` with no code changes.
- **Build**: Android Studio Jellyfish+ / JDK 17. Tapsell needs its private Maven key (see `docs/TAPSELL_INTEGRATION.md`).

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
