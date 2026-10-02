# Dicey Dicey Agent Guidelines

This doc covers *how* to work in this repo. It does not restate architecture or requirements —
those live in `docs/ARCHITECTURE.md` and `docs/FEATURES.md`. If anything here ever conflicts with
those, fix the conflict rather than picking one — architecture rules belong only in
`docs/ARCHITECTURE.md`.

## Session Start

1. Read `docs/PROGRESS.md` first. It names the current phase and the last thing that happened.
2. Read only that phase's section in `docs/IMPLEMENTATION-PLAN.md`, plus the parts of
   `docs/ARCHITECTURE.md` it references. You don't need to reload the whole doc set every time.
3. If something is ambiguous and none of the docs resolve it, decide it, write the decision down
   (in `docs/ARCHITECTURE.md` if it's a permanent rule, or `docs/PROGRESS.md` § Open Decisions
   otherwise), and proceed — don't leave it open for the next session to re-derive.

## While Working

- Make the smallest change required to complete the current phase.
- Do not rewrite or reorganise unrelated code.
- Do not create abstractions unless the current phase's contract requires them.
- Follow the exact type/function signatures given in `docs/IMPLEMENTATION-PLAN.md` for the
  current phase — they're fixed contracts, not suggestions, and later phases depend on them.
- Run relevant tests after changes.

## Session End

Before ending, update `docs/PROGRESS.md`:

- Mark the phase's status (`In progress`, `Blocked`, or `Done`).
- Add one short log entry (a few lines, not a narrative).
- Update "Next Action" to whatever the next session should do first.

Do this even if the phase isn't finished — an incomplete `PROGRESS.md` update is worse than a
short one.

## Dependencies

Do not add a new dependency without first updating `docs/ARCHITECTURE.md`'s Dependencies section
to say why.
