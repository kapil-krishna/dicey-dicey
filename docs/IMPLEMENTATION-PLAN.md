# Dicey Dicey Implementation Plan

## Purpose

This document defines the implementation order for Dicey Dicey.

The goal is to build the application incrementally, keeping each change small, testable, and easy to review.

Complete each phase before moving to the next phase.

Do not introduce future features or additional architecture unless required by the current phase.

---

# Phase 1: Project Foundation

## Goal

Ensure the Android project builds successfully with the required structure.

## Tasks

- Confirm Kotlin and Jetpack Compose are configured.
- Confirm the application launches successfully.
- Create the initial package structure.

Expected structure:


domain/
ui/
animation/


## Completion Criteria

- Application builds successfully.
- Application launches on Android.
- Package structure exists.
- No unnecessary dependencies are added.

---

# Phase 2: Dice Result Generation

## Goal

Implement the core dice rolling logic.

## Files

Create:


domain/DiceRoller.kt


Create tests:


test/DiceRollerTest.kt


## Requirements

The dice roller must:

- Return values between 1 and 6.
- Give each value an equal probability.
- Not depend on Android classes.
- Be independently testable.

## Completion Criteria

- Dice roller exists.
- Unit tests pass.
- No UI code exists in this component.

---

# Phase 3: Die Face Representation

## Goal

Create a representation of the six possible die faces.

## Files

Create:


domain/DieFace.kt


## Requirements

The representation must:

- Support values 1 through 6.
- Allow the UI to determine which dots should be displayed.
- Match traditional die layouts.

## Completion Criteria

- All six faces are represented.
- Invalid face values cannot be created.
- Logic is independent of Compose.

---

# Phase 4: Rolling State Management

## Goal

Implement the application state transitions.

## Files

Create:


domain/DiceState.kt
domain/DiceEngine.kt


## Requirements

The engine must manage:

### Ready State

Contains:

- Current die value.
- Ability to start a roll.

### Rolling State

Contains:

- Current rolling status.
- Final selected result.

Behaviour:

- Starting a roll moves from Ready to Rolling.
- A roll generates a new result.
- Completing a roll returns to Ready.
- Roll requests while already rolling are ignored.

## Completion Criteria

- State transitions work correctly.
- State logic has unit tests.
- No UI dependencies exist.

---

# Phase 5: Basic Compose UI

## Goal

Display the die on screen.

## Files

Create:


ui/DieView.kt


Update:


MainActivity.kt


## Requirements

The UI must:

- Display a die.
- Show the current face value.
- Centre the die on screen.
- Respond to taps.

At this stage:

- No animation is required.
- Tapping can simply trigger a state change.

## Completion Criteria

- App displays a die.
- User can tap the die.
- UI correctly displays different face values.

---

# Phase 6: Die Rendering

## Goal

Create the final visual representation of the die.

## Files

Update:


ui/DieView.kt


## Requirements

The die should:

- Render all six faces.
- Use programmatic drawing.
- Scale correctly.
- Match traditional die layouts.

## Completion Criteria

- Each face renders correctly.
- No image assets are required.
- Rendering works on different screen sizes.

---

# Phase 7: Rolling Animation

## Goal

Add the rolling visual experience.

## Files

Create:


animation/RollAnimation.kt


Update:


ui/DieView.kt


## Requirements

When rolling:

- Die visibly shakes/moves.
- Animation lasts approximately 5 seconds.
- Animation transitions smoothly into the final result.
- The user sees a clear final face.

The animation does not need realistic physics.

## Completion Criteria

- Rolling animation starts after tapping.
- Animation completes automatically.
- Final result is displayed correctly.
- App returns to ready state.

---

# Phase 8: Integration

## Goal

Connect all components together.

## Requirements

Complete flow:


User taps die
|
v
DiceEngine starts roll
|
v
Animation begins
|
v
Animation completes
|
v
New face displayed


## Completion Criteria

- Full user flow works.
- User can roll repeatedly.
- No broken states exist.

---

# Phase 9: Testing and Polish

## Goal

Ensure the application is reliable.

## Tasks

Add or improve tests for:

- Dice generation.
- State transitions.
- Roll behaviour.

Verify:

- Application builds.
- Animation is smooth.
- UI works on different screen sizes.

---

# Implementation Rules

While following this plan:

## Do

- Make small focused changes.
- Complete one phase before starting another.
- Run tests after changes.
- Keep existing behaviour working.

## Do Not

- Add features outside this plan.
- Create unnecessary abstractions.
- Add dependencies without approval.
- Refactor unrelated code.
- Rewrite working code without a clear reason.

---

# Definition of Done

Version 1 is complete when:

- The app launches successfully.
- The user can tap the die to roll.
- The die animates during the roll.
- A random face is displayed.
- The user can roll again.
- Core logic has tests.
- The code remains simple and maintainable.