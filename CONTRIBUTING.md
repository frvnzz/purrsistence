# Contributing to Purrsistence

## Branch Strategy

Branch names can be freely chosen, there is no strict rule enforced by Git.  
However, using consistent prefixes helps to keep the repository organized and easier to navigate.

Commonly used branch naming conventions:

| Branch           | Purpose                                               |
| ---------------- | ----------------------------------------------------- |
| `main`           | Stable, production-ready code                         |
| `feature/<name>` | New features                                          |
| `fix/<name>`     | Bug fixes                                             |
| `chore/<name>`   | Maintenance, dependencies, config                     |
| `dev/<name>`     | (Optional) Shared development branch for collaboration|

**Examples:** `feature/cat-ui`, `fix/timer-crash`, `chore/update-dependencies`

### Notes
- You are **not limited** to these names. Choose branch names that best describe your work.
- Prefixes like `feature/`, `fix/`, etc. are **conventions, not requirements**.
- The most important thing is **clarity and consistency within the team**.
- Keep branch names short but descriptive (e.g., `feature/login-flow`, not `feature/stuff`).
- A `dev/<name>` branch can be useful when multiple developers work on different parts of the same feature (e.g., one builds a Kotlin UI component while another implements backend logic), or as a temporary integration branch to verify functionality before merging into `main`.

---

## Workflow

1. Branch off `main` and keep branches focused
2. Make small, meaningful commits
3. Push and open a PR against `main`, direct pushes are not allowed
4. No force pushes, no direct deletions of `main`
5. After merging, either delete the branch or rename it to `archive/<name>`

---

## Commit Conventions

Use conventional commit messages as follows:

```text
<type>(<scope>): <short description>
```

| Type       | When to use                                                  |
| ---------- | ------------------------------------------------------------ |
| `feat`     | New feature                                                  |
| `fix`      | Bug fix                                                      |
| `chore`    | Dependencies, config, maintenance                            |
| `refactor` | Code restructure, no behavior change                         |
| `style`    | Formatting, whitespace, lint fixes (no code logic change)    |
| `docs`     | Documentation only                                           |
| `test`     | Adding or updating tests                                     |
| `perf`     | Performance improvements                                     |
| `build`    | Build system or dependency changes (e.g., npm, gradle)       |
| `ci`       | CI/CD configuration changes                                  |
| `revert`   | Revert a previous commit                                     |

**Examples:** `feat(goals): add goal creation screen` · `fix(timer): prevent background crash`

Read more about [Conventional Commits](https://www.conventionalcommits.org/).

---

## Code Review

- Code reviews are not required for merging
- If possible, still review changes from others
- If you do, leave specific, constructive comments
- Author resolves their own threads before merging
