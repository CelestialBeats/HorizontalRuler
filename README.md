# HorizontalRuler

A lightweight and customizable Android `View` to pick numeric values with horizontal drag/fling + step snapping.

## Why Use It
- Smooth drag and fling interaction
- Snaps to your desired step (`valueStep`)
- Supports negative and positive ranges
- Fully styleable from XML and Kotlin
- Runtime APIs for dynamic reconfiguration
- Safe state restore on configuration changes

Below are some sample Image:

![Slant Layout Example](https://github.com/user-attachments/assets/721e9026-2417-406c-89bf-6ba6657461d6)

## Demo
The sample app includes 2 examples:
- XML-only configuration
- Runtime Kotlin configuration (change range, style, spacing, step, and animate to value)

## Installation

### Option 1: Local module
Add the library module to your project and include:

```kotlin
implementation(project(":horizontalscrollview"))
```

## Quick Start (XML)

```xml
<com.cb.horizontalscrollview.HorizontalRuler
    android:id="@+id/horizontalRuler"
    android:layout_width="match_parent"
    android:layout_height="72dp"
    app:minValue="-150"
    app:maxValue="150"
    app:initialValue="0"
    app:valueStep="0.5"
    app:tickSpacing="38dp"
    app:minorTicksPerMajor="10"
    app:snapAnimationDurationMs="220"
    app:showBaseline="true"
    app:showEdges="true"
    app:flingEnabled="true"
    app:tickColor="#6A6A6A"
    app:indicatorColor="#E83CB0"
    app:rulerBackgroundColor="#FFF5F8"
    app:edgeColor="#BD9519" />
```

## Quick Start (Kotlin)

```kotlin
binding.horizontalRuler.setRange(min = -50f, max = 250f, initValue = 35f)
binding.horizontalRuler.setStepValue(step = 0.5f)
binding.horizontalRuler.setMinorTicksPerMajor(tickCount = 8)
binding.horizontalRuler.setTickSpacingPx(spacingPx = 30f * resources.displayMetrics.density)
binding.horizontalRuler.setSnapAnimationDuration(durationMs = 260)
binding.horizontalRuler.setDrawOptions(drawBaseline = true, drawEdges = true)
binding.horizontalRuler.setFlingEnabled(enabled = true)
binding.horizontalRuler.setOnValueChangeListener { value ->
    binding.valueText.text = String.format("%.1f", value)
}
binding.horizontalRuler.scrollToValue(value = 42.5f, animated = true)
```

## XML Attributes

- `minValue` (float, default `0`)
- `maxValue` (float, default `100`)
- `initialValue` (float, default midpoint)
- `valueStep` (float, default `1`)
- `minorTicksPerMajor` (int, default `10`)
- `tickSpacing` (dimension, default `38dp`)
- `majorTickHeight` (dimension, default `46dp`)
- `mediumTickHeight` (dimension, default `30dp`)
- `minorTickHeight` (dimension, default `18dp`)
- `tickStrokeWidth` (dimension, default `1dp`)
- `indicatorWidth` (dimension, default `2dp`)
- `indicatorHeight` (dimension, default `46dp`)
- `baselineStrokeWidth` (dimension, default `1dp`)
- `edgeStrokeWidth` (dimension, default `1.5dp`)
- `baselinePaddingBottom` (dimension, default `8dp`)
- `edgeInset` (dimension, default `8dp`)
- `tickColor` (color)
- `indicatorColor` (color)
- `rulerBackgroundColor` (color)
- `edgeColor` (color)
- `showBaseline` (boolean, default `true`)
- `showEdges` (boolean, default `true`)
- `flingEnabled` (boolean, default `true`)
- `snapAnimationDurationMs` (int, default `160`)

## Public APIs

- `reload(min, max, initValue)`
- `setRange(min, max, initValue)`
- `scrollToValue(value)`
- `scrollToValue(value, animated)`
- `setStepValue(step, snapCurrentValue)`
- `setMinorTicksPerMajor(tickCount)`
- `setTickSpacing(spacingPx)`
- `setTickSpacingPx(spacingPx)`
- `setSnapAnimationDuration(durationMs)`
- `setDrawOptions(drawBaseline, drawEdges)`
- `setFlingEnabled(enabled)`
- `setColors(tickColor, indicatorColor, backgroundColor, edgeColor)`
- `setStrokeWidths(tickStrokeWidthPx, baselineStrokeWidthPx, indicatorWidthPx, edgeStrokeWidthPx)`
- `setOnValueChangeListener(listener)`
- `scrollListener` (`ScrollRulerListener` for backward compatibility)

## Requirements
- `minSdk 24`
- Kotlin + Android View system

## License
Add your preferred license file (`MIT`, `Apache-2.0`, etc.) before publishing.
