# Progress

This is the single source of truth for **where the project currently stands**. Every other doc
describes the target or the plan; this doc describes reality right now.

Read this file first, every session, before anything else. It tells you which phase is active —
you only need to read that one phase's section of `IMPLEMENTATION-PLAN.md` in detail, plus the
parts of `ARCHITECTURE.md` it references. You do not need to re-read the whole doc set each time.

Update this file last, every session, before ending — even if the phase isn't finished. Future
sessions have no memory of this conversation; this file is the only thing that carries forward.

---

## Snapshot

- **Current phase:** Phase 6 (Die Rendering) — complete. Phase 7 not started.
- **Last updated:** 2026-09-21
- **Build status:** `./gradlew build` passes clean (compile, unit tests, lint, assemble).

---

## Phase Checklist

| Phase | Name | Status |
|---|---|---|
| 1 | Project Foundation | Done |
| 2 | Dice Roll Generation | Done |
| 3 | Die Face Dot Layout | Done |
| 4 | Dice State & Engine | Done |
| 5 | Basic Compose UI | Done |
| 6 | Die Rendering | Done |
| 7 | Rolling Animation | Not started |
| 8 | Integration | Not started |
| 9 | Testing & Polish | Not started |

Status values: `Not started`, `In progress`, `Done`, `Blocked`.

---

## Log

Most recent entry first. Keep entries short — a few lines each, not a narrative.

- **2026-09-21** — Phase 6 implemented: `ui/DieView.kt` updated to replace the plain pip-count
  text with an actual drawn die — a `Canvas` (60% of screen width, square aspect ratio) drawing a
  white rounded-rect face and black dots positioned from `DieFace.dotGrid()` (Phase 3). No image
  assets. Manually verified on the Pixel 10 Pro emulator: tapping cycles through all six faces
  and each dot layout matches a traditional die. `./gradlew build` passes clean.
- **2026-09-21** — Phase 5 implemented: `ui/DieView.kt` (a `DieView` composable holding
  `remember { DiceEngine() }`, mirroring `engine.state` into a `mutableStateOf` for recomposition,
  showing the current face's pip count as centered text via `DieFace.fromInt`) and `MainActivity.kt`
  updated to render it in place of the default "Hello Android" template (removed the unused
  `Greeting`/`GreetingPreview` composables). Tap calls `startRoll()` then immediately
  `completeRoll()` — no animation yet, deliberately deferred to Phase 7. Manually verified on a
  Pixel 10 Pro emulator: app opens showing a value, tapping instantly shows a new value 1–6.
  `./gradlew build` passes clean.
- **2026-09-21** — Phase 4 implemented: `domain/DiceState.kt` (`sealed interface DiceState` with
  `Idle(faceValue)` / `Rolling(target)`) and `domain/DiceEngine.kt` (a class, not an object —
  holds mutable `state`, exposes `startRoll()`/`completeRoll()`). Purely synchronous: no
  coroutines, no timers, no duration constant — that's deliberately deferred to Phase 7 per
  `ARCHITECTURE.md`'s timing decision. `test/domain/DiceEngineTest.kt` covers: initial state,
  idle→rolling on `startRoll()`, a second `startRoll()` while rolling being a no-op that leaves
  `target` unchanged, `completeRoll()` moving rolling→idle with the target value, `completeRoll()`
  while idle being a no-op, and rolling again after completing. `./gradlew build` passes clean.
- **2026-09-21** — Phase 3 implemented: `domain/DieFace.kt` (`enum class DieFace(val pips: Int)`
  with `fromInt`, plus a `dotGrid(): List<List<Boolean>>` extension mapping each face to a
  traditional 3x3 pip layout) and `test/domain/DieFaceTest.kt` (round-trip on `fromInt`, grid
  shape, dot count matches pips, and explicit layout checks for ONE/FOUR/SIX). No
  Compose/Android imports. `./gradlew build` passes clean.
- **2026-09-21** — Phase 2 implemented: `domain/DiceRoller.kt` (`object DiceRoller { fun
  roll(random: Random = Random.Default): Int }`, uniform 1..6 via `random.nextInt(1, 7)`) and
  `test/domain/DiceRollerTest.kt` (range check over 1000 rolls; a seeded-`Random(42)` fairness
  check over 600 rolls asserting each face appears at least half its expected count). No Android
  imports. `./gradlew build` passes clean.
- **2026-09-21** — Deleted `domain/DiceState.kt` and `domain/DieEngine.kt`: both predated the
  contracts in `ARCHITECTURE.md` and did not compile (interface called as a constructor,
  reference to an undeclared `faceValue`, a custom `Seed` type passed where `Random` expects an
  `Int`/`Long`). `domain/DieFace.kt` was already deleted before this session. Also deleted
  `test/domain/DiceRollerTest.kt` and its stray zero-byte `.ktc` sibling — both predated
  `DiceRoller`'s current contract and referenced a class that doesn't exist yet (its
  `kotlin.test.*` imports also weren't even resolvable — that dependency isn't in the project).
  `./gradlew build` now passes clean end to end. `domain/` is empty except `.gitkeep`. Left
  the Android-Studio-default `ExampleUnitTest.kt` / `ExampleInstrumentedTest.kt` in place — they
  don't reference any project code and aren't affected by this cleanup.
- **2026-09-19** — Documentation rewritten (README, ARCHITECTURE, FEATURES, IMPLEMENTATION-PLAN,
  AGENTS, this file). Project is being redone from Phase 2 onward. Phase 1 scaffolding (Gradle
  config, package structure, app launches) is untouched and still valid.

---

## Open Decisions

None currently. When a session hits a genuine ambiguity the docs don't resolve, decide it,
record the decision and its reasoning here (or in `ARCHITECTURE.md` if it's a permanent rule),
and only then proceed — don't leave it for the next session to re-derive.

---

## Next Action

Start Phase 7 in `IMPLEMENTATION-PLAN.md`: create `animation/RollAnimation.kt` and update
`ui/DieView.kt` to remove the Phase 5 shortcut (immediate `completeRoll()`). On `startRoll()`
returning `true`, run a `LaunchedEffect` shake for ~5 seconds, then call `completeRoll()`. The
duration constant lives in the animation layer, not the domain — see `ARCHITECTURE.md`'s
Decision on timing ownership. This is the last phase with meaningful new logic; Phase 8 is just
integration verification and Phase 9 is the final Acceptance Criteria pass.
