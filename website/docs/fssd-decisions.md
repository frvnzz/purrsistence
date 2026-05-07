---
sidebar_position: 2
---

# Project Decisions

## Project Overview

**App:** Purrsistence  
A gamified time management and habit tracking app where users stay focused, track progress, and earn
rewards (cats, items). Includes social features and weekly statistics.

**Problem:**  
Users struggle with consistency due to high effort, low engagement, and lack of motivation in
existing apps.

---

## Technology Stack

- **Language:** Kotlin
- **Backend / Services:** Supabase

**🧠 Reasoning:** Kotlin is taught at university → team familiarity. Supabase provides fast setup,
auth, and realtime features without needing a custom backend.

---

## Repository Setup

- **Platform:** GitHub
- **Structure:** Single repository (monorepo)
- **Guidelines:** A
  [`CONTRIBUTING.md`](https://github.com/frvnzz/purrsistence/blob/main/CONTRIBUTING.md)
  file is maintained for internal reference and contribution rules

**🧠 Reasoning:** Simpler setup, easier collaboration for a small team.

---

## Git Workflow

- **Strategy:** Branch-based workflow as defined in `CONTRIBUTING.md`
- Branch off `main` and keep branches focused
- Open PRs against `main`, direct pushes are not allowed
- Required GitHub Actions check `Unit Tests (all)` must pass before merging
- No force pushes, no direct deletions of `main`
- After merging, branches are deleted or renamed to `archive/<name>`

---

## Branching Strategy

- Branch names follow flexible conventions defined in `CONTRIBUTING.md`
- Common prefixes:
    - `feature/<name>` → new features
    - `fix/<name>` → bug fixes
    - `chore/<name>` → maintenance / dependencies / config
    - `dev/<name>` → optional shared development branch
    - `main` → stable production branch

- Naming is not strictly enforced by Git, but consistency is expected
- Branch names should be short, clear, and descriptive

**🧠 Reasoning:**  
We do not enforce strict naming rules for our branches (e.g. through regex), but we rely on
conventions to keep the repository clean and organized.

---

## Commit Conventions

- Uses Conventional Commits as defined in CONTRIBUTING.md

Format:

```text
<type>(<scope>): <short description>
```

Examples:

- feat(goals): add goal creation screen
- fix(timer): prevent background crash

Supported types:

`feat`, `fix`, `chore`, `refactor`, `style`, `docs`, `test`, `perf`, `build`, `ci`, `revert`

**🧠 Reasoning:**  
We use Conventional Commits to improve readability of our commit history. Furthermore, with this we
have support for automated tooling like CHANGELOG generation, if we ever were to add them.

Read more: https://www.conventionalcommits.org/

---

## Code Review

- Code reviews are not required for merging
- If possible, still review changes from others
- If you do, leave specific, constructive comments
- Authors resolve comments before merge

---

## Project Management

- **Tool:** Jira
- **Methodology:** Scrum (required)
- **Practices:** Sprints, backlog, task breakdown into tickets
- **🧠 Reasoning:** Structured workflow required by the MLAB; supports planning and team coordination

---

## Externals of the Application

- **Authentication:** Not integrated yet (planned). The app currently uses local Room data only
  (`User` entity plus the seed insert in `MainActivity`), with no auth SDK wired in.
- **Profile Images:** Picked with the Android photo picker. Local `content://` images are copied
  into app cache when needed, then stored in Room as a local file URL through `ProfileService`.
  No external image storage service is connected yet.
- **Time:** Abstracted via `TimeProvider`; production uses `SystemTimeProvider : TimeProvider`,
  while tests use `FakeTimeProvider : TimeProvider` for timing.
- **Database:** Local storage via Room (`Goal`, `TrackingSession`, `User` entities). No external
  database or backend integration is fully connected yet, although Supabase is planned for later.
- **Statistics:** Implemented locally through Room (`Dao.observeTotalTime(goalId)` and
  `GoalWithSessions` relation). No external analytics/statistics provider is connected yet.

<!-- **Notifications:** Not integrated yet. `TrackingSession` contains `pauseReminder`, but there is currently no notification scheduling/service implementation. -->

---

## Architecture

---

## Frontend / Backend Split

---

## Data Storage

<!-- Explain Data Translation (Entity, DTO, Presentation) and how it applies to our app. Logic has no DB entity names (-> leaks, etc.) -->

---

## Domain Model

---

## Trade-offs

- **+ Fast development (Supabase)**
- **+ Familiar tech (Kotlin)**
- **– Less backend control**
- **– Supabase lock-in**

---

## Tasks from Class

### Task - April 14th, 2026

Think about a future data class we might want to add to the app and define it.

```kotlin
data class ShopItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: UShort,
    val pathToImage: String
)
```
