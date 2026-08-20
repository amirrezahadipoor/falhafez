# فال حافظ | تعبیر هوشمند (Fal-e Hafez)

[![Android CI](https://github.com/amirrezahadipoor/falhafez/actions/workflows/android.yml/badge.svg)](https://github.com/amirrezahadipoor/falhafez/actions/workflows/android.yml)

A Persian-language **native Android** app for the centuries-old Iranian tradition of *fal-e Hafez*: you make a silent wish (نیّت), draw a random poem from the Divan of Hafez, and receive the verse together with a warm modern-Persian interpretation of what it means for your wish. The **complete Divan-e Hafez (495 ghazals)** and **complete Ruba'iyat of Khayyam (178)** ship offline, plus selected verses from **Saadi** and **Rumi (Molana)**.

> ✅ **All seven phases complete** (see [PROJECT.md](PROJECT.md) and [docs/QA_AND_ACCESSIBILITY.md](docs/QA_AND_ACCESSIBILITY.md)).

## Highlights

- **100% offline** core functionality — 702 poems (4,885 beits), interpretations, fonts, artwork and logic all bundled at install time. Only ads and optional update-checks may touch the network.
- **Native Kotlin + Jetpack Compose (Material 3)**, MVVM + Unidirectional Data Flow, Clean Architecture (data / domain / presentation), Room, Hilt.
- **Full RTL + Persian typography** (Vazirmatn for reading, Nastaliq-style display face for verses).
- A genuine **draw ritual**: نیّت → animated "opening of the Divan" → line-by-line verse reveal → interpretation reveal.
- **Share-as-image** with direct buttons to Telegram, WhatsApp, Rubika, Bale, Instagram, Eitaa and Soroush.
- **Beit-by-beit meaning** for the full Divan (free & offline) + a deterministic **daily fal** (فالِ روز) + home-screen widget.
- **Personal stats & streaks** (کارنامه), theme filters, study mode, haptic ritual feedback, and 6 visual themes (incl. نوروز & premium یلدا).
- **Heavy visual polish**: animated shimmer/gold buttons, rotating khatam star, breathing floral rosette and tazhib corner motifs.
- 4+ **visual themes** (تذهیب manuscript, candlelight, starlit garden, modern minimal).
- History, favorites, full searchable Divan browser, **share-as-image**, daily reminder, settings, onboarding.
- **Monetization** behind an `AdManager` abstraction (Tapsell + AdMob mediation), placed so ads never interrupt the ritual.

## Tech stack

Kotlin · Jetpack Compose · Material 3 · Coroutines/Flow · Room (with FTS) · Hilt · Navigation Compose · DataStore · WorkManager · Lottie · Tapsell/AdMob (via abstraction)

## Build

Requires **Android Studio (Jellyfish or newer)** and **JDK 17+**. Open the project root, sync Gradle, run the `app` configuration. Min SDK 23 (Android 6.0), target SDK 35.

## License

Source code: see LICENSE when present. Poem texts are public-domain classical Persian literature sourced from scholarly-verified editions (e.g. Ganjoor). Generated artwork is original.

## Structure

```
app/src/main/java/ir/falhafez/tabir/
├── core/          # design system, typography, RTL utils, theme data
├── data/          # Room entities/DAOs, repositories, asset seeding
├── domain/        # models, repository contracts, use-cases
├── di/            # Hilt modules
└── presentation/  # navigation + feature screens (home, history, favorites, library, settings, onboarding)
```
