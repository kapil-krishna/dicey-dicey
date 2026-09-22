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

- **Current phase:** All nine phases done — **v1 is complete.** Every `FEATURES.md` Acceptance
  Criteria box is checked.
- **Last updated:** 2026-09-28
- **Build status:** `./gradlew build` passes clean. Installed and manually confirmed on the
  Pixel 10 Pro emulator: tap starts a roll, die tumbles in place (no toss/lift, removed per
  feedback) with a punchy-but-smooth settle, centred on a black background, using the final
  re-exported model (circular dots, smooth edges).

---

## Phase Checklist

| Phase | Name | Status |
|---|---|---|
| 1 | Project Foundation | Done |
| 2 | Dice Roll Generation | Done |
| 3 | Die Face Dot Layout | Done |
| 4 | Dice State & Engine | Done |
| 5 | Basic Compose UI | Done |
| 6 | 3D Die Model & Rendering | Done |
| 7 | Rolling Animation | Done |
| 8 | Integration | Done |
| 9 | Testing & Polish | Done |

Status values: `Not started`, `In progress`, `Done`, `Blocked`, `Needs rework`.

---

## Log

Most recent entry first. Keep entries short — a few lines each, not a narrative.

- **2026-09-28** — Also stopped tracking `.idea/misc.xml` (add to `.gitignore` list — same
  reasoning as `deviceManager.xml`/`gradle.xml`: it recorded a machine-local JDK name that
  changed on its own during a Gradle sync). Fixed two stale lines in `FEATURES.md` left over from
  the 3D pivot ("programmatically rendered" → "rendered as a real 3D model"; "mesh + textures" →
  "mesh + materials", since the model has no textures). Documented the release/signing process in
  `README.md` § Release Build (keystore generation, `keystore.properties` pattern,
  `assembleRelease`/`bundleRelease`) — this project has no `signingConfigs` set up yet, so this is
  a future manual step, not something completed. Added `keystore.properties`/`*.jks`/`*.keystore`
  to `.gitignore` pre-emptively so a future keystore can't be committed by accident. User is
  pushing the current work manually; session ended here.
- **2026-09-24** — Phase 9 (acceptance pass) done; all 10 criteria in `FEATURES.md` checked.
  No code changes were needed. How each was verified: `./gradlew build` passes (also from clean);
  `./gradlew test` passes 9/9 — `DiceEngineTest` (6: initial idle, idle→rolling, second
  `startRoll()` rejected with `target` unchanged, rolling→idle, `completeRoll()` no-op while
  idle, re-roll after completion) and `DiceRollerTest` (2: range over 1000 rolls, seeded
  fairness over 600) plus the template `ExampleUnitTest`. "Independent of prior rolls" holds by
  construction (`DiceRoller` is stateless; no test asserts it, by design — see Phase 2 notes).
  On the emulator, driven via `adb input tap`: app opens on a die and is stable at idle; a tap
  starts motion; a tap right after settling starts a new roll; four taps during one roll were
  all received but produced exactly one animation start/end (no restart or extension). Timing was
  measured with temporary `Log.d` lines (removed; files restored via `git checkout`): the
  animation runs 5.017s (three rolls: 5018/5017/5017 ms) and starts 11–17 ms after the tap. A
  screenshot-diff approach read ~6s on the emulator (display/capture latency, not the animation).
  All six faces verified in Phase 8. "Smoothly settles" is a by-eye judgement (owner confirmed),
  not measured. Left alone: `.idea/misc.xml` shows a JDK-name change from the IDE's Gradle sync
  (`jbr-25` → `temurin-25`) — machine-specific, not part of this work.
- **2026-09-24** — Phase 8 verification: confirmed the face-value → orientation mapping on-device
  rather than assuming it. Temporarily built with `DiceEngine(initialFace = n)` for n = 1..6,
  installed each on the emulator, and read a screenshot of each: 1 = centre dot, 2 = diagonal,
  3 = diagonal + centre, 4 = four corners, 5 = corners + centre, 6 = two columns of three — all
  correct, so the table in `ARCHITECTURE.md` (derived from mesh geometry) and its unverified
  camera/rotation-sign assumptions are now confirmed. This checks the `Idle` orientation; a roll
  settles on the same target (spins are added in whole multiples of 360°), so the post-roll
  orientation is the same. `DieView.kt` was restored exactly afterwards (`git diff` clean) and the
  real build reinstalled. Screenshots were temporary and deleted.
- **2026-09-24** — Swapped in the re-exported `die.v2.glb` (modifiers applied, smooth shading):
  74KB → 5.7MB, dot mesh 168 → 7,386 vertices, face mesh 1,514 → 100,779. Re-ran the pip-per-axis
  geometry analysis on the new file — mapping unchanged (+X=6, −X=1, +Y=4, −Y=3, +Z=5, −Z=2), so
  no code changes were needed. Installed; no crashes or OOM in logcat; user confirmed it "looks
  perfect." Resolves the dot-shape/rough-edges Open Decision (cause was unapplied modifiers, as
  suspected). `ARCHITECTURE.md`'s current-asset note updated to match.
- **2026-09-22** — Removed the vertical toss arc per feedback — the die now stays in place and
  only rotates during a roll. `animation/RollAnimation.kt`'s `liftY`/`keyframes`/toss constants
  removed; `RollTransform` is back to just `rotationX`/`rotationY`; `ui/DieView.kt`'s `ModelNode`
  no longer passes a `position`.
- **2026-09-22** — Phases 6/7 rework completed and confirmed working on-device, after several
  rounds of fixes:
  - Pinned `io.github.sceneview:sceneview:4.38.0` (verified via Maven Central + the library's own
    compiled `.api` dump, not just its docs — the docs' example claiming a `Rotation(x,y,z)` type
    turned out to be inaccurate; the real type is `dev.romainguy.kotlin.math.Float3`). Required
    bumping `kotlin` from 2.2.10 to 2.4.10 in `gradle/libs.versions.toml` — SceneView pulls in a
    newer `kotlin-stdlib` than the old compiler could read, which surfaced as a wall of unrelated
    "incompatible Kotlin metadata version" errors across the whole codebase, not just the new file.
  - Derived the face-value → orientation mapping by parsing `die.v2.glb`'s actual vertex buffer
    (grouping the "Dot" material's 21 connected components by nearest local axis) rather than
    guessing — recorded in `ARCHITECTURE.md` § 3D Die Model. (Was unverified on-device at the
    time; verified for all six faces on 2026-09-24 — see the Phase 8 entry above.)
  - Fixed tap handling: `SceneView`'s `cameraManipulator` defaults to non-null (enabling
    pinch/orbit/double-tap-zoom), and its native surface consumes touch events for that before a
    `Modifier.clickable` layered outside it would ever see them. Set `cameraManipulator = null`
    and moved tap handling to `onGestureListener`'s `onSingleTapConfirmed`.
  - Fixed sizing: `SceneView` was filling the whole screen; wrapped it in a centered `Box` sized
    to 70% width, square aspect ratio.
  - Fixed the visible black box: set `Scaffold`'s `containerColor` to black app-wide so it blends
    with `SceneView`'s own black-cleared viewport instead of showing as a boxed edge.
  - Reworked the roll motion twice: first fixed a real concurrency bug (the tumble loop animated
    the X rotation fire-and-forget while awaiting Y, and the settle phase animated X and Y
    *sequentially* — both could desync); then redesigned entirely away from "discrete tumble loop
    + separate settle" (which had an inherent speed discontinuity where the two phases met) to one
    continuous eased motion per axis — several extra full spins landing exactly on the target,
    animated in one `tween` — plus a vertical toss arc via `keyframes`. Switched the easing from
    `FastOutSlowInEasing` to `LinearOutSlowInEasing` (Material's "decelerate" curve — full speed
    from frame one) after feedback that it read as slow at both ends; user confirmed the result as
    smooth and appropriately punchy.
  - `domain/DieFace.kt` and its test were deleted (resolves the "keep or drop" Open Decision from
    last session) — fully unused once rendering stopped going through `dotGrid()`.
- **2026-09-21** — Pivoted Phases 6/7 from 2D/fake-3D to a real 3D model. Tried a code-only
  fake-3D tumble first (`graphicsLayer` with `rotationX`/`rotationY`/`cameraDistance` perspective,
  a toss arc, a fading ground shadow — all still driving the same flat `Canvas` face). On-device
  it looked flat/paper-like, not like an object tumbling — a single 2D layer can only ever show
  one face at a time, which is also a real functional gap (the requirement is that multiple real
  faces should be visible passing by while rolling). Decision: build the die as an actual 3D
  model (Blender → `.glb`, rendered via SceneView/Filament). Documentation updated —
  `ARCHITECTURE.md` now has a § 3D Die Model section with the full design and a documented,
  narrow exception to "minimise dependencies" for the rendering library; `FEATURES.md` and
  `README.md` updated to match; `IMPLEMENTATION-PLAN.md`'s Phase 6/7 rewritten for the new
  approach. `DieFace.dotGrid()` (from Phase 3) is marked retired — it and its Canvas-drawing code
  in `DieView.kt` get deleted when Phase 6 rework lands, not kept as a fallback. No domain-layer
  impact at all — `DiceRoller`/`DiceState`/`DiceEngine` (Phases 2–4) are unaffected, which is the
  architecture doing its job: swapping the rendering approach didn't ripple past the UI/animation
  layers. Blocked on the user producing the `.glb` model in Blender before code work resumes.
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

v1 is complete against `FEATURES.md`. Nothing is required. Candidate follow-ups, all explicitly
out of scope for v1 (see `FEATURES.md` § Out of Scope and the README roadmap):

- **Dark/light theme** (deferred by the user): the die's `Dot`/`Face` materials are flat
  base-colour values, so runtime recolouring should be possible without a second asset; the 3D
  view's black clear colour would also need to change so it doesn't show as a box in a light
  theme. Verify SceneView's material API before promising it.
- Other roadmap items: haptics, sound, roll history, additional dice types.

Adding any of these means updating `FEATURES.md` (scope) and `IMPLEMENTATION-PLAN.md` first —
don't just start coding.

Separately, not a feature and not tracked as a phase: **release signing isn't set up** — see
`README.md` § Release Build. `app/build.gradle.kts` has no `signingConfigs` block, so there's no
signed release build yet, only the debug-signed one from `installDebug`. Needed before this can
be sideloaded as a "real" build or published anywhere. Whoever does this generates the keystore
themselves (not something to do unprompted — it's a credential, and losing it has permanent
consequences if the app is ever published).

`IMPLEMENTATION-PLAN.md`'s Phase 6/7 sections are historical (they describe what was planned);
this log is the more detailed record of what actually happened, including the mid-course
redesigns.
