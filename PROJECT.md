# Project status — فال حافظ | تعبیر هوشمند (Fal-e Hafez)

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
| 13 — Full divans + stories + sound | ✅ | **Complete Saadi (1,158) + Rumi (4,246)** — 6,177 poems total; **50 self-awareness & cosmos reads** («جهان» — non-religious, sourced from world science books); 10 visual themes; synthesized ritual sounds (buttons/draw/reveal/ambient) + haptics toggles; release-only CI |
| 14 — Scroll-free layout | ✅ | Whole-app redesign to minimize scrolling: single-screen niyyat ritual, pinned bottom action bars, 2-column grids, dense history, tabbed settings |
| 15 — Polish + release | ✅ | Fal-source selector (حافظ/سعدی/مولانا/خیام/همه) with live poem counts; font size + font COLOR presets; complete Rumi rubaiyat (1,994) + Saadi minor collections — **8,515 poems**; generated release keystore + random secrets; CI signs release when secrets present |
| 16 — Branding + Bazaar | ✅ | App name «فال حافظ | تعبیر هوشمند»; unique package `ir.siliksama.falhafez`; Tapsell registration guide + ready-to-copy info sheet |

| 17 — Support + channel + Tapsell | ✅ | 3 financial-support tiers (100/300/490k Toman) → permanent ad removal + perks; user social-channel (icon + link + promo-image generator) promoted on every shared fal; "other apps by developer" banners in Settings; **AdMob fully removed**, Tapsell prepared |
| 18 — Size + universal | ✅ | corpus gzipped, Vazirmatn subset, theme JPEGs recompressed, WAVs downsampled; **universal release APK only**; flat Bazaar icon (34KB) |
| 19 — Tapsell live + signing | ✅ | **Tapsell Mediation SDK 1.3.0** (real key + 4 placements, auto-init); **release APK signed with the real keystore** via GitHub Secrets |
| 20 — Zones + update check | ✅ | All 5 Tapsell zone IDs wired; in-app Cafe Bazaar update check (manual + once-a-day auto) |
| 21 — Poolakey + support heart | ✅ | Poolakey 2.0.0 (Bazaar IAP), SKUs fal_support_base/plus/gold; beating-heart «حمایت مالی» on Home; version 1.0.0 |
| 22 — Polish + fixes | ✅ | rotating settings gear; share image shows beits + full tafsir; compact settings tabs; golden fading scrollbar; ad-removal bug fixed; corpus validated |
| 23 — Freemium draws | ✅ | **2 free draws/day**; beyond that each draw = rewarded video; **subscribers unlimited** |
| 24 — Bug pass + ads schedule | ✅ | rewarded-ad failure toast, midnight quota refresh, Locale.US formatting, guarded hydration; docs: ad schedule + gap analysis |
| 25 — Value pass | ✅ | **3-page onboarding**; **Masnavi beit meanings restored**; **unit tests** (Jalali + PersianText) in CI |
| 26 — Full content + free themes | ✅ | **All 13 themes free**; **complete beit meanings for whole corpus (99%)**; **Hafez tafsirs longer + motif-grounded** |
| 27 — Supporter share card | ✅ | Tier perks rewritten; **supporter name+channel shown on share template only for PLUS/GOLD** (gold-framed card); GOLD badge; instant-draw GOLD-only |
| 28 — Back navigation + fixes | ✅ | **System back button wired app-wide**; **widget now refreshes daily**; deep content scan (0 issues) |
| 29 — Share tafsir + home fix | ✅ | **Share template budget-based layout** — full tafsir always visible; **navigation-bar insets** on Settings/Onboarding |
| 30 — Transparent icons + subscriber value | ✅ | Social-channel icons de-whitened; **exclusive «طلایِ حامیان» theme**; **supporter badge** on Home heart |
| 31 — Read marks + fixes | ✅ | **«خوانده‌شده» markers** (Room v3) — auto-mark + toggle + badges; **stories/library empty bug fixed** (load retry); **draw sound louder**; **فال دوباره centered** |
| 32 — Deep bug hunt | ✅ | back button no longer **exits the app during the Divan animation** (was unhandled); "همه" draw fallback can no longer return a story; source-count zero-guard at first launch; content scan 0 issues |

## Corpus (complete, offline) — 8,515 poems / 84,506 beits
> Hafez 692 (غزل 495 + قطعه 34 + رباعی 42 + قصیده 3 + منتسب 118) · Khayyam 178 · Saadi 1,355 (غزلیات 637 + گلستان 291 + بوستان 230 + رباعیات 146 + قطعات 20 + ملحقات 31) · Rumi 6,240 (مثنوی 972 + شمس 3,274 + رباعیات 1,994) · + ۵۰ مطلبِ «جهان»
> Saadi: غزلیات (637) + گلستان (291) + بوستان (230) · Rumi: مثنوی (972) + دیوان شمس (3,274) · Hafez 692 · Khayyam 178 · + ۵۰ مطلبِ «جهان»

## Corpus — measured (tools/validate_content.py)
| Poet | Poems | Beits |
|------|-------|-------|
| حافظ | 692 | 5,179 |
| خیام | 178 | 356 |
| سعدی | 1,355 | 12,917 |
| مولانا | 6,240 | 65,954 |
| «جهان» | 50 | 100 |
| **جمع** | **8,515** | **84,506** |

## Content provenance
- Poem texts: **Ganjoor** (public-domain classical Persian corpus), fetched verbatim — verse text,
  numbering and eraab are Ganjoor's scholarly editions, not hand-typed.
- Interpretations (tafsir): original. Hand-written for the famous poems; for the rest, composed by
  `tools/tafsir_engine.py` **from each poem's own focal verse, motifs and detected theme** — every
  one of the 8,515 interpretations is unique (verified in CI). The earlier three-slot template
  (872 unique bodies across 8,465 poems) has been fully retired.
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
app/src/main/java/ir/falhafez/tabir/
├── core/          # design system, typography, RTL/Jalali utils, theme data
├── data/          # Room entities/DAOs, repositories, settings, ads (AdManager)
├── domain/        # models, repository contracts, use-cases
├── di/            # Hilt modules (app + ads)
└── presentation/  # navigation + features (home, history, favorites, library, settings,
                   # onboarding, splash, share, notifications, ads)
```
