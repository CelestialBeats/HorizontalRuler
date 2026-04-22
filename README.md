# HorizontalRuler Library

`HorizontalRuler` is a customizable Android `View` for selecting numeric values by horizontal drag/fling with snapping.

## Features
- Min/max range with automatic clamping
- Configurable step snapping (`valueStep`)
- Fling + snap animation
- Custom tick/indicator colors and dimensions
- Configurable tick density (`minorTicksPerMajor`)
- Optional baseline and edge lines
- Runtime APIs for range/style updates
- Rotation-safe state restore

## XML Usage
```xml
<com.cb.horizontalscrollview.HorizontalRuler
    android:id="@+id/horizontalRuler"
    android:layout_width="match_parent"
    android:layout_height="64dp"
    app:minValue="-100"
    app:maxValue="100"
    app:initialValue="0"
    app:valueStep="0.5"
    app:tickSpacing="38dp"
    app:minorTicksPerMajor="10"
    app:snapAnimationDurationMs="180"
    app:showBaseline="true"
    app:showEdges="true"
    app:flingEnabled="true" />
```

## Kotlin Usage
```kotlin
binding.horizontalRuler.setRange(min = -100f, max = 100f, initValue = 12.5f)
binding.horizontalRuler.setStepValue(step = 0.5f)
binding.horizontalRuler.setOnValueChangeListener { value ->
    binding.valueText.text = value.toString()
}
```

## Public Runtime APIs
- `reload(min, max, initValue)` (existing, backward compatible)
- `setRange(min, max, initValue)`
- `scrollToValue(value, animated = false)`
- `setStepValue(step, snapCurrentValue = true)`
- `setMinorTicksPerMajor(tickCount)`
- `setTickSpacing(spacingPx)` / `setTickSpacingPx(spacingPx)`
- `setSnapAnimationDuration(durationMs)`
- `setDrawOptions(drawBaseline, drawEdges)`
- `setFlingEnabled(enabled)`
- `setColors(tickColor, indicatorColor, backgroundColor, edgeColor)`
- `setStrokeWidths(tickStrokeWidthPx, baselineStrokeWidthPx, indicatorWidthPx, edgeStrokeWidthPx)`
- `setOnValueChangeListener { }`

## Next Release Suggestions
- Add label/text rendering for major values
- Add RTL support toggle/inversion mode
- Add optional haptic feedback on step crossing
- Add Jetpack Compose wrapper
- Add Maven Central publishing config (or JitPack release tags)
