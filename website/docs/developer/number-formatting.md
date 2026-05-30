# Number Formatting

For localization-aware number formatting in the application, format numbers using the functions from
`NumberFormatter.kt`. With this, decimal and grouping separators stick to the user's
localized phone settings (e.g., displaying `10.0` or `10,0` correctly, and formatting large numbers
like `1,000` or `1.000` accurately).

## Available Helpers

You can find the following functions in `com.example.purrsistence.ui.util.NumberFormatter`:

- `formatLocalizedNumber`: Base function for highly customized formatting (provides min and max
  fraction digits, and use grouping).
- `formatLocalizedDecimal`: Helpful for formatting Double/Float values.
- `formatLocalizedInteger`: Helpful for formatting integers (e.g. balance, price, goals), ensuring
  large values use appropriate grouping separators based on locale.

## Usage Example

```kotlin
import com.example.purrsistence.ui.util.formatLocalizedInteger

Text(text = formatLocalizedInteger(balance)) // e.g. 1,000 or 1.000
```
