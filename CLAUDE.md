# CLAUDE.md

## Context

This is a native Android app. The product surface itself (screens, features, exact flows) is defined through conversation and issues, not fixed here. This file is a **development guideline document**: it governs *how* code gets written, structured, and reviewed in this repo, regardless of what feature is being built. Do not treat this file as a spec or a feature list. If a feature decision and a guideline in this file ever seem to conflict, ask rather than silently picking one.

## Guardrails Against Scope Creep / AI Slop

These are hard rules, not suggestions. They apply to every change, big or small.

1. **No speculative abstraction.** Do not build generic, configurable, or "pluggable" architecture for something that currently has one concrete use case. Solve the problem in front of you. Generalize only when a second, real use case actually shows up, not in anticipation of one.
2. **No new dependencies without justification.** Before adding any library, state in one sentence why it's needed and what it replaces. Default to Android/Kotlin standard library and Jetpack first. Don't pull in a library for something a few lines of code can do.
3. **File and class count should track actual complexity.** If the number of files, interfaces, or layers is growing faster than the underlying logic, stop and consolidate. A small feature should not require a small architecture diagram to explain.
4. **No premature infrastructure.** Don't introduce a backend, sync layer, dependency injection framework, or multi-module structure until the app's actual requirements clearly justify it. Local-first and simple by default.
5. **No unused code.** Don't generate helper functions, interfaces, or classes "for future flexibility" that nothing currently calls. Delete dead code immediately instead of leaving it "just in case." Grep for usages before assuming something is needed.
6. **One clear data model per concept.** Avoid parallel or overlapping models representing the same real-world thing. If two models look similar, that's a signal to merge them, not a coincidence to ignore.
7. **Every unit of code should be traceable to a real requirement.** If you can't point to what currently asks for a piece of code, it probably shouldn't exist yet.
8. **Prefer deletion and reuse over addition when changing existing code.** Before writing something new, check whether existing code can be trimmed, moved, or reused instead.
9. **No copy-pasted near-duplicates.** If similar logic appears in two places, extract it once it appears a second time, not preemptively, not never.
10. **Stop and ask before large structural changes.** Introducing a new architectural pattern (a new layer, a new module boundary, a new state-management approach) is a decision to flag explicitly, not to make silently mid-task.

## Code Style

- Idiomatic modern Kotlin. Prefer Kotlin standard library constructs (scope functions, data classes, sealed classes) over verbose Java-style patterns.
- Small, single-purpose functions over long ones. A function should do one thing you can name clearly.
- Descriptive names over comments explaining unclear names. Comment *why*, not *what*, when a comment is needed at all.
- No commented-out code left in the codebase, delete it; git history preserves it if ever needed.
- One consistent formatter/lint config (ktlint or Android Studio's default Kotlin style) decided at project start. Don't churn on style later.
- Favor immutability (`val` over `var`, immutable data classes) unless mutability is genuinely required.

## Architecture Defaults

- Keep UI, state, and data concerns separated, but don't over-engineer the separation. A pragmatic MVVM-ish structure (UI -> ViewModel -> a plain data source) is enough until real complexity demands more.
- Local persistence (Room, DataStore, or plain file storage, whichever fits the data shape) is the default. Don't add a backend or network layer unless a feature explicitly requires one.
- Favor Jetpack Compose and current recommended Android practices over legacy patterns (XML layouts, AsyncTask, etc.) unless there's a specific reason to do otherwise.
- Avoid introducing a DI framework (Hilt/Koin) until the object graph is complex enough that manual construction is genuinely painful. Simple constructor injection is fine for a small app.

## ViewModel File Convention (hard rule)

Deterministic so it can be caught mechanically:

- **Every `ViewModel` lives in its own file under `com.example.monitrack.ui.viewmodel`, named `<Feature>ViewModel.kt`** (e.g. `TrackViewModel.kt`, `SettingsViewModel.kt`). The file's `ViewModel` class name must match its file name.
- **Never declare a class extending `ViewModel`/`AndroidViewModel` inside a screen, composable, or any file whose name does not end in `ViewModel.kt`.** A `class …ViewModel` in `*Screen.kt` (or anywhere outside `ui/viewmodel/`) is a violation.
- Types that exist only as a ViewModel's input/output state (its UI-state data classes, its filter enums) belong in that ViewModel's file, not in the screen file.
- Screens obtain their ViewModel via `viewModel()` and import it from `ui.viewmodel`; they must not define it.
- Grep check for a violation: a `class .*ViewModel` match in any file not matching `ui/viewmodel/*ViewModel.kt`.

## Definition of Done for Any Change

Before considering a task complete, confirm:
- [ ] The change was scoped to what was actually asked, nothing extra was bundled in
- [ ] No new files, classes, or abstractions were added beyond what the change strictly needs
- [ ] Any new dependency has a one-line justification
- [ ] Dead code, unused imports, and unused parameters were removed, not left behind
- [ ] The change was actually run/tested, not just written
- [ ] Naming is clear enough that a comment isn't needed to explain intent

## Working Style

- When a request is ambiguous, ask a clarifying question rather than guessing and building the wrong thing at scale.
- When a task could be done in a small way or an elaborate way, default to the small way unless told otherwise.
- Surface trade-offs briefly when a non-obvious architectural choice is made, don't silently commit to a big decision without a one-line rationale.