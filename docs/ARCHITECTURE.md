# Dicey Dicey Architecture

## Overview

Dicey Dicey is an Android application built using Kotlin and Jetpack Compose.

The application follows a simple separation between:

- Domain logic: rules and behaviour of rolling a die
- UI layer: displaying the die and handling user interaction

The architecture is intentionally lightweight. The goal is to keep the code easy to understand, test, and maintain.

---

# Architectural Principles

## Keep Logic Independent

Core application behaviour should not depend on Android UI components.

Business rules should be testable without requiring:

- Activities
- Composables
- Android Context
- Device resources

---

## Prefer Simplicity

The application should avoid unnecessary abstraction.

Do not introduce:

- Complex dependency injection
- Multiple layers without a clear purpose
- Generic frameworks
- Interfaces that only have one implementation

A simple solution is preferred over a highly abstract one.

---

# Component Structure

The application is divided into the following areas:

app/
└── src/
├── main/
│ ├── domain/
│ ├── animation/
│ ├── ui/
│ └── MainActivity.kt
│
└── test/


---

# Domain Layer

The domain layer contains the core rules of the dice application.

Responsibilities:

- Generate valid dice results.
- Represent the current die state.
- Define valid state transitions.
- Contain behaviour that does not require Android APIs.

The domain layer should not know:

- How the die is drawn.
- How animations are displayed.
- How touch events are received.

---

## Die Result

The application represents a standard six-sided die.

The domain layer is responsible for:

- Generating values from 1 to 6.
- Ensuring each value has equal probability.
- Returning results independently from previous rolls.

---

## Dice State

The application has two main states:

### Ready

The die:

- Has a current face value.
- Is waiting for user input.

### Rolling

The die:

- Is transitioning towards a new result.
- Does not accept additional roll requests.

The UI observes these states and displays the appropriate visual behaviour.

---

# Animation Layer

The animation layer controls visual movement during a roll.

Responsibilities:

- Manage rolling animation timing.
- Transition from rolling to settled state.
- Provide animation state information to the UI.

The animation layer should not decide:

- What number is rolled.
- How the die face is drawn.

The final result should come from the domain layer.

---

# UI Layer

The UI layer is implemented using Jetpack Compose.

Responsibilities:

- Display the die.
- Render the current face.
- Handle user taps.
- Trigger roll actions.
- Display animation states visually.

The UI layer should not contain:

- Random number generation.
- Dice rules.
- Business logic.

---

# Rendering

Die faces should be generated programmatically.

Requirements:

- Support faces 1 through 6.
- Match traditional die layouts.
- Scale correctly across screen sizes.

External image assets should not be required.

---

# Data Flow

The expected flow is:

User taps die
|
v
UI sends roll request
|
v
Domain generates result
|
v
Animation begins
|
v
UI displays rolling state
|
v
Animation completes
|
v
UI displays final face


---

# Testing Strategy

Tests should focus on behaviour rather than implementation details.

Important areas:

## Dice Logic Tests

Verify:

- Results are always between 1 and 6.
- Invalid values cannot be produced.
- Multiple rolls are independent.

---

## State Tests

Verify:

- Initial state is ready.
- Starting a roll changes state correctly.
- Completing a roll returns to ready state.
- Additional roll requests during rolling are ignored.

---

# Dependencies

The project should minimise dependencies.

Preferred:

- Kotlin standard library.
- Android SDK.
- AndroidX libraries required for Compose.

Additional dependencies should only be added when they provide clear value.

---

# Future Considerations

The current architecture keeps business logic separated from the UI.

If additional platforms are added in the future, the existing separation may make reuse easier.

However, future portability should not introduce unnecessary complexity into the Android implementation.

