# Dicey Dicey Features & Requirements

This doc defines *what* v1 must do. `ARCHITECTURE.md` defines *how* it's built.
`IMPLEMENTATION-PLAN.md` defines the order. `PROGRESS.md` tracks where things currently stand.

## Overview

Dicey Dicey simulates rolling a standard six-sided die. The user interacts only with the die
itself: tap to roll, watch the animation, see the result.

---

# Version 1 Scope

- A single six-sided die
- Tap-to-roll interaction
- Animated rolling behaviour
- Random result generation
- A die rendered as a real 3D model
- Ability to roll again immediately after completion

---

# User Interaction

## Initial State

- A single die is displayed showing an initial face value.
- No buttons or additional controls. The entire visible die area responds to taps.

## Starting a Roll

Tapping the die immediately generates a new random result and begins the rolling animation. No
confirmation step.

## Rolling Behaviour

### Phase 1: Rolling Animation

- The die visibly moves/shakes.
- The animation feels unpredictable; the final result isn't obvious immediately.
- Runs for approximately 5 seconds.

### Phase 2: Settling Animation

- The die transitions smoothly into its final position and displays the result clearly.
- The animation ends naturally, not abruptly.
- After settling, the die returns to the ready state and can be tapped again immediately.

---

# Random Result Requirements

- Valid results are integers 1–6.
- Each result has equal probability (1/6).
- Previous rolls do not affect future rolls.
- No roll history needs to be stored.

---

# Die Display Requirements

- All faces 1–6 supported, matching a traditional die (opposite faces sum to 7).
- Readable across screen sizes; centred on screen.
- Rendered as a real 3D model (see `ARCHITECTURE.md` § 3D Die Model) — a code-only fake-3D
  attempt (a flat layer with a perspective transform) was tried and rejected as unconvincing,
  since it can only ever show one face at a time. The die's visual is a single bundled `.glb`
  asset (mesh + materials together, authored in Blender), which is a deliberate, narrow exception
  to "no external image assets" — there are still no separate per-face image files.

---

# Animation Requirements

- Smooth motion, no visible stuttering under normal operation.
- The die visibly tumbles in 3D while rolling — multiple real faces should be visible passing by,
  not just one face wobbling — and the transition into the final result feels natural.
- Real-world physics accuracy is not required — a convincing visual effect is what matters.

---

# Touch Behaviour

## Supported

- **Tap**: starts a roll when idle. Does nothing while a roll is already in progress.

## Unsupported in v1

Long press, multi-touch, swipe, buttons/menus, changing dice type, interrupting a roll with a
second tap.

---

# Application States

## Ready

Displays the current result, waits for input, can be tapped to start a new roll.

## Rolling

Runs the rolling animation, ignores additional input, transitions automatically to the final
result.

---

# Non-Functional Requirements

- **Simplicity**: no unnecessary screens, configuration, or unrelated features.
- **Performance**: starts quickly, maintains smooth animation, works across common Android screen
  sizes at `minSdk = 30` and above (see `ARCHITECTURE.md`).
- **Dependencies**: only the Android/AndroidX libraries already in the project, plus one
  sanctioned exception — a 3D rendering library for the die model (see `ARCHITECTURE.md`
  § Dependencies).

---

# Out of Scope for Version 1

Multiple dice, other dice types (D4/D8/D10/D12/D20), roll history, statistics, sound effects,
themes, customisation, online features, accounts, leaderboards, settings screens, interrupting a
roll with a second tap.

These may be considered for future versions.

---

# Acceptance Criteria

This is the **only** completion checklist for v1 — `IMPLEMENTATION-PLAN.md` does not keep a
separate one, to avoid the two drifting apart. Check `PROGRESS.md` for current status against
this list.

- [x] The app opens showing a die.
- [x] Tapping the die starts a rolling animation.
- [x] A random value from 1–6 is selected, with equal probability, independent of prior rolls.
- [x] The rolling animation runs for approximately 5 seconds.
- [x] The die smoothly settles on the final result.
- [x] All six die faces render correctly on the 3D model, matching a traditional die.
- [x] The user can immediately start another roll after completion.
- [x] Additional taps during rolling do not interrupt the animation or change its target result.
- [x] The application builds successfully (`./gradlew build`).
- [x] Domain unit tests cover: dice roll range/fairness, and `DiceEngine` state transitions
      (idle → rolling → idle, and rejecting a second `startRoll()` while rolling).

Verified 2026-09-24 (see `PROGRESS.md` for how each was checked). Two notes: the "smoothly
settles" item is a judgement call, confirmed by eye by the project owner rather than measured;
and the ~5 second figure was measured inside the app (5.017s per roll, starting ~15ms after the
tap) — screenshot-based timing on the emulator read ~6s because of display/capture latency.
