# Dicey Dicey

A lightweight Android dice rolling app built with Kotlin and Jetpack Compose.

The app has a single purpose: simulate rolling a standard six-sided die with a smooth animation and a fair random outcome. There are no menus, settings, advertisements, accounts or unnecessary features—just tap the die to roll.

The project is intentionally small and is designed as an exercise in writing clean, maintainable Android code with a clear separation between domain logic and UI.

---

## Features

- Roll a standard six-sided die
- Smooth 3D rolling animation
- Uniform random outcomes (1–6)
- Tap the die repeatedly with no cooldown
- Die rendered as a real 3D model (see `docs/ARCHITECTURE.md` § 3D Die Model)
- Minimal user interface

---

## Technology

| Component | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Platform | Android |
| Build | Gradle |
| Minimum SDK | 30 (target/compile SDK 37) |
| 3D Rendering | SceneView for Jetpack Compose (Filament), for the die model only |

The application otherwise uses only standard Android and AndroidX libraries. The 3D rendering
library is the one sanctioned exception — see `docs/ARCHITECTURE.md` § Dependencies for why.

---

## Project Goals

This project prioritises:

- Simple architecture
- Readable code
- Clear separation of responsibilities
- Testable business logic
- Minimal dependencies

The codebase is intentionally structured so that the dice logic remains independent of the Android UI wherever practical.

---

## Project Structure

```
app/
 ├── src/
 │   ├── main/
 │   │   ├── java/
 │   │   ├── ui/
 │   │   ├── domain/
 │   │   ├── animation/
 │   │   └── assets/       — the die's .glb 3D model (see ARCHITECTURE.md § 3D Die Model)
 │   └── test/
 │
docs/
 ├── FEATURES.md
 ├── ARCHITECTURE.md
 ├── IMPLEMENTATION-PLAN.md
 └── PROGRESS.md
```

---

## Building

Clone the repository and build using Gradle.

```bash
./gradlew build
```

To install on a connected device:

```bash
./gradlew installDebug
```

`installDebug` signs the app with an auto-generated debug key — fine for testing on your own
device, but not accepted by the Play Store and not meant for distribution.

---

## Release Build

This project does not yet have a signing key set up (`app/build.gradle.kts` has no
`signingConfigs` block). To produce a real, signed release build:

1. **Generate a keystore** (once, ever, for this app). Keep the file and its password safe and
   backed up — losing it means never being able to publish an update under the same identity if
   this app is ever published.
   ```bash
   keytool -genkeypair -v -keystore dicey-dicey.jks -keyalg RSA -keysize 2048 -validity 10000 -alias dicey-dicey
   ```
2. **Create `keystore.properties`** at the project root (already gitignored — never commit
   signing credentials):
   ```properties
   storeFile=/absolute/path/to/dicey-dicey.jks
   storePassword=...
   keyAlias=dicey-dicey
   keyPassword=...
   ```
3. **Add a `signingConfigs` block** to `app/build.gradle.kts` that reads from that file, and
   reference it from the `release` build type. Not present yet — needs to be added when this is
   actually set up.
4. **Build:**
   - `./gradlew assembleRelease` → a signed `.apk` at `app/build/outputs/apk/release/` — for
     sideloading or sharing directly.
   - `./gradlew bundleRelease` → a signed `.aab` at `app/build/outputs/bundle/release/` — the
     format Google Play requires for uploads (not directly installable itself).

---

## Documentation

Project documentation is split into separate documents.

| Document | Purpose |
|----------|---------|
| README.md | Project overview |
| docs/FEATURES.md | Functional requirements + the single acceptance checklist |
| docs/ARCHITECTURE.md | Software architecture, layer contracts, resolved design decisions |
| docs/IMPLEMENTATION-PLAN.md | Step-by-step implementation roadmap (phase work orders) |
| docs/PROGRESS.md | Current status — read this first, update it last, every session |
| AGENTS.md | How an AI agent should work in this repo |

Keeping these documents separate helps ensure the implementation remains aligned with the design.
Each one has exactly one job; none of them duplicate another's content.

---

## Roadmap

Version 1 focuses solely on delivering a polished Android application.

Potential future enhancements include:

- Haptic feedback
- Sound effects
- Theme support
- Roll history
- Additional dice types (D4, D8, D10, D12, D20)

These are intentionally out of scope for Version 1.

---

## License

Apache 2.0

---

## Philosophy

Dicey Dicey is intentionally small.

Rather than demonstrating every Android feature, the goal is to demonstrate good software design, clean architecture, and maintainable Kotlin code while producing a polished user experience.