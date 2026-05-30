# Statistics

The Statistics screen shows users their weekly tracking progress with a chart and per-goal statistics.
It uses a responsive layout that adapts between portrait and landscape orientations for better usability.

## Main files

- `StatisticsScreen.kt`: main screen composable that handles responsiveness with `BoxWithConstraints`.
- `StatisticsViewModel.kt`: manages UI state, loads weekly stats, and handles week navigation (previous/next).
- `WeeklyChart.kt`: displays daily tracking aggregates as a visual chart (in hours, shows empty state if no data).
- `WeekSelector.kt`: navigation to move between weeks.
- `GoalStatsList.kt`: shows tracked time per goal as a list with progress bars.
- `StatisticsUiState.kt`: holds UI data (daily stats, goal stats, loading state, week offset).
- `StatisticsService.kt`: fetches weekly and goal statistics from repositories.
- `TimeFormatter.kt`: utility function that formats minutes into human-readable time strings (e.g., "45 min", "2h 30min").

## Responsive layout design

### Portrait mode (width ≤ 600dp)

1. Full-width vertical layout
2. Scrollable column with all content
3. Order: Week selector → Chart → Goal stats
4. Works well on phones in portrait

### Landscape mode (width > 600dp)

1. Two-column layout using `Row`
2. Left side: Fixed chart (45% width) - never scrolls
3. Right side: Scrollable column (55% width)
4. Only goal entries scroll while chart stays visible
5. Much better for tablets and landscape phones

The threshold of 600dp is a common breakpoint that catches most phones in landscape and smaller tablets.

## Time formatting

- Chart displays time in hours only (cleaner axis, easier to read)
- Goal stats use dynamic formatting: minutes when under 1h, hours and minutes when over (e.g., "2h 30min")
- `formatMinutes()` utility handles all logic in one place

## Empty state

- When no tracking data exists for a week, the chart shows a message instead of an empty grid
- Prevents confusing empty axis labels

## Implementation notes

- Both layout variants share the same components (chart, selector, stats list)
- `BoxWithConstraints` checks screen width at composition time to pick the right layout
- No external libraries needed - uses standard Compose APIs
