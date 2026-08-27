# Dicey Dicey

A lightweight Android dice rolling app built with Kotlin and Jetpack Compose.

The app has a single purpose: simulate rolling a standard six-sided die with a smooth animation and a fair random outcome. There are no menus, settings, advertisements, accounts or unnecessary features—just tap the die to roll.

The project is intentionally small and is designed as an exercise in writing clean, maintainable Android code with a clear separation between domain logic and UI.

---

## Features

- Roll a standard six-sided die
- Smooth rolling animation
- Uniform random outcomes (1–6)
- Tap the die repeatedly with no cooldown
- Programmatically rendered die faces (no image assets)
- Minimal user interface

---

## Technology

| Component | Technology |
|----------|------------|
| Language | Kotlin |
| UI | Jetpack Compose |
| Platform | Android |
| Build | Gradle |
| Minimum SDK | *(Specify when chosen)* |

The application uses only standard Android and AndroidX libraries. No third-party dependencies are required.

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
 │   │   └── animation/
 │   └── test/
 │
docs/
 ├── FEATURES.md
 ├── ARCHITECTURE.md
 └── IMPLEMENTATION-PLAN.md
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

---

## Documentation

Project documentation is split into separate documents.

| Document | Purpose |
|----------|---------|
| README.md | Project overview |
| FEATURES.md | Functional requirements |
| ARCHITECTURE.md | Software architecture |
| IMPLEMENTATION-PLAN.md | Step-by-step implementation roadmap |

Keeping these documents separate helps ensure the implementation remains aligned with the design.

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