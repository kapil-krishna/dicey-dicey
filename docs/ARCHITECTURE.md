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

**One deliberate, explicit exception:** a 3D rendering library for the die model (see § 3D Die
Model below). A pure-2D fake-perspective animation was tried and rejected as visually
unconvincing — a flat layer with a perspective transform can only ever show one face at a time,
which can't look like an actual tumbling object. This is a narrow, justified exception for one
specific need, not a loosening of the rule in general — don't read it as license to add other
frameworks.

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

Represents which of a traditional die's faces is showing (1–6). Still domain, not UI — it's just
"which face is this," independent of how it gets drawn.

```kotlin
enum class DieFace(val pips: Int) {
    ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6);

    companion object {
        fun fromInt(value: Int): DieFace // throws or clamps on values outside 1..6 — pick one and be consistent
    }
}
```

**`dotGrid()` is retired.** The original 2D-programmatic-rendering approach (a `dotGrid(): List<List<Boolean>>`
extension mapping a face to which cells of a 3x3 grid are lit, drawn with Compose `Canvas`) has
been superseded by the 3D model approach below — see § 3D Die Model. `dotGrid()` and its Canvas
drawing code in `ui/DieView.kt` should be deleted when the 3D model replaces them, not kept
alongside as a fallback. Requirement carried forward either way: layouts must match a traditional
die (opposite faces sum to 7: 1↔6, 2↔5, 3↔4).

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
   plays the roll animation (the 3D tumble — see § 3D Die Model) for ~5 seconds.
3. When that effect finishes, it calls `diceEngine.completeRoll()`, which flips `Rolling -> Idle`
   and the UI recomposes to show the final face.

The domain layer is a pure state machine driven entirely by two method calls. The animation
layer decides when the second call happens. Neither layer needs to know how to do the other's
job.

---

# 3D Die Model

The die is rendered as a real 3D model, not drawn in 2D. This replaces the original Phase 6/7
approach (a flat Compose `Canvas` face plus a fake-perspective `graphicsLayer` rotation), which
was built, tried on-device, and rejected as unconvincing — a single flat layer can only ever show
one face at a time, so it read as a card tilting rather than an object tumbling. See
`PROGRESS.md`'s log for that attempt.

## Creating the asset

- Model a cube with the pip pattern textured/sculpted onto each of its 6 faces in a 3D modeling
  tool. **Blender** (free) is the standard choice — either model it from scratch or start from a
  free pre-made low-poly die model.
- Export as a single `.glb` file (glTF binary — the standard real-time 3D interchange format; it
  bundles the mesh and textures together, so this is still one asset, not six separate per-face
  images).
- Requirement carried over from the retired `dotGrid()` approach: layouts must match a
  traditional die (opposite faces sum to 7: 1↔6, 2↔5, 3↔4).
- Bundle the exported `.glb` under `app/src/main/assets/` (create this directory when adding it).

**Current asset:** `app/src/main/assets/die.v2.glb` (5.7MB, glTF 2.0 binary, Blender's glTF I/O
v5.2.40 exporter, exported with modifiers applied and smooth shading). One mesh (`Cube`) with two
primitives/materials: `Dot` (near-black, 21 separate connected components — the pips, ~7.4k
vertices) and `Face` (light gray, the body, ~100k vertices) — no textures, so the pips are real
geometry, not a painted-on texture. Export with **Apply Modifiers** checked and the mesh set to
Shade Smooth — an earlier 74KB export without them rendered the dots as diamonds with faceted
edges (the low-poly base mesh, not the subdivided one).

## Rendering it

Jetpack Compose has no native 3D renderer, so this needs a rendering library — the deliberate,
documented exception under "Prefer Simplicity" above. **SceneView for Jetpack Compose**
(`io.github.sceneview:sceneview:4.38.0`, wrapping Google's **Filament** renderer) is pinned in
`gradle/libs.versions.toml` / `app/build.gradle.kts`. Key API surface used:

```kotlin
val engine = rememberEngine()
val modelLoader = rememberModelLoader(engine)

SceneView(modifier = Modifier.fillMaxSize(), engine = engine, modelLoader = modelLoader) {
    rememberModelInstance(modelLoader, "die.v2.glb")?.let { instance ->
        ModelNode(modelInstance = instance, rotation = Rotation(x = ..., y = ..., z = 0f))
    }
}
```

`ModelNode`'s `rotation` is Euler angles (`Rotation(x, y, z)` in degrees) and is reactive to
Compose state — updating the backing state redraws the model at the new orientation, same as any
other Compose state. The asset path `"die.v2.glb"` is resolved relative to `app/src/main/assets/`.

## Driving the roll

The rolling motion is still entirely code-driven — only the rendering mechanism changes:

- **Idle**: the model sits at the fixed orientation that shows `DiceState.Idle.faceValue` face-up
  (see § Face-value → orientation mapping below).
- **Rolling**: the animation layer applies a randomized rotation to `rotationX`/`rotationY` over
  ~5 seconds (same timing ownership as before — see § Decision above), so multiple real faces are
  visible as it spins, then eases into the exact orientation for `DiceState.Rolling.target` and
  calls `completeRoll()`. Settling must land on the correct angle **modulo 360°** relative to
  however many full spins accumulated during the tumble (round to the nearest angle congruent to
  the target, don't animate back to the raw target value directly) — otherwise it snaps backward
  instead of settling smoothly, the same bug the original 2D attempt had.

## Face-value → orientation mapping

Determined by analyzing the actual mesh data in `die.v2.glb` (decoding the glTF's vertex buffer,
grouping the `Dot` material's geometry into 21 connected components — one per pip — and
classifying each by which of the 6 local axis directions its centroid is nearest to). Pip counts
per local direction, in the model's own coordinate space:

| Local direction | Pips | Opposite | Pips | Sum |
|---|---|---|---|---|
| +X | 6 | -X | 1 | 7 ✓ |
| +Y | 4 | -Y | 3 | 7 ✓ |
| +Z | 5 | -Z | 2 | 7 ✓ |

All three pairs sum to 7, confirming this matches a correctly-built traditional die.

Assuming the default camera looks toward the object along **-Z** (so the face pointing toward
world **+Z** is the one visible to the viewer — SceneView's default single-object camera setup),
the rotation needed to bring each face value to world +Z is a single-axis 90°/180° rotation
(derived by solving each case with a standard rotation matrix — no guessing):

| Face value | rotation.x | rotation.y | rotation.z |
|---|---|---|---|
| 1 | 0 | 90 | 0 |
| 2 | 180 | 0 | 0 |
| 3 | -90 | 0 | 0 |
| 4 | 90 | 0 | 0 |
| 5 | 0 | 0 | 0 |
| 6 | 0 | -90 | 0 |

**Unverified assumption:** the camera-faces-toward--Z direction, and SceneView/Filament's
rotation sign convention matching the standard right-handed math used to derive this table. Both
are plausible defaults but not confirmed on-device yet. If the wrong face shows at rest, the fix
is mechanical, not a redesign: either the camera assumption is backwards (swap the target from
+Z to -Z and re-derive) or the sign convention is flipped (negate every non-zero angle above) —
verify by rendering `Idle(1)` and checking whether face 1 is actually shown, then correct this
table and this note.

## Open questions still to resolve

- Whether the `DieFace` enum is still worth keeping now that `dotGrid()` is gone, or whether
  everything should just key off the raw `Int` face value directly (the table above already does).

---

# Animation Layer

Responsibilities:

- Own the ~5 second roll duration (the single place this constant lives).
- Drive the 3D roll animation (see § 3D Die Model) while `state is DiceState.Rolling`.
- Call `diceEngine.completeRoll()` when the animation finishes.
- Provide a smooth transition into the settled face's exact orientation.

Must not:

- Decide what number is rolled (that's already decided by `startRoll()`, held in `Rolling.target`).
- Contain dice rules.

---

# UI Layer

Responsibilities:

- Own a single `DiceEngine` instance for the screen's lifetime (e.g. `remember { DiceEngine() }`).
- Embed and render the 3D die model (see § 3D Die Model), setting its orientation from the
  current `DiceState`.
- Forward taps to `diceEngine.startRoll()` unconditionally (see Decision above).
- Contain no dice rules and no random number generation.

---

# Data Flow

```
User taps
  -> UI calls diceEngine.startRoll()               [domain: picks result, sets Rolling(target)]
  -> UI recomposes on Rolling, animation layer starts a ~5s 3D tumble
  -> animation layer finishes, calls diceEngine.completeRoll()  [domain: Rolling -> Idle(target)]
  -> UI recomposes on Idle, shows final face
```

---

# Testing Strategy

## Domain tests (JVM unit tests, no Android, no coroutines)

`test/domain/`:

- `DiceRoller.roll()` always returns 1..6.
- Repeated rolls are not all identical (basic fairness sanity check — see
  `IMPLEMENTATION-PLAN.md` Phase 2 for the exact non-flaky approach).
- `DiceEngine`: initial state is `Idle`. `startRoll()` moves to `Rolling`, returns `true`, and
  picks its target with the supplied `Random`. `startRoll()` while already `Rolling` returns
  `false` and does not change `target`. `completeRoll()` moves `Rolling(target)` to
  `Idle(target)`. `completeRoll()` while `Idle` is a no-op.

## Animation logic (JVM unit tests)

`test/animation/RollAnimationTest.kt`. `targetRotation` and `spinTo` in `RollAnimation.kt` are
`internal` (not `private`) so they can be tested directly, and `spinTo` takes a
`random: Random = Random.Default` parameter for the same reason `DiceRoller` does.

- `targetRotation` matches the on-device-verified face → orientation table exactly, gives six
  distinct orientations, and rejects values outside 1..6.
- `spinTo` always lands on an angle equivalent (mod 360) to the target, adds 4–7 full turns in
  either direction (both directions and both range ends are reachable), and continues from the
  current angle rather than resetting.

## Roll lifecycle (instrumented Compose tests — need a device/emulator)

`androidTest/animation/RollAnimationComposeTest.kt` drives `rememberRollAnimation` on the Compose
test clock with `autoAdvance = false`, so the ~5s roll is checked exactly without waiting for it:
idle shows each face's resting orientation and never completes; a roll is in motion part-way
through, completes exactly once between 4.9s and 5.1s, and settles on the target face (all six,
back to back); returning to idle snaps to the exact resting angle; leaving `Rolling` early
cancels without calling `onRollComplete`.

`androidTest/MainActivityTest.kt` is a launch smoke test: the app starts (SceneView engine and
`die.v2.glb` load) and stays resumed.

Run with `./gradlew connectedDebugAndroidTest`. Not part of `./gradlew build`.

## Still manual

Tapping the die in `DieView` (tap → `startRoll()` → state flows into the animation) and the
visual result. SceneView handles taps on its native surface outside Compose (see UI Layer), so
Compose test input can't reliably reach it, and there's no hook to observe the engine from
outside without changing `DieView` for tests. The pieces either side of that tap are covered
above; the wiring is checked manually per `FEATURES.md`. The template theme files
(`ui/theme/`) aren't tested.

---

# Dependencies

Kotlin standard library, Android SDK, and the AndroidX/Compose libraries already in
`gradle/libs.versions.toml`. No new dependency without updating this doc first.

**One sanctioned exception**, per § 3D Die Model above: **SceneView for Jetpack Compose**
(`io.github.sceneview:sceneview:4.38.0`, wrapping Filament) to display the die's `.glb` model.
Pinned in `gradle/libs.versions.toml` and `app/build.gradle.kts`. This is the only dependency
allowed outside the "Kotlin/Android SDK/AndroidX" set, and only for this purpose.

---

# Target Platform

- `minSdk = 30`, `targetSdk` / `compileSdk = 37` (already set in `app/build.gradle.kts` — this is
  the actual, decided baseline; nothing further to resolve here).
