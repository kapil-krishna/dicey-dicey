# Dicey Dicey Implementation Plan

Build order for v1. Complete each phase before starting the next. Each phase below is meant to be
readable on its own — you should be able to open just this phase's section, plus the referenced
parts of `ARCHITECTURE.md`, and implement it without re-reading the whole doc set.

The single completion checklist for the whole project lives in `FEATURES.md` ("Acceptance
Criteria") — this plan does not keep a second copy. Current status lives in `PROGRESS.md`, not
here — do not track progress in this file.

Do not introduce features or architecture beyond what the current phase requires.

---

# Phase 1: Project Foundation — DONE

Gradle/Kotlin/Compose configured, app launches, package structure (`domain/`, `ui/`,
`animation/`) exists. No further action needed here.

---

# Phase 2: Dice Roll Generation

## Goal

Pure random result generation, decoupled from everything else.

## Files

- Create `domain/DiceRoller.kt`
- Create `test/domain/DiceRollerTest.kt`

## Contract

Exact signature — see `ARCHITECTURE.md` § Domain Layer § DiceRoller:

```kotlin
object DiceRoller {
    fun roll(random: Random = Random.Default): Int
}
```

## Requirements

- Returns only 1..6.
- Uniform distribution.
- No Android imports.

## Tests

- `roll()` is always in `1..6` (run it many times in a loop, assert the range each time).
- Fairness without flakiness: seed a fixed `Random(seed)` instance, roll it N times (e.g. 600),
  and assert every one of the 6 outcomes appears within a reasonable band (e.g. at least 100/6 *
  0.5). Do **not** assert on unseeded `Random.Default` output for anything beyond range — that's
  what causes flaky tests.
- Two separately-seeded rolls are independent (no correlation asserted beyond "both are in
  range" — don't try to statistically prove independence in a unit test).

## Done when

- File exists, matches the contract above, compiles.
- Tests exist and pass.
- Mark Phase 2 `Done` in `PROGRESS.md` and add a log entry before ending the session.

---

# Phase 3: Die Face Dot Layout

## Goal

Map a face value to which dots are lit, independent of how they're drawn.

## Files

- Create `domain/DieFace.kt`
- Create `test/domain/DieFaceTest.kt`

## Contract

See `ARCHITECTURE.md` § Domain Layer § DieFace:

```kotlin
enum class DieFace(val pips: Int) {
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6);
    companion object {
        fun fromInt(value: Int): DieFace
    }
}

fun DieFace.dotGrid(): List<List<Boolean>> // 3x3, row-major, true = dot present
```

## Requirements

- All six faces representable; `fromInt` covers exactly 1..6.
- Dot layouts match a traditional die (opposite faces sum to 7: 1↔6, 2↔5, 3↔4).
- No Compose/Android imports — this returns booleans, not pixels.

## Done when

- `fromInt(1..6)` round-trips to the right `pips`.
- Each `dotGrid()` has exactly `pips` `true` cells and matches a conventional die layout.
- Mark Phase 3 `Done` in `PROGRESS.md`.

---

# Phase 4: Dice State & Engine

## Goal

The state machine: idle vs. rolling, and the transitions between them. This is the part that
broke in the previous attempt — follow the contract exactly rather than redesigning it.

## Files

- Create `domain/DiceState.kt`
- Create `domain/DiceEngine.kt`
- Create `test/domain/DiceEngineTest.kt`

## Contract

See `ARCHITECTURE.md` § Domain Layer § DiceState / DiceEngine, and § Decision: who owns the
~5-second timing. In short: the engine is synchronous, has no timers, and is a plain class (never
an `object`).

```kotlin
sealed interface DiceState {
    data class Idle(val faceValue: Int) : DiceState
    data class Rolling(val target: Int) : DiceState
}

class DiceEngine(initialFace: Int = 1) {
    var state: DiceState = DiceState.Idle(initialFace)
        private set
    fun startRoll(random: Random = Random.Default): Boolean
    fun completeRoll()
}
```

## Requirements

- `startRoll()` while `Idle`: picks a result via `DiceRoller.roll()`, moves to
  `Rolling(target)`, returns `true`.
- `startRoll()` while already `Rolling`: no-op, returns `false`, `target` unchanged.
- `completeRoll()` while `Rolling(target)`: moves to `Idle(target)`.
- `completeRoll()` while `Idle`: no-op.
- No coroutines, no `delay`, no duration constant anywhere in this file — timing belongs to
  Phase 7, not here.

## Done when

- All four behaviors above have a passing test.
- Mark Phase 4 `Done` in `PROGRESS.md`.

---

# Phase 5: Basic Compose UI

## Goal

Get a `DiceEngine` on screen with no animation yet — tapping should instantly flip between
states so the wiring can be verified before animation is layered on.

## Files

- Create `ui/DieView.kt`
- Update `MainActivity.kt`

## Requirements

- `remember { DiceEngine() }` — one instance for the composable's lifetime.
- Display the current face (via `DieFace.fromInt(state.faceValue or target)`, plain text/number
  is fine at this stage — dot rendering comes in Phase 6).
- Tapping calls `startRoll()` and then **immediately** `completeRoll()` (no animation yet, so a
  tap should instantly show a new settled result). This isolates "does the state wiring work"
  from "does the animation work," which gets layered in at Phase 7.
- Centred on screen.

## Done when

- Tapping repeatedly always shows a new (possibly repeated) value 1–6.
- No crashes, no dead taps.
- Mark Phase 5 `Done` in `PROGRESS.md`.

---

# Phase 6: Die Rendering

## Goal

Replace the placeholder text from Phase 5 with a real drawn die face using `DieFace.dotGrid()`.

## Files

- Update `ui/DieView.kt`

## Requirements

- Draw all six faces from `dotGrid()` (Canvas or equivalent Compose drawing — no image assets).
- Scales correctly across screen sizes.

## Done when

- Every face 1–6 visually matches a traditional die when triggered.
- Mark Phase 6 `Done` in `PROGRESS.md`.

---

# Phase 7: Rolling Animation

## Goal

Add the ~5 second shake + settle animation, and move the `startRoll()`/`completeRoll()` pairing
from "instant" (Phase 5's shortcut) to animation-driven, per `ARCHITECTURE.md`'s timing decision.

## Files

- Create `animation/RollAnimation.kt`
- Update `ui/DieView.kt` (remove the Phase 5 immediate `completeRoll()` call)

## Requirements

- On `startRoll()` returning `true`, a `LaunchedEffect(state)` (or equivalent) runs a shake
  visual for ~5 seconds, then calls `completeRoll()`.
- The ~5 second constant lives here, not in the domain layer.
- Transition into the final result is smooth, not abrupt.
- Taps during `Rolling` are already no-ops via the engine (Phase 4) — no extra UI-side flag
  needed.

## Done when

- Tap → shake for ~5s → settles on the face `DiceEngine` picked at tap time.
- Repeated taps during the animation do nothing.
- Mark Phase 7 `Done` in `PROGRESS.md`.

---

# Phase 8: Integration

## Goal

Confirm the full flow end to end with nothing left stubbed from earlier phases.

## Done when

- Full flow works: tap → shake → settle → tap again, indefinitely, no broken states.
- Mark Phase 8 `Done` in `PROGRESS.md`.

---

# Phase 9: Testing and Polish

## Goal

Final pass against `FEATURES.md`'s Acceptance Criteria.

## Tasks

- Confirm every item in `FEATURES.md` § Acceptance Criteria is actually true (manually verify the
  UI/animation items; run the test suite for the domain items).
- Fix anything that doesn't hold up. Do not add new features while doing this.

## Done when

- Every Acceptance Criteria checkbox in `FEATURES.md` is checked.
- Mark Phase 9 `Done` in `PROGRESS.md` and update the Snapshot section to reflect v1 complete.

---

# Implementation Rules

## Do

- Make small, focused changes, one phase at a time.
- Run tests after changes.
- Update `PROGRESS.md` before ending a session, even mid-phase.

## Do Not

- Add features outside the current phase.
- Create abstractions the current phase doesn't need.
- Add dependencies without updating `ARCHITECTURE.md` first.
- Refactor unrelated code.
- Leave `PROGRESS.md` stale — a future session trusts it completely.
