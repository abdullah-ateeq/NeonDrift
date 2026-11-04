# ⚡ NeonDrift

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg?style=flat&logo=android)](https://www.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org/)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Design](https://img.shields.io/badge/Design-Material%203-757575.svg?style=flat&logo=materialdesign)](https://m3.material.io)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-orange.svg)](https://developer.android.com/about/dashboards)
[![Build Status](https://img.shields.io/badge/Tests-17%20Passed-brightgreen.svg)]()

**NeonDrift** is a sleek, minimalist futuristic arcade steering game built natively for Android using **Jetpack Compose** and **Material 3**. Steer your glowing point along a continuously winding, dynamically procedurally generated neon path. Test your reflexes, build distance streaks, and survive as the path progressively narrows and turns become sharper!

---

## 🌟 Key Features

* 🔮 **Procedural Path Generator**: Smooth, organic C1 continuous path geometry synthesized using 4-point **Catmull-Rom splines**.
* ⚡ **Dynamic Difficulty Scaling**: Continuous path narrowing and turn frequency escalation as distance increases.
* 🎵 **Procedural Audio Synthesis**: Zero external audio asset dependencies! Sound effects (clicks, chord milestone chimes, collision rumbles) are synthesized programmatically in real-time using PCM 16-bit `AudioTrack`.
* 📳 **Tactile Haptic Feedback**: Dynamic haptic vibrations integrated via `Vibrator` and `VibrationEffect`.
* 🎨 **Material 3 & Theme Support**: Full support for Dark, Light, and System Default theme modes using custom Neon color palettes (`NeonCyan`, `NeonPurple`, `NeonAmber`, `NeonPink`).
* 📊 **Persistent Statistics**: Best score, max distance, and user preferences persisted using **Jetpack DataStore Preferences**.
* 📱 **Intuitive Drag & Tap Controls**: Fluid left/right dragging or touch controls for smooth precision steering.
* ✈️ **100% Offline & Standalone**: Zero network calls, zero external data tracking, lightweight footprint.

---

## 🎮 Game Screenshots & Workflow

| Splash & Home | Gameplay HUD | Pause Menu | Game Over & Stats | Settings |
| :---: | :---: | :---: | :---: | :---: |
| Splash animation & High Scores | Smooth winding canvas with HUD | In-game pause & navigation | Score summary, streaks & new record badges | Theme toggle, audio controls & stats reset |

---

## 🛠️ Architecture & Tech Stack

NeonDrift follows modern Android development best practices and clean architectural principles:

* **Language**: [Kotlin](https://kotlinlang.org/) (100%)
* **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 components (`Canvas`, `NavHost`, `AnimatedVisibility`)
* **Architecture**: **MVVM (Model-View-ViewModel)** with reactive unidirectional data flow using `StateFlow`
* **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
* **Local Data**: [Jetpack DataStore Preferences](https://developer.android.com/topic/libraries/architecture/datastore)
* **Audio Engine**: Low-latency PCM wave synthesis via `android.media.AudioTrack`
* **Concurrency**: Kotlin Coroutines & `SupervisorJob`
* **Unit Testing**: JUnit 4 (17 unit tests verifying collision math, path generation, scoring, and difficulty curves)

---

## 📂 Project Structure

```text
NeonDrift/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/neondrift/
│   │   │   │   ├── data/                 # Settings models & DataStore repository
│   │   │   │   ├── domain/
│   │   │   │   │   ├── audio/            # SoundSynth & HapticsManager
│   │   │   │   │   ├── engine/           # PathGenerator, CollisionDetector, DifficultyManager, ScoreCalculator
│   │   │   │   │   └── model/            # GameState, PathSegment, DifficultyLevel
│   │   │   │   └── ui/
│   │   │   │       ├── components/       # Custom NeonButton & NeonCard widgets
│   │   │   │       ├── navigation/       # Compose Navigation route definitions
│   │   │   │       ├── screens/          # Splash, Home, Game, Result, Settings, About
│   │   │   │       └── theme/            # Material 3 Neon color scheme & typography
│   │   │   └── res/                      # Icons and launcher graphics
│   │   └── test/                         # Unit tests (PathEngineTest, CollisionAndMovementTest, GameRulesAndDifficultyTest)
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🚀 Getting Started

### Prerequisites

* **Android Studio**: Ladybug (2024.2.1) or newer recommended
* **JDK**: 11 or higher
* **Android SDK**: Compile SDK 37, Minimum SDK 24 (Android 7.0+)

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/NeonDrift.git
   cd NeonDrift
   ```

2. Open the project in **Android Studio**.

3. Sync Gradle dependencies:
   ```bash
   ./gradlew assembleDebug
   ```

4. Run on an Android Emulator or connected physical device.

---

## 🧪 Running Tests

To run the unit test suite:

```bash
./gradlew test
```

Sample output:
```text
> Task :app:testDebugUnitTest
17 passed, 0 skipped, 0 failed
BUILD SUCCESSFUL
```

---

## 📜 License & Credits

Created by **Abdullah Ateeq**. Built with Jetpack Compose, Material 3, and Kotlin.

© 2026 NeonDrift Arcade Studio. All rights reserved.
