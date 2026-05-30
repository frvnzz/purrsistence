# Tracking Sessions & Sectioned Rewards

This document explains the core logic behind focus tracking, multiplier resets, and the "Sectioned Reward" system.

## Core Concepts

The tracking system is designed to reward consistent focus while being fair to users who take long breaks.

### 1. The Multiplier
Users earn a multiplier on their focus time that increases every 15 minutes of effective focus, up to a maximum of **2.0x**.
*   **0 - 15 min**: 1.0x
*   **15 - 30 min**: 1.15x
*   **30 - 45 min**: 1.25x
*   ... and so on.

### 2. Multiplier Resets
If a user pauses for **15 minutes or longer in a single interval**, the multiplier is reset back to **1.0x**. Short pauses do not trigger a reset.

---

## Sectioned Reward System

To ensure users don't lose the bonus currency they earned before a reset, we use a "Sectioned Reward" approach.

### The Checkpoint Logic
When a long pause (> 15m) is detected upon resuming:
1.  **Calculate Current Earnings**: The system calculates the coins earned in the focus block *before* the pause began, using the multiplier active at that time.
2.  **Checkpointing**: These coins are "locked in" and added to a `checkpointedCurrency` total.
3.  **Reset Focus Timer**: The "effective minutes since last reset" is set to zero. The next focus block starts fresh at 1.0x.

### Final Reward Calculation
Total Session Reward = `checkpointedCurrency` + `currentBlockEarnings`.

---

## Data Persistence (`pauseHistory`)

All session data is stored in the `TrackingSessionEntity`. To avoid adding unnecessary columns to the database, we use a structured string field called `pauseHistory`.

**Format**: `totalPausedMillis;interval1Start-interval1End,...;checkpointedCurrency`

*   **Segment 1**: Total cumulative paused time in milliseconds (used for SQL statistics).
*   **Segment 2**: A comma-separated list of all pause intervals (used to detect if any single pause exceeded the 15m limit).
*   **Segment 3**: The total currency earned in previous focus blocks that have been reset.

---

## Real-time UI Updates

The `TrackingViewModel` runs a ticker every second to update the UI:
*   **Multiplier Arc**: Shows progress towards the next multiplier (0% to 100% of the current 15m block).
*   **Live Multiplier**: Shows the current active multiplier (e.g., x1.15).
*   **Earned Counter**: Displays the live sum of `checkpointedCurrency` plus the potential coins being earned in the current block.