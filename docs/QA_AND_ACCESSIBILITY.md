# QA & Accessibility notes (Phase 7)

## Accessibility
- **RTL**: the whole app forces `LayoutDirection.Rtl`; Persian text is right-aligned everywhere.
- **Font scaling**: a Settings slider (0.85×–1.4×) scales all `sp`-based text app-wide via `LocalDensity`.
- **Typography**: high-contrast cream-on-navy; the light "Minimal" theme uses dark ink on cream.
- **Touch targets**: primary actions are ≥48dp (IconButtons) or large padded buttons.
- **Content descriptions**: Persian labels on all icon-only controls (settings, favorite, share, library, back, search).
- **Contrast**: gold accents are used for decoration only; body text keeps ≥ 4.5:1 contrast.

## Performance (low-end devices)
- All animations are Compose `Canvas`/`Animatable` (no heavy per-frame allocations); the Divan-opening runs once per draw.
- `RitualBackground` decodes theme artwork once (`remember`) and skips its infinite transition when particles are off (list/detail screens).
- The Room FTS search is debounced (250 ms) and returns quickly on the bundled 60-poem corpus.
- Share-image rendering runs on `Dispatchers.Default` and is capped at 6 beits.

## Known intentional choices
- **No sound assets** — the reveal is silent by design (the prompt allows omitting the ambient-sound toggle).
- **Ads never appear** during the niyyat→draw→reveal→interpretation ritual; the banner shows on the calm
  niyyat/home screen, History and Library; the interstitial is frequency-capped (every 4th draw) and only
  fires on returning home; rewarded video unlocks extra draws; one labeled native unit sits in the Library list.
- **Offline degradation**: with no network, `AdManager` simply does nothing; every core feature keeps working.

## Manual QA checklist
- [ ] First launch → splash → onboarding → main (skip works).
- [ ] Niyyat: question field + category chips → draw → Divan animation → line-by-line reveal → tafsir → action bar.
- [ ] Favorite toggle persists after app restart (Room).
- [ ] History shows date (Jalali), question and verse; opens detail.
- [ ] Library: poets → collections → poems → detail; search finds a verse (e.g. «بنی آدم»).
- [ ] Share-as-image produces a 1080×1350 PNG and opens the share sheet.
- [ ] Theme switch (تذهیب/شمع/باغ/مینیمال) reskins the draw flow.
- [ ] Font-size slider scales all text live.
- [ ] Daily reminder toggle requests notification permission (API 33+) and schedules WorkManager.
- [ ] Airplane mode: everything works except ads (which silently hide).
