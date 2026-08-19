# Project status — فال حافظ (Fal-e Hafez)

Built in phases per the master prompt. Each phase is committed separately.

## Phase 1 — Foundation ✅ (commit: `feat(phase1): …`)
- Kotlin + Jetpack Compose (Material 3) project, Gradle Kotlin DSL, version catalog.
- Min SDK 23, target/compile 35, Kotlin 2.0.21, AGP 8.5.2, Hilt, Room (FTS4), DataStore, WorkManager, Lottie, kotlinx.serialization.
- MVVM + UDF (StateFlow), Clean layering: `core/ data/ domain/ di/ presentation/`.
- Forced RTL + Persian typography: Vazirmatn (reading) + Noto Nastaliq Urdu (display), bundled in `res/font` (OFL fonts).
- Bespoke dark gold/navy design system (`FalThemeSpec`, `FalText`, `RitualBackground`).
- Room schema: `poems`, `verses`, `poems_fts` (FTS4/unicode61), `draws`, `favorites`.
- Offline seeding pipeline (`CorpusSeeder` → assets JSON → Room).
- Navigation: Splash → Onboarding → Main (tabs: فال / تاریخچه / دیوان / علاقه‌مندی‌ها) + Settings.
- Working draw loop: niyyat field + category chips → weighted-random fal (recent 30 excluded) → reveal + interpretation + favorite.

## Phase 2 — Content pipeline ✅ (commit: `feat(phase2): …`)
- Researched scholarly-verified texts from **Ganjoor** (public-domain classical Persian corpus).
- Seeded **60 poems**: 27 Hafez ghazals, 16 Khayyam rubaiyat, 10 Saadi (ghazal/golestan/bustan), 7 Rumi (masnavi/shams).
- Every poem carries a hand-written modern-Persian **tafsir** + **theme tag**.
- Long masnavi/golestan sections trimmed to "selected verses" (the famous passages).
- Corpus ships as 4 offline JSON assets (`assets/corpus/*.json`).

> Note: the full Divan-e Hafez (~495 ghazals) is not yet included — the seed set is a curated
> representative collection of the most famous poems. The pipeline supports dropping in the
> complete corpus later without code changes.

## Phase 3 — Hero draw flow (next)
- Niyyat screen → "opening of the Divan" animation → line-by-line reveal → interpretation beat → action bar.

## Phase 4 — Theming system + generated artwork
## Phase 5 — History, Favorites, Library/search, Share-as-image, Settings, Notifications
## Phase 6 — Monetization (Tapsell/AdMob behind `AdManager`)
## Phase 7 — Polish & QA

## Build
Android Studio (Jellyfish+) + JDK 17. Open project root, sync, run `app`.
