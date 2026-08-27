# Dicey Dicey Agent Guidelines

## Working Style

When making changes:

1. Read the relevant documentation before coding.
2. Make the smallest change required to complete the task.
3. Do not rewrite or reorganise unrelated code.
4. Do not create abstractions unless they solve an existing problem.
5. Prefer completing one feature fully before starting another.

Before modifying files:
- Identify which files need to change.
- Explain the intended changes briefly.
- Confirm the approach if requirements are ambiguous.

After changes:
- Run relevant tests.
- Report what changed and any remaining issues.

---

## Project Context

Dicey Dicey is an Android application built with:

- Kotlin
- Jetpack Compose
- Gradle

The current target is Android only.

Future portability is a consideration, but should not influence implementation unless specifically requested.

---

## Design Principles

### Simplicity

Prefer simple, readable solutions.

Avoid:
- unnecessary abstractions
- premature optimisation
- additional frameworks
- unnecessary dependencies

A small amount of duplication is preferable to complex architecture.

---

## Architecture Rules

Separate:

### Domain Logic
Contains:
- dice rolling rules
- animation state management
- business behaviour

Must not depend on:
- Android Context
- Activities
- Composables
- Android lifecycle APIs

### UI Layer
Contains:
- Compose components
- touch handling
- visual rendering
- Android-specific behaviour

---

## Kotlin Style

Prefer:

- small focused classes
- descriptive names
- immutable values where possible
- simple functions

Avoid:
- overly generic classes
- unnecessary interfaces
- large files containing unrelated logic

---

## Dependencies

Do not add new dependencies without approval.

Prefer:
- Kotlin standard library
- Android SDK
- AndroidX libraries already included

---

## Testing

Add tests for:

- dice outcome generation
- state transitions
- important business rules

Do not write tests for trivial framework behaviour.

---

## Current Documentation

Before implementing features:

Read:

1. README.md
2. docs/FEATURES.md
3. docs/ARCHITECTURE.md
4. docs/IMPLEMENTATION-PLAN.md

Use these documents as the source of truth.