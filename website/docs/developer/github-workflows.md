---
sidebar_position: 2
---

# GitHub Workflows

This page explains the automations in `.github/workflows` for our project.

## Quick map

- `.github/workflows/android-ci.yml`: runs the Android unit tests.
- `.github/workflows/coverage.yml`: generates [JaCoCo](https://github.com/jacoco/jacoco) coverage
  and uploads to [Codecov](https://about.codecov.io/).
- `.github/workflows/deploy-website.yml`: builds and deploys Docusaurus to GitHub Pages.
- `.github/dependabot.yml`: opens dependency update PRs for configured ecosystems.
- `codecov.yml`: Codecov status/threshold/ignore rules.

## 1) Android CI (`android-ci.yml`)

### When it runs

- On push to any branch (`branches: ['**']`).
- On pull requests targeting `main`.
- On manual trigger (`workflow_dispatch`).

### What it does

1. checks out the repo
2. sets up JDK 17
3. runs `./gradlew test --stacktrace`
4. publishes JUnit test results
5. uploads test report artifacts

## 2) Coverage + Codecov (`coverage.yml`)

### When it runs

- On push to `main`
- On pull requests to `main`
- On manual trigger

### What it does

1. runs `./gradlew testDebugUnitTest jacocoTestReport --stacktrace`
2. uploads `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml` to Codecov
3. uploads JaCoCo artifacts for debugging

## 3) Website deployment (`deploy-website.yml`)

### When it runs

- On push to `main`

### What it does

Build job:

1. checks out repository
2. sets up Node 20
3. runs `npm ci` in `website/`
4. runs `npm run build`
5. uploads `website/build` as Pages artifact

Deploy job:

1. takes the build artifact
2. deploys with `actions/deploy-pages`

## 4) Dependabot (`.github/dependabot.yml`)

Current setup:

- enabled for `github-actions` and `gradle`
- checks weekly
- max 5 open Dependabot PRs
