# Hexa Connect 🔷

**Build your path. Block theirs.**

Hexa Connect is a modern, polished local 2-player connection strategy game built natively for Android using **Jetpack Compose**, **Material 3**, and pure **Kotlin** game architecture. Inspired by the classical game of Hex, two players race to form an unbroken chain of hexagonal cells connecting their opposing sides of the board.

---

## 🌟 Highlights & Features

- 🎮 **Local 2-Player Pass & Play**: Competitive two-player gameplay on a single device.
  - **Player 1**: Connects **Top ↕ Bottom** edges.
  - **Player 2**: Connects **Left ↔ Right** edges.
- 📐 **9×9 Hexagonal Grid Geometry**: Custom-engineered canvas renderer with smooth touch interactions, hover previews, and placement fill animations.
- 🏆 **Algorithmic Path Detection**: Breadth-First Search (BFS) pathfinding immediately detects and animates the winning connection upon completion.
- 🎨 **Dynamic Themes & Styling**:
  - Light, Dark, and System Theme modes with transparent Edge-to-Edge system bars.
  - Custom board palettes (**Classic** and **Midnight**).
- 🔊 **Zero-Asset Audio Engine**: Generates synthesized sine-wave audio tones directly into runtime cache WAV files played through `SoundPool`—eliminating static media assets to minimize APK size.
- 📲 **Haptics & Accessibility**: Responsive haptic feedback on placements and wins, plus complete **TalkBack** screen reader support with per-cell semantics.
- 📊 **Persistent Statistics**: DataStore-backed persistence tracking games played, player win counts, and shortest winning path.
- ↩️ **Full Turn Controls**: Undo previous moves and restart active matches seamlessly.

---

## 🎮 How to Play

1. **Take Turns**: Players alternate placing one piece on any empty hexagonal cell.
2. **Permanent Placement**: Pieces cannot be moved, captured, or removed once played.
3. **Winning Condition**:
   - **Player 1** wins by forming a continuous path connecting any top rail cell to any bottom rail cell.
   - **Player 2** wins by forming a continuous path connecting any left rail cell to any right rail cell.
4. **No Ties**: Mathematically, a draw is impossible on a Hex board—one player *must* complete a path first.

---

## 🛠️ Architecture & Tech Stack

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose with Material 3 design components
- **Navigation**: Jetpack Navigation Compose
- **State Management**: Jetpack ViewModel, StateFlow, and Coroutines
- **Storage**: Jetpack DataStore Preferences for settings and game stats
- **Audio Engine**: Custom Kotlin WAV generator & Android `SoundPool`
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with Gradle Version Catalog (`libs.versions.toml`)

### 📂 Project Structure

```text
com.example.hexaconnect/
├── audio/            # Runtime synthesized sound generator & SoundPool manager
├── data/             # DataStore repository for settings and statistics
├── game/             # Pure Kotlin game logic, board grid models, and BFS pathfinding algorithm
├── ui/
│   ├── board/        # Hex geometry calculations and custom Compose Canvas renderers
│   ├── components/   # Reusable Compose UI elements, buttons, and tokens
│   ├── screens/      # GameScreen, HomeScreen, ResultScreen, SettingsScreen, HowToPlayScreen, etc.
│   └── theme/        # Material 3 colors, typography, board palettes, and theme definitions
├── HexaConnectApp.kt # Application class initializing repository & audio
└── MainActivity.kt   # Edge-to-edge Activity host
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Ladybug / Meerkat (2024.2.1+) or newer
- **JDK**: JDK 17 (recommended) or JDK 11
- **Android SDK**: Min API 26 (Android 8.0) | Target SDK 37

### Building from Source

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdullah-ateeq/Hexa-Connect.git
   cd Hexa-Connect
   ```

2. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 🧪 Testing

The game logic includes comprehensive unit tests verifying board initialization, cell placement, player switching, illegal move prevention, undo history, and BFS winning path calculation across multiple scenarios.

Run tests via command line:
```bash
./gradlew test
```

---

## 📄 License

```text
Copyright (c) 2025 Abdullah Ateeq

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
