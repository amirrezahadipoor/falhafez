# Tapsell integration guide (mediation with AdMob)

The app monetizes behind the `AdManager` abstraction (`data/ads/AdManager.kt`), so the
ad network can be swapped or mediated **without touching any UI code**. The buildable
reference implementation is `AdMobAdManager`; Tapsell is added as the primary network
for the Iranian market as follows.

## 1. Add the Tapsell SDK dependency

Tapsell distributes its SDK via a **private Maven repository** that requires your
account key. In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://raw.githubusercontent.com/tapsellorg/TapsellSDK-Android/master/Repository")
            credentials {
                username = "your-tapsell-key"   // from the Tapsell dashboard
                password = "your-tapsell-token"
            }
        }
    }
}
```

And in `gradle/libs.versions.toml` + `app/build.gradle.kts`:

```toml
tapsell = "4.9.2"   # confirm the current version in the Tapsell docs
```

```kotlin
implementation("ir.tapsell.sdk:tapsell-sdk-android:4.9.2")
```

## 2. Configure the app id

Add your Tapsell app key to the manifest `<application>`:

```xml
<meta-data android:name="TAPSELL_APP_KEY" android:value="YOUR_TAPSELL_APP_KEY" />
```

## 3. Implement the adapter

Create `data/ads/TapsellAdManager.kt`:

```kotlin
@Singleton
class TapsellAdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val frequencyPolicy: AdFrequencyPolicy
) : AdManager {

    override val enabled = true

    override suspend fun isNetworkAvailable(): Boolean { /* as in AdMobAdManager */ }

    override suspend fun onDrawCompleted() = frequencyPolicy.onDrawCompleted()

    override suspend fun showInterstitial(activity: Activity): Boolean {
        if (!isNetworkAvailable()) return false
        if (!frequencyPolicy.shouldShowInterstitial()) return false
        // Tapsell interstitial is request-based:
        val responseId = suspendCancellableCoroutine<String?> { cont ->
            Tapsell.showBannerAd(...) // replace with TapsellPlus interstitial API
        }
        return true
    }

    override suspend fun showRewarded(activity: Activity, onReward: () -> Unit): Boolean {
        // TapsellPlus.showRewardedVideoAd(context, responseId, onReward)
        return true
    }
}
```

> The exact Tapsell v4 API differs by version (Tapsell "new" SDK vs TapsellPlus).
> Follow the current SDK docs for the correct rewarded/interstitial call signatures,
> and keep the same `AdManager` contract above.

## 4. Switch the binding

In `di/AdModule.kt`, either bind `TapsellAdManager` directly (primary network) or
wrap both in a `MediatingAdManager` that tries Tapsell first and falls back to AdMob
for additional fill:

```kotlin
@Binds @Singleton
abstract fun bindAdManager(impl: MediatingAdManager): AdManager
```

## Placement summary (already wired in UI)

| Placement   | Location                          | Rule                                |
|-------------|-----------------------------------|-------------------------------------|
| Banner      | Home (niyyat), History, Library   | never during ritual/reveal          |
| Interstitial| after dismissing a fal result     | every 4th draw (offline cap)        |
| Rewarded    | extra draws beyond daily limit    | also theme unlocks / cooldown skip  |
| Native      | Library list (after 4th item)     | labeled "حمایت‌شده" (sponsored)     |

---

## نکتهٔ مهم برای بازار ایران (کافه‌بازار)

AdMob به **Google Play Services** وابسته است که روی بیشتر گوشی‌های ایرانی نصب نیست؛
بنابراین برای درآمد واقعی در ایران، **Tapsell باید شبکهٔ اصلی باشد**. اپلیکیشن طوری طراحی
شده که نبودِ Play Services هیچ خطایی ایجاد نکند (`FalHafezApp` → `runCatching`) و تبلیغات
به‌سادگی نمایش داده نشود تا وقتی Tapsell وصل شود.

الگوی مدیتیشن (اول Tapsell، بعد AdMob):

```kotlin
@Singleton
class MediatingAdManager @Inject constructor(
    private val tapsell: TapsellAdManager,   // شبکهٔ اصلی (ایران)
    private val admob: AdMobAdManager        // پشتیبان (جایی که Play Services هست)
) : AdManager {
    override val enabled = true
    override suspend fun isNetworkAvailable() = tapsell.isNetworkAvailable()
    override suspend fun showInterstitial(activity: Activity) =
        tapsell.showInterstitial(activity) || admob.showInterstitial(activity)
    override suspend fun showRewarded(activity: Activity, onReward: () -> Unit) =
        tapsell.showRewarded(activity, onReward) || admob.showRewarded(activity, onReward)
    override suspend fun onDrawCompleted() { tapsell.onDrawCompleted(); admob.onDrawCompleted() }
}
```

سپس در `di/AdModule.kt` فقط بایندینگ را عوض کنید:
```kotlin
@Binds @Singleton abstract fun bindAdManager(impl: MediatingAdManager): AdManager
```
