# Testing Strategy

## Test Definitions

### Unit Test:
Tests if one specific class or function behaves correctly under controlled conditions. All external dependencies are replaced wtih fake versions for testing purposes, so that the result of the test only depends on the code being tested. A unit test should be fast, deterministic and independent of databases, network connections or (other services).  In the app, unit tests are mainly used to test if the mappers, the service logic and the Viewmodel state changes work correctly by themselves.

### Integration Test:
Verifies whether multiple real parts of the application work correctly together. Instead of isolating one class, it checks the interaction between the layers such as DAOs, repositories, mappers and Room database. The goal is mostly to prove that the communication between two seperate parts of logic works correctly.

### End-to-End test:
Tests a complete workflow from the users perspective. It is intended to check whether a user action leads to the expected result across all relevant layers. The purpose is to confirm that the app works correctly from the user´s perspective and does not target only one specific interaction or class.

## Test Types

### 1. Unit Tests
- **Location**: `app/src/test/java/com/example/purrsistence`
- **Scope**: Testing business logic in isolation (ViewModels, Services, Repositories).
- **Tools**: JUnit 4, Kotlinx Coroutines Test.
- **Mocks/Fakes**: We prefer using "Fakes" (e.g., `FakeTimeProvider`) over mocking frameworks where possible for better performance and readability.

### 2. Instrumented Tests
- **Location**: `app/src/androidTest/java/com/example/purrsistence`
- **Scope**: Testing UI components and database operations that require an Android environment.
- **Tools**: Espresso, Compose UI Test.

---

## Running Tests

### Via Android Studio
- Right-click on a test class or package and select **Run 'Tests in...'**.

### Via Command Line
To run all unit tests:
```bash
./gradlew test
```

To run a specific test class:
```bash
./gradlew testDebugUnitTest --tests "com.example.purrsistence.domain.service.TrackingServiceTest"
```

---

## Code Coverage

We use **JaCoCo** to measure and report code coverage.

### Generating Reports
To run tests and generate a coverage report, execute:
```bash
./gradlew jacocoTestReport
```

### Viewing Reports
The reports are generated in both HTML and XML formats:
- **HTML**: `app/build/reports/jacoco/jacocoTestReport/html/index.html`
- **XML**: `app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`