# Contributing to Purrsistence

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Stable, production-ready code |
| `feature/<name>` | New features |
| `fix/<name>` | Bug fixes |
| `chore/<name>` | Maintenance, dependencies, config |

**Examples:** `feature/cat-cafe-ui`, `fix/timer-crash`, `chore/update-dependencies`

---

## Workflow

1. Branch off `main` and keep branches focused
2. Make small, meaningful commits
3. Push and open a PR against `main` — direct pushes are not allowed
4. No force pushes, no direct deletions of `main`

---

## Commit Conventions
Use conventional commit messages as follow:
```
<type>(<scope>): <short description>
```

| Type | When to use |
|---|---|
| `feat` | New feature |
| `fix` | Bug fix |
| `chore` | Deps, config, non-functional |
| `refactor` | Code restructure, no behavior change |
| `style` | Formatting, UI tweaks |
| `docs` | Documentation only |

**Examples:** `feat(goals): add goal creation screen` · `fix(timer): prevent background crash`

Read more about Conventional Commits [here](https://www.conventionalcommits.org/).

---

## Code Review

- Code reviews are not required for merging
- If possible, still review changes from others
- Leave specific, constructive comments
- Author resolves their own threads before merging
