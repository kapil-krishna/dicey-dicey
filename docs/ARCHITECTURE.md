# Dicey Dicey Architecture

This is the single source of truth for architecture rules and contracts. `AGENTS.md` does not
duplicate these rules — it only points here.

---

# Architectural Principles

## Keep Logic Independent

Domain code must not depend on Android Context, Activities, Composables, Android lifecycle APIs,
or coroutines/timers. It must be plain Kotlin, callable and testable from a JVM unit test with no
Android framework on the classpath.

## Prefer Simplicity

Do not introduce:

- Dependency injection frameworks
- Extra layers without a clear purpose
- Generic/abstract frameworks
- Interfaces with only one implementation
- `object` singletons for anything that holds mutable state (see Dice Engine below — this is
  what broke last time)

A simple, slightly duplicated solution beats a clever abstract one.

---

# Component Structure

```
app/src/main/java/.../
├── domain/       — dice rules, state, pure logic. No Android imports.
├── animation/    — rolling animation timing and visual sequencing.
├── ui/           — Compose screens/components, touch handling.
└── MainActivity.kt
```

---

# Domain Layer

The domain layer has exactly two responsibilities: decide what number was rolled, and track
whether the die is currently rolling. It has fixed, exact contracts below — implement these
signatures as given so later phases and tests can rely on them without re-deriving the design.

## DiceRoller

Stateless. An `object` is fine here — it holds no state, so a singleton causes no test isolation
problems.

```kotlin
object DiceRoller {
    fun roll(random: Random = Random.Default): Int // returns 1..6, uniform
}
```

## DieFace

Represents which of a traditional die's faces is showing, and which dots that face displays.
This is still domain, not UI: it answers "which of the 9 grid cells are filled," not "how many
pixels." Drawing pixels is Phase 6's job (UI layer).

```kotlin
enum class DieFace(val pips: Int) {
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6);

    companion object {
        fun fromInt(value: Int): DieFace // throws or clamps on values outside 1..6 — pick one and be consistent
    }
}

// 3x3 grid, outer list = rows (top to bottom), inner list = columns (left to right).
// true = a dot is present at that cell.
fun DieFace.dotGrid(): List<List<Boolean>>
```

Requirement: layouts must match a traditional die (opposite faces sum to 7: 1↔6, 2↔5, 3↔4).

## DiceState

```kotlin
sealed interface DiceState {
    data class Idle(val faceValue: Int) : DiceState
    data class Rolling(val target: Int) : DiceState
}
```

`Idle` is the settled state showing the current face. `Rolling` carries the *target* result the
engine already picked — the domain layer decides the outcome the instant a roll starts, not at
the end of the animation. What the UI displays while `Rolling` is true (e.g. a cycling/shaking
visual) is a UI concern and does not need to match `target` until the state flips back to `Idle`.

## DiceEngine

Holds mutable state, so it **must be an instantiable class**, never an `object`. Each test gets
its own instance; the UI layer owns exactly one instance (see UI Layer below).

```kotlin
class DiceEngine(initialFace: Int = 1) {
    var state: DiceState = DiceState.Idle(initialFace)
        private set

    // No-op (returns false) if already Rolling. Otherwise picks a result immediately
    // and moves to Rolling. Returns true if a roll actually started.
    fun startRoll(random: Random = Random.Default): Boolean

    // No-op if not currently Rolling. Otherwise moves Rolling -> Idle(target).
    fun completeRoll()
}
```

This is deliberately synchronous and has no concept of time. The engine does not know a roll
takes ~5 seconds — it only knows two states and the transitions between them. This is what keeps
domain tests instant and flake-free (no coroutines, no `delay`, no `runTest`).

---

# Decision: who owns the ~5-second timing?

This was left ambiguous in the original docs and is what produced the broken, self-contradictory
state code in the first attempt. It's resolved now:

**The animation/UI layer owns timing. The domain layer never does.**

Flow:

1. User taps → UI calls `diceEngine.startRoll()` unconditionally, every tap, with no UI-side
   "am I already rolling" flag of its own. If a roll is already in progress, `startRoll()` is a
   no-op and returns `false`; the UI does nothing further. This keeps the single source of truth
   for "can a roll start" in one place (the engine) instead of duplicating it in UI state.
2. If a roll started, the UI/animation layer runs a `LaunchedEffect(state)` (or equivalent) that
   plays the shake animation for ~5 seconds.
3. When that effect finishes, it calls `diceEngine.completeRoll()`, which flips `Rolling -> Idle`
   and the UI recomposes to show the final face.

The domain layer is a pure state machine driven entirely by two method calls. The animation
layer decides when the second call happens. Neither layer needs to know how to do the other's
job.

---

# Animation Layer

Responsibilities:

- Own the ~5 second roll duration (the single place this constant lives).
- Drive the visual shake while `state is DiceState.Rolling`.
- Call `diceEngine.completeRoll()` when the animation finishes.
- Provide a smooth transition into the settled face.

Must not:

- Decide what number is rolled (that's already decided by `startRoll()`, held in `Rolling.target`).
- Contain dice rules.

---

# UI Layer

Responsibilities:

- Own a single `DiceEngine` instance for the screen's lifetime (e.g. `remember { DiceEngine() }`).
- Render the current `DiceState` (via `DieFace.dotGrid()` for the settled face, or a shake visual
  while `Rolling`).
- Forward taps to `diceEngine.startRoll()` unconditionally (see Decision above).
- Contain no dice rules and no random number generation.

---

# Data Flow

```
User taps
  -> UI calls diceEngine.startRoll()               [domain: picks result, sets Rolling(target)]
  -> UI recomposes on Rolling, animation layer starts a ~5s shake
  -> animation layer finishes, calls diceEngine.completeRoll()  [domain: Rolling -> Idle(target)]
  -> UI recomposes on Idle, shows final face
```

---

# Testing Strategy

## Domain tests (JVM unit tests, no Android, no coroutines)

- `DiceRoller.roll()` always returns 1..6.
- Repeated rolls are not all identical (basic fairness sanity check — see
  `IMPLEMENTATION-PLAN.md` Phase 2 for the exact non-flaky approach).
- `DieFace.fromInt()` covers 1..6 and each `dotGrid()` matches a traditional layout.
- `DiceEngine`: initial state is `Idle`. `startRoll()` moves to `Rolling` and returns `true`.
  `startRoll()` while already `Rolling` returns `false` and does not change `target`.
  `completeRoll()` moves `Rolling(target)` to `Idle(target)`. `completeRoll()` while `Idle` is a
  no-op.

## UI/animation

Not unit tested in v1. Verified manually per `FEATURES.md`'s acceptance criteria (run the app,
tap the die, watch it work).

---

# Dependencies

Kotlin standard library, Android SDK, and the AndroidX/Compose libraries already in
`gradle/libs.versions.toml`. No new dependency without updating this doc first.

---

# Target Platform

- `minSdk = 30`, `targetSdk` / `compileSdk = 37` (already set in `app/build.gradle.kts` — this is
  the actual, decided baseline; nothing further to resolve here).
